#!/usr/bin/env node
"use strict";

/**
 * Minecraft Forge 1.8 Mod Builder
 * ================================
 * A Node.js script that:
 *   1. Detects or auto-downloads a portable JDK 8
 *   2. Sets up (or uses an existing) Forge 1.8 MDK
 *   3. Builds the mod via Gradle
 *
 * Usage:
 *   node forge-build.js [options]
 *
 * Options:
 *   --mdk <path>         Path to existing Forge MDK directory
 *   --output <path>      Output directory for the built jar (default: ./build/libs)
 *   --forge-version <v>  Forge version for 1.8 (default: 1.8.9-11.15.1.2318-1.8.9)
 *   --no-jdk-check       Skip JDK detection / download
 *   --clean              Run clean build (removes build/ and .gradle)
 *   --help               Show this help message
 */

const { execSync, spawn, execFileSync } = require("child_process");
const { createReadStream, createWriteStream, existsSync, mkdirSync, rmSync, cpSync, statSync } = require("fs");
const { join, resolve, basename, dirname } = require("path");
const { createInterface } = require("readline");
const https = require("https");
const http = require("http");
const { pipeline } = require("stream/promises");
const { promisify } = require("util");
const { chmod, unlink } = require("fs/promises");
const os = require("os");
const zlib = require("zlib");
const url = require("url");

// ─── ANSI Colors ───────────────────────────────────────────────────────────────

const C = {
  reset: "\x1b[0m",
  bold: "\x1b[1m",
  red: "\x1b[31m",
  green: "\x1b[32m",
  yellow: "\x1b[33m",
  blue: "\x1b[34m",
  magenta: "\x1b[35m",
  cyan: "\x1b[36m",
  gray: "\x1b[90m",
};

// ─── Helpers ───────────────────────────────────────────────────────────────────

function log(msg, color = C.reset) {
  console.log(`${C.gray}[forge-build]${C.reset} ${color}${msg}${C.reset}`);
}

function success(msg) { log(msg, C.green); }
function warn(msg) { log(msg, C.yellow); }
function error(msg) { log(msg, C.red); }
function info(msg) { log(msg, C.cyan); }
function step(n, total, msg) { log(`${C.bold}Step ${n}/${total}:${C.reset} ${msg}`, C.blue); }

/** Parse CLI arguments */
function parseArgs() {
  const args = process.argv.slice(2);
  const opts = {};
  for (let i = 0; i < args.length; i++) {
    if (args[i] === "--help") {
      printHelp();
      process.exit(0);
    }
    if (args[i] === "--mdk" && args[i + 1]) { opts.mdkPath = args[++i]; continue; }
    if (args[i] === "--output" && args[i + 1]) { opts.outputPath = args[++i]; continue; }
    if (args[i] === "--forge-version" && args[i + 1]) { opts.forgeVersion = args[++i]; continue; }
    if (args[i] === "--no-jdk-check") { opts.skipJdkCheck = true; continue; }
    if (args[i] === "--clean") { opts.cleanBuild = true; continue; }
  }
  return opts;
}

function printHelp() {
  console.log(`
${C.bold}Minecraft Forge 1.8 Mod Builder${C.reset}
${C.cyan}Usage:${C.reset}  node forge-build.js [options]

${C.cyan}Options:${C.reset}
  --mdk <path>           Path to existing Forge 1.8 MDK directory
  --output <path>        Output directory for the built jar (default: ./build/libs)
  --forge-version <v>   Forge version string (default: 1.8.9-11.15.1.2318-1.8.9)
  --no-jdk-check         Skip automatic JDK 8 detection and download
  --clean                Delete build/ and .gradle caches before building
  --help                 Show this help message

${C.cyan}Examples:${C.reset}
  node forge-build.js
  node forge-build.js --mdk ./MyMod --clean
  node forge-build.js --forge-version 1.8-11.14.4.1563-1.8
`);
}

// ─── Platform Detection ────────────────────────────────────────────────────────

function getPlatform() {
  const p = process.platform;
  if (p === "win32") return "windows";
  if (p === "darwin") return "mac";
  return "linux";
}

function getArch() {
  return process.arch === "x64" ? "x64" : "x32";
}

function getJdkFileExtension() {
  const plat = getPlatform();
  if (plat === "windows") return ".zip";
  return ".tar.gz";
}

// ─── HTTP Download with Progress ───────────────────────────────────────────────

function downloadFile(fileUrl, destPath) {
  return new Promise((resolve, reject) => {
    const parsed = new URL(fileUrl);
    const client = parsed.protocol === "https:" ? https : http;

    client.get(fileUrl, { followAllRedirects: true }, (res) => {
      // Handle redirects
      if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
        return downloadFile(res.headers.location, destPath).then(resolve, reject);
      }

      if (res.statusCode !== 200) {
        reject(new Error(`HTTP ${res.statusCode} downloading ${fileUrl}`));
        return;
      }

      const totalBytes = parseInt(res.headers["content-length"], 10);
      let downloadedBytes = 0;
      let lastPercent = -1;

      const fileStream = createWriteStream(destPath);
      res.on("data", (chunk) => {
        downloadedBytes += chunk.length;
        if (totalBytes > 0) {
          const pct = Math.floor((downloadedBytes / totalBytes) * 100);
          if (pct !== lastPercent && pct % 5 === 0) {
            process.stdout.write(`\r  ${C.cyan}Downloading...${C.reset} ${pct}% (${formatBytes(downloadedBytes)} / ${formatBytes(totalBytes)})`);
            lastPercent = pct;
          }
        }
      });

      res.pipe(fileStream);
      fileStream.on("finish", () => {
        fileStream.close();
        console.log(); // newline after progress bar
        resolve(destPath);
      });

      fileStream.on("error", reject);
      res.on("error", reject);
    }).on("error", reject);
  });
}

function formatBytes(bytes) {
  if (bytes < 1024) return bytes + " B";
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + " KB";
  return (bytes / 1048576).toFixed(1) + " MB";
}

// ─── JDK 8 Detection ───────────────────────────────────────────────────────────

/**
 * Check if a given java binary is JDK 8.
 * Returns { version: "1.8.x", path: "/path/to/java" } or null.
 */
function probeJava(javaBin) {
  try {
    const output = execFileSync(javaBin, ["-version"], {
      encoding: "utf-8",
      stdio: ["pipe", "pipe", "pipe"],
    });
    // The version info is in stderr for OpenJDK/Oracle JDK
    const versionOutput = output;
    if (versionOutput.includes('"1.8') || versionOutput.includes('"8.')) {
      const match = versionOutput.match(/"(1\.8\.[^"]+)"|"(8\.[^"]+)"/);
      const ver = match ? (match[1] || match[2]) : "1.8.0";
      return { version: ver, path: javaBin };
    }
    return null;
  } catch {
    return null;
  }
}

/**
 * Search for JDK 8 on the system.
 * Checks JAVA_HOME, PATH, and common install locations.
 */
function findSystemJdk8() {
  info("Searching for JDK 8 on the system...");

  const candidates = [];

  // 1. Check JAVA_HOME
  if (process.env.JAVA_HOME) {
    const jh = process.env.JAVA_HOME;
    candidates.push(
      join(jh, "bin", "java"),
      join(jh, "bin", "java.exe")
    );
  }

  // 2. Common install locations
  const plat = getPlatform();
  if (plat === "windows") {
    const drive = process.env.SystemDrive || "C:";
    const programFiles = process.env.ProgramFiles || `${drive}\\Program Files`;
    const programFilesX86 = process.env["ProgramFiles(x86)"] || `${drive}\\Program Files (x86)`;
    candidates.push(
      join(programFiles, "Java", "jdk1.8.0_*", "bin", "java.exe"),
      join(programFilesX86, "Java", "jdk1.8.0_*", "bin", "java.exe"),
      join(programFiles, "Eclipse Adoptium", "jdk-8*", "bin", "java.exe"),
      join(programFiles, "AdoptOpenJDK", "jdk-8*", "bin", "java.exe"),
      join(programFiles, "Zulu", "zulu-8*", "bin", "java.exe"),
      join(drive, "tools", "jdk-8*", "bin", "java.exe")
    );
  } else if (plat === "mac") {
    candidates.push(
      "/Library/Java/JavaVirtualMachines/jdk1.8*/Contents/Home/bin/java",
      "/Library/Java/JavaVirtualMachines/temurin-8*/Contents/Home/bin/java",
      "/Library/Java/JavaVirtualMachines/zulu-8*/Contents/Home/bin/java",
      "/usr/local/opt/openjdk@8/bin/java",
      "/opt/homebrew/opt/openjdk@8/bin/java"
    );
  } else {
    candidates.push(
      "/usr/lib/jvm/java-8-openjdk-*/bin/java",
      "/usr/lib/jvm/java-8-oracle/bin/java",
      "/usr/lib/jvm/temurin-8-*/bin/java",
      "/usr/lib/jvm/adoptopenjdk-8-*/bin/java",
      "/usr/local/java/jdk-8*/bin/java",
      "/opt/java/jdk-8*/bin/java",
      "/opt/jdk-8*/bin/java"
    );
  }

  // 3. Try `which java` / `where java`
  for (const javaBin of candidates) {
    // Handle globs
    const { execSync: exec } = require("child_process");
    try {
      // For glob patterns, try expanding them
      if (javaBin.includes("*")) {
        let expanded;
        if (plat === "windows") {
          const base = dirname(javaBin.replace(/\\[^\\]*$/, ""));
          // Use dir /b to find matching dirs
          expanded = execSync(
            `cmd /c "dir /b "${javaBin.replace(/\//g, "\\")}" 2>nul"`,
            { encoding: "utf-8", timeout: 5000 }
          ).trim();
        } else {
          expanded = execSync(`echo ${javaBin}`, {
            encoding: "utf-8",
            shell: "/bin/bash",
            timeout: 5000,
          }).trim();
        }
        if (expanded) {
          for (const p of expanded.split(/\s+/)) {
            const result = probeJava(p);
            if (result) return result;
          }
        }
      } else {
        const result = probeJava(javaBin);
        if (result) return result;
      }
    } catch {
      // Not found, continue
    }
  }

  // 4. Try system PATH
  try {
    const result = probeJava("java");
    if (result) return result;
  } catch {
    // not on PATH
  }

  return null;
}

// ─── Adoptium JDK 8 Download ───────────────────────────────────────────────────

/**
 * Query the Adoptium API for the latest JDK 8 download URL for the current platform.
 */
async function getAdoptiumJdk8Url() {
  const plat = getPlatform();
  const arch = getArch();

  const osMap = { windows: "windows", mac: "mac", linux: "linux" };
  const archMap = { x64: "x64", x32: "x86" };

  // Image type: jdk (full JDK, not just JRE)
  const apiUrl = new URL("https://api.adoptium.net/v3/assets/latest/8/hotspot");
  apiUrl.searchParams.set("os", osMap[plat]);
  apiUrl.searchParams.set("arch", archMap[arch]);
  apiUrl.searchParams.set("image_type", "jdk");
  apiUrl.searchParams.set("vendor", "eclipse");

  info(`Querying Adoptium API for JDK 8 (${osMap[plat]}/${archMap[arch]})...`);

  return new Promise((resolve, reject) => {
    https.get(apiUrl.toString(), (res) => {
      let data = "";
      res.on("data", (chunk) => { data += chunk; });
      res.on("end", () => {
        try {
          const json = JSON.parse(data);
          if (!Array.isArray(json) || json.length === 0) {
            reject(new Error("No JDK 8 assets found from Adoptium API"));
            return;
          }
          const asset = json[0];
          const pkg = asset.binary?.package;
          if (!pkg?.link) {
            reject(new Error("No download link found in Adoptium response"));
            return;
          }
          resolve({
            downloadUrl: pkg.link,
            fileName: pkg.name,
            size: pkg.size,
            version: asset.version?.semver || "8.0.0",
          });
        } catch (e) {
          reject(new Error(`Failed to parse Adoptium API response: ${e.message}`));
        }
      });
      res.on("error", reject);
    }).on("error", reject);
  });
}

/**
 * Extract a .tar.gz archive.
 */
async function extractTarGz(archivePath, destDir) {
  const { createReadStream: crs } = require("fs");
  // Use system tar for reliability
  const tarBin = getPlatform() === "windows" ? undefined : "tar";

  if (tarBin) {
    return new Promise((resolve, reject) => {
      mkdirSync(destDir, { recursive: true });
      const child = spawn("tar", ["-xzf", archivePath, "-C", destDir, "--strip-components=1"], {
        stdio: ["inherit", "inherit", "inherit"],
      });
      child.on("close", (code) => {
        if (code === 0) resolve();
        else reject(new Error(`tar exited with code ${code}`));
      });
      child.on("error", reject);
    });
  } else {
    // Fallback: use Node.js built-in for .zip on Windows
    warn("No system tar found, using Node.js extraction...");
    await extractZip(archivePath, destDir);
  }
}

/**
 * Extract a .zip archive using PowerShell on Windows.
 */
async function extractZip(archivePath, destDir) {
  return new Promise((resolve, reject) => {
    mkdirSync(destDir, { recursive: true });
    // Use PowerShell's Expand-Archive
    const psCmd = `Expand-Archive -Path '${archivePath}' -DestinationPath '${destDir}' -Force`;
    const child = spawn("powershell", ["-NoProfile", "-Command", psCmd], {
      stdio: ["inherit", "inherit", "inherit"],
    });
    child.on("close", (code) => {
      if (code === 0) resolve();
      else reject(new Error(`PowerShell Expand-Archive exited with code ${code}`));
    });
    child.on("error", reject);
  });
}

/**
 * Download and extract a portable JDK 8 to the cache directory.
 */
async function downloadPortableJdk8() {
  const cacheDir = join(os.homedir(), ".forge-build", "jdk8");
  const markerFile = join(cacheDir, ".jdk8-ready");

  // Check if we already have a cached JDK 8
  if (existsSync(markerFile)) {
    info("Found cached portable JDK 8");
    const javaBin = getPlatform() === "windows"
      ? join(cacheDir, "bin", "java.exe")
      : join(cacheDir, "bin", "java");
    const result = probeJava(javaBin);
    if (result) {
      success(`Using cached JDK 8: ${result.version}`);
      return cacheDir;
    } else {
      warn("Cached JDK 8 is corrupted, re-downloading...");
      rmSync(cacheDir, { recursive: true, force: true });
    }
  }

  info("Downloading portable JDK 8 from Adoptium...");
  const jdkInfo = await getAdoptiumJdk8Url();
  info(`JDK version: ${jdkInfo.version} (${formatBytes(jdkInfo.size || 0)})`);

  // Download to temp location
  const tmpDir = join(os.tmpdir(), `forge-build-jdk8-${Date.now()}`);
  mkdirSync(tmpDir, { recursive: true });
  const archivePath = join(tmpDir, jdkInfo.fileName);

  await downloadFile(jdkInfo.downloadUrl, archivePath);

  // Extract
  mkdirSync(cacheDir, { recursive: true });
  info("Extracting JDK 8...");

  if (archivePath.endsWith(".tar.gz")) {
    await extractTarGz(archivePath, cacheDir);
  } else if (archivePath.endsWith(".zip")) {
    await extractZip(archivePath, cacheDir);
  } else {
    throw new Error(`Unsupported archive format: ${archivePath}`);
  }

  // Verify
  const javaBin = getPlatform() === "windows"
    ? join(cacheDir, "bin", "java.exe")
    : join(cacheDir, "bin", "java");

  const result = probeJava(javaBin);
  if (!result) {
    throw new Error("Downloaded JDK does not appear to be valid JDK 8");
  }

  success(`Portable JDK 8 installed: ${result.version}`);

  // Write marker
  require("fs").writeFileSync(markerFile, JSON.stringify({ version: result.version, path: cacheDir, date: new Date().toISOString() }));

  // Cleanup archive
  try { await unlink(archivePath); } catch { /* ignore */ }

  return cacheDir;
}

/**
 * Ensure JDK 8 is available. Returns the JAVA_HOME path to use.
 */
async function ensureJdk8(skipCheck = false) {
  if (skipCheck) {
    if (process.env.JAVA_HOME) return process.env.JAVA_HOME;
    warn("--no-jdk-check specified but JAVA_HOME is not set; build may fail.");
    return undefined;
  }

  // 1. Try to find system JDK 8
  const systemJdk = findSystemJdk8();
  if (systemJdk) {
    success(`Found system JDK 8: ${systemJdk.version} at ${systemJdk.path}`);
    return dirname(dirname(systemJdk.path)); // JAVA_HOME = parent of bin/
  }

  warn("No JDK 8 found on the system.");

  // 2. Download portable JDK 8
  const portableHome = await downloadPortableJdk8();
  return portableHome;
}

// ─── Forge MDK Setup ───────────────────────────────────────────────────────────

/**
 * Download Forge 1.8 MDK from Maven Central / Forge files.
 */
async function downloadForgeMdk(forgeVersion, targetDir) {
  info(`Downloading Forge ${forgeVersion} MDK...`);

  // Forge MDK is hosted on Maven Central and also on files.minecraftforge.net
  // The MDK zip for 1.8.9-11.15.1.2318-1.8.9:
  const mavenUrl = `https://maven.minecraftforge.net/net/minecraftforge/forge/${forgeVersion}/forge-${forgeVersion}-mdk.zip`;

  const tmpDir = join(os.tmpdir(), `forge-mdk-${Date.now()}`);
  mkdirSync(tmpDir, { recursive: true });
  const archivePath = join(tmpDir, `forge-${forgeVersion}-mdk.zip`);

  try {
    await downloadFile(mavenUrl, archivePath);
  } catch (err) {
    // Fallback to alternate URL
    warn(`Maven Central download failed: ${err.message}`);
    info("Trying alternative download source...");
    const fallbackUrl = `https://files.minecraftforge.net/maven/net/minecraftforge/forge/${forgeVersion}/forge-${forgeVersion}-mdk.zip`;
    await downloadFile(fallbackUrl, archivePath);
  }

  info("Extracting Forge MDK...");
  mkdirSync(targetDir, { recursive: true });

  const plat = getPlatform();
  if (plat === "windows") {
    await extractZip(archivePath, targetDir);
  } else {
    // The MDK zip usually contains a root folder; extract to tmp then move contents
    const extractTmp = join(tmpDir, "extracted");
    await new Promise((resolve, reject) => {
      mkdirSync(extractTmp, { recursive: true });
      const child = spawn("unzip", ["-q", archivePath, "-d", extractTmp], {
        stdio: ["inherit", "inherit", "inherit"],
      });
      child.on("close", (code) => {
        if (code === 0) resolve();
        else reject(new Error(`unzip exited with code ${code}`));
      });
      child.on("error", reject);
    });

    // The MDK zip may have a root dir; move contents up
    const entries = require("fs").readdirSync(extractTmp);
    if (entries.length === 1 && statSync(join(extractTmp, entries[0])).isDirectory()) {
      const innerDir = join(extractTmp, entries[0]);
      for (const entry of require("fs").readdirSync(innerDir)) {
        cpSync(join(innerDir, entry), join(targetDir, entry), { recursive: true });
      }
    } else {
      for (const entry of entries) {
        cpSync(join(extractTmp, entry), join(targetDir, entry), { recursive: true });
      }
    }
  }

  // Cleanup
  try { rmSync(tmpDir, { recursive: true, force: true }); } catch { /* ignore */ }

  success(`Forge MDK extracted to ${targetDir}`);
  return targetDir;
}

/**
 * Verify that a directory looks like a valid Forge MDK.
 */
function verifyMdkDir(dir) {
  const required = ["build.gradle", "gradlew", "gradlew.bat"];
  const plat = getPlatform();
  const checks = plat === "windows"
    ? ["build.gradle", "gradlew.bat"]
    : ["build.gradle", "gradlew"];

  for (const file of checks) {
    if (!existsSync(join(dir, file))) {
      return false;
    }
  }
  return true;
}

// ─── Gradle Build ──────────────────────────────────────────────────────────────

/**
 * Run a Gradle command in the given working directory with the specified JAVA_HOME.
 */
function runGradle(workingDir, javaHome, args = []) {
  return new Promise((resolve, reject) => {
    const isWindows = getPlatform() === "windows";
    const gradlew = isWindows ? "gradlew.bat" : "./gradlew";

    // Make gradlew executable on Unix
    if (!isWindows) {
      try { chmodSync(join(workingDir, "gradlew"), 0o755); } catch { /* ignore */ }
    }

    const env = { ...process.env };
    if (javaHome) {
      env.JAVA_HOME = javaHome;
      // Prepend JDK bin to PATH for maximum compatibility
      env.PATH = join(javaHome, "bin") + (isWindows ? ";" : ":") + (env.PATH || "");
    }

    const child = spawn(gradlew, args, {
      cwd: workingDir,
      env,
      stdio: ["inherit", "inherit", "inherit"],
      shell: isWindows,
    });

    child.on("close", (code) => {
      if (code === 0) {
        resolve(code);
      } else {
        reject(new Error(`Gradle exited with code ${code}`));
      }
    });

    child.on("error", (err) => {
      reject(new Error(`Failed to start Gradle: ${err.message}`));
    });
  });
}

/** Synchronous chmod for gradlew */
function chmodSync(path, mode) {
  require("fs").chmodSync(path, mode);
}

// ─── Main ──────────────────────────────────────────────────────────────────────

async function main() {
  const opts = parseArgs();
  const TOTAL_STEPS = 5;

  console.log();
  console.log(`${C.bold}${C.magenta}  ╔══════════════════════════════════════════╗${C.reset}`);
  console.log(`${C.bold}${C.magenta}  ║   Minecraft Forge 1.8 Mod Builder      ║${C.reset}`);
  console.log(`${C.bold}${C.magenta}  ║   Node.js Edition                       ║${C.reset}`);
  console.log(`${C.bold}${C.magenta}  ╚══════════════════════════════════════════╝${C.reset}`);
  console.log();

  const forgeVersion = opts.forgeVersion || "1.8.9-11.15.1.2318-1.8.9";
  const startTime = Date.now();

  try {
    // ── Step 1: Ensure JDK 8 ────────────────────────────────────────────
    step(1, TOTAL_STEPS, "Ensure JDK 8 is available");
    const javaHome = await ensureJdk8(opts.skipJdkCheck);
    console.log();

    // ── Step 2: Set up / verify MDK directory ───────────────────────────
    step(2, TOTAL_STEPS, "Set up Forge MDK");
    let mdkDir;

    if (opts.mdkPath) {
      mdkDir = resolve(opts.mdkPath);
      if (!existsSync(mdkDir)) {
        error(`Specified MDK path does not exist: ${mdkDir}`);
        process.exit(1);
      }
      if (!verifyMdkDir(mdkDir)) {
        error(`Directory does not appear to be a valid Forge MDK: ${mdkDir}`);
        error("Expected files: build.gradle, gradlew[.bat]");
        process.exit(1);
      }
      success(`Using existing MDK at: ${mdkDir}`);
    } else {
      // Download MDK into ./forge-mdk
      mdkDir = resolve("./forge-mdk");
      if (existsSync(mdkDir) && verifyMdkDir(mdkDir)) {
        success(`Using existing MDK at: ${mdkDir}`);
      } else {
        if (existsSync(mdkDir)) {
          warn(`Removing incomplete MDK directory: ${mdkDir}`);
          rmSync(mdkDir, { recursive: true, force: true });
        }
        await downloadForgeMdk(forgeVersion, mdkDir);
      }
    }
    console.log();

    // ── Step 3: Clean build (optional) ──────────────────────────────────
    if (opts.cleanBuild) {
      step(3, TOTAL_STEPS, "Clean previous build artifacts");
      const buildDir = join(mdkDir, "build");
      const gradleDir = join(mdkDir, ".gradle");
      if (existsSync(buildDir)) { rmSync(buildDir, { recursive: true, force: true }); }
      if (existsSync(gradleDir)) { rmSync(gradleDir, { recursive: true, force: true }); }
      success("Build artifacts cleaned.");
      console.log();
    } else {
      step(3, TOTAL_STEPS, "Skipping clean (use --clean to reset)");
      console.log();
    }

    // ── Step 4: Setup Decomp Workspace ───────────────────────────────────
    step(4, TOTAL_STEPS, `Run Gradle setupDecompWorkspace (Forge ${forgeVersion})`);
    info("This may take a while on the first run (downloading Minecraft sources)...");

    try {
      await runGradle(mdkDir, javaHome, ["setupDecompWorkspace"]);
      success("Decomp workspace setup complete.");
    } catch (err) {
      error(`setupDecompWorkspace failed: ${err.message}`);
      error("This is often caused by network issues or Gradle version incompatibilities.");
      error("Ensure you have internet access and try again.");
      process.exit(1);
    }
    console.log();

    // ── Step 5: Build the mod ────────────────────────────────────────────
    step(5, TOTAL_STEPS, "Build the mod");

    try {
      await runGradle(mdkDir, javaHome, ["build"]);
      success("Build successful!");
    } catch (err) {
      error(`Build failed: ${err.message}`);
      process.exit(1);
    }
    console.log();

    // ── Collect output ───────────────────────────────────────────────────
    const buildLibs = join(mdkDir, "build", "libs");
    if (existsSync(buildLibs)) {
      const jars = require("fs").readdirSync(buildLibs).filter(f => f.endsWith(".jar"));
      if (jars.length > 0) {
        console.log(`${C.bold}${C.green}  ┌──────────────────────────────────────────┐${C.reset}`);
        console.log(`${C.bold}${C.green}  │  BUILD SUCCESSFUL                          │${C.reset}`);
        console.log(`${C.bold}${C.green}  ├──────────────────────────────────────────┤${C.reset}`);
        for (const jar of jars) {
          const jarPath = join(buildLibs, jar);
          const size = statSync(jarPath).size;
          console.log(`${C.green}  │  ${jar.padEnd(38)}${formatBytes(size).padStart(8)}  │${C.reset}`);
        }
        console.log(`${C.bold}${C.green}  └──────────────────────────────────────────┘${C.reset}`);

        // Copy to output directory if specified
        if (opts.outputPath) {
          const outDir = resolve(opts.outputPath);
          mkdirSync(outDir, { recursive: true });
          for (const jar of jars) {
            cpSync(join(buildLibs, jar), join(outDir, jar));
          }
          success(`Output jars copied to: ${outDir}`);
        }
      } else {
        warn("Build completed but no jars found in build/libs/");
      }
    }

    const elapsed = ((Date.now() - startTime) / 1000).toFixed(1);
    console.log();
    success(`Done in ${elapsed}s`);
    console.log();

  } catch (err) {
    console.log();
    error(`Fatal error: ${err.message}`);
    error(err.stack);
    process.exit(1);
  }
}

main();
