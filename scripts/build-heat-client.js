#!/usr/bin/env node
"use strict";

/**
 * Heat Client Build Script
 * ========================
 * Automatically provisions JDK 8 + Gradle 2.14, then builds the
 * Heat Client Forge 1.8.9 mod.
 *
 * Usage:  node build-heat-client.js [--skip-jdk] [--skip-gradle] [--clean]
 */

const { spawn, execSync, execFileSync } = require("child_process");
const {
  createWriteStream, existsSync, mkdirSync, rmSync, statSync,
  cpSync, readdirSync, writeFileSync
} = require("fs");
const { join, resolve, dirname, basename } = require("path");
const https = require("https");
const http  = require("http");
const os    = require("os");
const zlib  = require("zlib");
const url   = require("url");

// ─── Config ──────────────────────────────────────────────────────────────────

const PROJECT_DIR   = resolve(__dirname, "..", "heat-client");
const GRADLE_VER    = "4.10.3";
const GRADLE_URL    = `https://services.gradle.org/distributions/gradle-${GRADLE_VER}-bin.zip`;
const GRADLE_CACHE  = join(os.homedir(), ".forge-build", `gradle-${GRADLE_VER}`);
const JDK_CACHE     = join(os.homedir(), ".forge-build", "jdk8");

// ─── ANSI helpers ────────────────────────────────────────────────────────────

const C = {
  reset: "\x1b[0m", bold: "\x1b[1m",
  red: "\x1b[31m", green: "\x1b[32m", yellow: "\x1b[33m",
  blue: "\x1b[34m", magenta: "\x1b[35m", cyan: "\x1b[36m", gray: "\x1b[90m",
};

const log     = (m, c = C.reset) => console.log(`${C.gray}[build]${C.reset} ${c}${m}${C.reset}`);
const success = m => log(m, C.green);
const warn    = m => log(m, C.yellow);
const error   = m => log(m, C.red);
const info    = m => log(m, C.cyan);
const step    = (n, t, m) => log(`${C.bold}Step ${n}/${t}:${C.reset} ${m}`, C.blue);

// ─── Platform ────────────────────────────────────────────────────────────────

const plat = process.platform === "win32" ? "windows"
         : process.platform === "darwin" ? "mac" : "linux";
const isWin = plat === "windows";

// ─── Parse CLI flags ─────────────────────────────────────────────────────────

const argv = process.argv.slice(2);
const OPTS = {
  skipJdk:    argv.includes("--skip-jdk"),
  skipGradle: argv.includes("--skip-gradle"),
  clean:      argv.includes("--clean"),
};

// ─── HTTP download with progress ─────────────────────────────────────────────

function download(fileUrl, dest) {
  return new Promise((res, rej) => {
    const parsed = new URL(fileUrl);
    const client = parsed.protocol === "https:" ? https : http;

    client.get(fileUrl, (r) => {
      if (r.statusCode >= 300 && r.statusCode < 400 && r.headers.location) {
        return download(r.headers.location, dest).then(res, rej);
      }
      if (r.statusCode !== 200) {
        rej(new Error(`HTTP ${r.statusCode} from ${fileUrl}`));
        return;
      }
      const total = parseInt(r.headers["content-length"], 10) || 0;
      let done = 0, lastPct = -1;
      const ws = createWriteStream(dest);
      r.on("data", (chunk) => {
        done += chunk.length;
        if (total > 0) {
          const pct = Math.floor((done / total) * 100);
          if (pct !== lastPct && pct % 5 === 0) {
            process.stdout.write(
              `\r  ${C.cyan}Downloading...${C.reset} ${pct}% (${fmtBytes(done)} / ${fmtBytes(total)})`
            );
            lastPct = pct;
          }
        }
      });
      r.pipe(ws);
      ws.on("finish", () => { console.log(); res(dest); });
      ws.on("error", rej);
      r.on("error", rej);
    }).on("error", rej);
  });
}

function fmtBytes(b) {
  if (b < 1024) return b + " B";
  if (b < 1048576) return (b / 1024).toFixed(1) + " KB";
  return (b / 1048576).toFixed(1) + " MB";
}

// ─── Extract .zip (cross-platform) ───────────────────────────────────────────

function extractZip(archive, dest) {
  return new Promise((res, rej) => {
    mkdirSync(dest, { recursive: true });
    let child;
    if (isWin) {
      child = spawn(
        "powershell", ["-NoProfile", "-Command",
          `Expand-Archive -LiteralPath '${archive}' -DestinationPath '${dest}' -Force`],
        { stdio: ["inherit", "inherit", "inherit"] }
      );
    } else {
      child = spawn("unzip", ["-qo", archive, "-d", dest],
        { stdio: ["inherit", "inherit", "inherit"] }
      );
    }
    child.on("close", code => code === 0 ? res() : rej(new Error(`extract exited ${code}`)));
    child.on("error", rej);
  });
}

// ─── JDK 8 detection & download ──────────────────────────────────────────────

function probeJava(bin) {
  try {
    const out = execSync(`${bin} -version 2>&1`, { encoding: "utf-8", timeout: 5000 });
    if (/(?:openjdk|java) version "1\.8/.test(out) || /version "8\./.test(out)) {
      const m = out.match(/"(1\.8[^"]+)"|"(8\.[^"]+)"/);
      return { version: m ? (m[1] || m[2]) : "1.8.0", path: bin };
    }
  } catch {}
  return null;
}

function findSystemJdk8() {
  info("Searching for JDK 8 on the system...");
  const cands = [];
  if (process.env.JAVA_HOME) {
    const jh = process.env.JAVA_HOME;
    cands.push(join(jh, "bin", isWin ? "java.exe" : "java"));
  }
  if (plat === "windows") {
    const pf = process.env.ProgramFiles || `C:\Program Files`;
    cands.push(
      join(pf, "Java", "jdk1.8.0_*", "bin", "java.exe"),
      join(pf, "Eclipse Adoptium", "jdk-8*", "bin", "java.exe"),
      join(pf, "AdoptOpenJDK", "jdk-8*", "bin", "java.exe")
    );
  } else if (plat === "mac") {
    cands.push(
      "/Library/Java/JavaVirtualMachines/jdk1.8*/Contents/Home/bin/java",
      "/Library/Java/JavaVirtualMachines/temurin-8*/Contents/Home/bin/java",
      "/usr/local/opt/openjdk@8/bin/java"
    );
  } else {
    cands.push(
      "/usr/lib/jvm/java-8-openjdk-*/bin/java",
      "/usr/lib/jvm/java-8-oracle/bin/java",
      "/usr/lib/jvm/temurin-8-*/bin/java",
      "/opt/java/jdk-8*/bin/java"
    );
  }
  for (const pattern of cands) {
    try {
      let expanded;
      if (pattern.includes("*")) {
        expanded = execSync(
          isWin ? `cmd /c "dir /b "${pattern.replace(/\//g, "\\")}" 2>nul`
                 : `echo ${pattern}`,
          { encoding: "utf-8", shell: isWin ? undefined : "/bin/bash", timeout: 5000 }
        ).trim();
      } else {
        expanded = pattern;
      }
      for (const p of expanded.split(/\s+/)) {
        const r = probeJava(p);
        if (r) return r;
      }
    } catch {}
  }
  // PATH fallback
  try {
    const r = probeJava("java");
    if (r) return r;
  } catch {}
  return null;
}

async function getAdoptiumJdk8Url() {
  const osMap   = { windows: "windows", mac: "mac", linux: "linux" };
  const archMap = { x64: "x64", x32: "x86", arm64: "aarch64" };
  const apiUrl  = new URL("https://api.adoptium.net/v3/assets/latest/8/hotspot");
  apiUrl.searchParams.set("os", osMap[plat]);
  apiUrl.searchParams.set("arch", archMap[process.arch] || "x64");
  apiUrl.searchParams.set("image_type", "jdk");
  apiUrl.searchParams.set("vendor", "eclipse");

  return new Promise((res, rej) => {
    https.get(apiUrl.toString(), (r) => {
      let d = "";
      r.on("data", c => d += c);
      r.on("end", () => {
        try {
          const j = JSON.parse(d);
          if (!Array.isArray(j) || !j[0]?.binary?.package?.link)
            return rej(new Error("No JDK 8 from Adoptium"));
          res({
            url: j[0].binary.package.link,
            file: j[0].binary.package.name,
            size: j[0].binary.package.size,
            ver: j[0].version?.semver || "8.0.0",
          });
        } catch (e) { rej(e); }
      });
      r.on("error", rej);
    }).on("error", rej);
  });
}

async function ensureJdk8() {
  if (OPTS.skipJdk && process.env.JAVA_HOME) return process.env.JAVA_HOME;

  const sys = findSystemJdk8();
  if (sys) {
    success(`Found system JDK 8: ${sys.version} at ${sys.path}`);
    return dirname(dirname(sys.path));
  }
  warn("No JDK 8 on system — downloading portable JDK 8 from Adoptium...");

  const marker = join(JDK_CACHE, ".jdk8-ready");
  if (existsSync(marker)) {
    const javaBin = join(JDK_CACHE, "bin", isWin ? "java.exe" : "java");
    const r = probeJava(javaBin);
    if (r) { success(`Using cached JDK 8: ${r.version}`); return JDK_CACHE; }
    warn("Cached JDK 8 corrupted, re-downloading...");
    rmSync(JDK_CACHE, { recursive: true, force: true });
  }

  const jdk = await getAdoptiumJdk8Url();
  info(`JDK ${jdk.ver} (${fmtBytes(jdk.size || 0)})`);

  const tmp = join(os.tmpdir(), `forge-jdk8-${Date.now()}`);
  mkdirSync(tmp, { recursive: true });
  const archive = join(tmp, jdk.file);
  await download(jdk.url, archive);

  mkdirSync(JDK_CACHE, { recursive: true });
  info("Extracting JDK 8...");

  if (archive.endsWith(".tar.gz")) {
    await new Promise((res, rej) => {
      const child = spawn("tar", ["-xzf", archive, "-C", JDK_CACHE, "--strip-components=1"],
        { stdio: ["inherit", "inherit", "inherit"] });
      child.on("close", code => code === 0 ? res() : rej(new Error(`tar ${code}`)));
      child.on("error", rej);
    });
  } else {
    await extractZip(archive, JDK_CACHE);
  }

  const javaBin = join(JDK_CACHE, "bin", isWin ? "java.exe" : "java");
  const r = probeJava(javaBin);
  if (!r) throw new Error("Downloaded JDK is not valid JDK 8");
  success(`Portable JDK 8 installed: ${r.version}`);
  writeFileSync(marker, JSON.stringify({ ver: r.version, date: new Date().toISOString() }));
  try { require("fs/promises").then(fs => fs.unlink(archive)); } catch {}
  return JDK_CACHE;
}

// ─── Gradle 2.14 download ────────────────────────────────────────────────────

async function ensureGradle() {
  if (OPTS.skipGradle) {
    // assume "gradle" is on PATH
    return { bin: isWin ? "gradle.bat" : "gradle", cached: false };
  }

  const marker = join(GRADLE_CACHE, ".gradle-ready");
  const gradleBin = isWin
    ? join(GRADLE_CACHE, "bin", "gradle.bat")
    : join(GRADLE_CACHE, "bin", "gradle");

  if (existsSync(marker) && existsSync(gradleBin)) {
    success(`Using cached Gradle ${GRADLE_VER}`);
    return { bin: gradleBin, cached: true };
  }

  info(`Downloading Gradle ${GRADLE_VER}...`);
  const tmp = join(os.tmpdir(), `forge-gradle-${Date.now()}`);
  mkdirSync(tmp, { recursive: true });
  const archive = join(tmp, `gradle-${GRADLE_VER}-bin.zip`);

  await download(GRADLE_URL, archive);

  info("Extracting Gradle...");
  if (existsSync(GRADLE_CACHE)) rmSync(GRADLE_CACHE, { recursive: true, force: true });
  await extractZip(archive, GRADLE_CACHE);

  // The zip extracts into a subdirectory; move contents up if needed
  const entries = readdirSync(GRADLE_CACHE);
  if (entries.length === 1 && statSync(join(GRADLE_CACHE, entries[0])).isDirectory()) {
    const inner = join(GRADLE_CACHE, entries[0]);
    const innerEntries = readdirSync(inner);
    for (const e of innerEntries) {
      cpSync(join(inner, e), join(GRADLE_CACHE, e), { recursive: true });
    }
    rmSync(inner, { recursive: true, force: true });
  }

  if (!isWin) {
    try { require("fs").chmodSync(gradleBin, 0o755); } catch {}
  }

  writeFileSync(marker, new Date().toISOString());
  success(`Gradle ${GRADLE_VER} installed.`);
  try { require("fs/promises").then(fs => fs.unlink(archive)); } catch {}
  return { bin: gradleBin, cached: true };
}

// ─── Run a command and stream output ─────────────────────────────────────────

function runCmd(bin, args, env, cwd) {
  return new Promise((res, rej) => {
    const child = spawn(bin, args, {
      cwd, env,
      stdio: ["inherit", "inherit", "inherit"],
      shell: isWin,
    });
    child.on("close", code => code === 0 ? res(code) : rej(new Error(`exit code ${code}`)));
    child.on("error", rej);
  });
}

// ─── Main ────────────────────────────────────────────────────────────────────

async function main() {
  const STEPS = 4;
  const t0 = Date.now();

  console.log();
  console.log(`${C.bold}${C.magenta}  \u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557${C.reset}`);
  console.log(`${C.bold}${C.magenta}  \u2551  Heat Client Build Script              \u2551${C.reset}`);
  console.log(`${C.bold}${C.magenta}  \u255a\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255d${C.reset}`);
  console.log();

  if (!existsSync(join(PROJECT_DIR, "build.gradle"))) {
    error(`build.gradle not found at ${PROJECT_DIR}`);
    process.exit(1);
  }

  // ── 1. JDK 8 ────────────────────────────────────────────────────────
  step(1, STEPS, "Ensure JDK 8");
  const javaHome = await ensureJdk8();
  console.log();

  // ── 2. Gradle ───────────────────────────────────────────────────────
  step(2, STEPS, `Ensure Gradle ${OPTS.skipGradle ? "(skipped)" : GRADLE_VER}`);
  const { bin: gradleBin } = await ensureGradle();
  console.log();

  // Build the env
  const env = { ...process.env };
  if (javaHome) {
    env.JAVA_HOME = javaHome;
    env.PATH = join(javaHome, "bin") + (isWin ? ";" : ":") + (env.PATH || "");
  }

  // ── 3. setupDecompWorkspace ──────────────────────────────────────────
  step(3, STEPS, "Gradle setupDecompWorkspace  (first run downloads Minecraft)");
  try {
    await runCmd(gradleBin, ["setupDecompWorkspace", "--no-daemon", "--console=plain"], env, PROJECT_DIR);
    success("Decomp workspace ready.");
  } catch (err) {
    error(`setupDecompWorkspace failed: ${err.message}`);
    process.exit(1);
  }
  console.log();

  // ── 4. build ─────────────────────────────────────────────────────────
  step(4, STEPS, "Gradle build");
  const buildArgs = ["build", "--no-daemon", "--console=plain"];
  if (OPTS.clean) buildArgs.push("clean");
  try {
    await runCmd(gradleBin, buildArgs, env, PROJECT_DIR);
    success("Build successful!");
  } catch (err) {
    error(`Build failed: ${err.message}`);
    process.exit(1);
  }
  console.log();

  // ── Report output jars ───────────────────────────────────────────────
  const libsDir = join(PROJECT_DIR, "build", "libs");
  if (existsSync(libsDir)) {
    const jars = readdirSync(libsDir).filter(f => f.endsWith(".jar"));
    if (jars.length > 0) {
      console.log(`${C.bold}${C.green}  \u250c\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2510${C.reset}`);
      console.log(`${C.bold}${C.green}  \u2502  BUILD OUTPUT                            \u2502${C.reset}`);
      console.log(`${C.bold}${C.green}  \u251c\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2524${C.reset}`);
      for (const jar of jars) {
        const p = join(libsDir, jar);
        const sz = statSync(p).size;
        console.log(`${C.green}  \u2502  ${jar.padEnd(35)}${fmtBytes(sz).padStart(8)}  \u2502${C.reset}`);
      }
      console.log(`${C.bold}${C.green}  \u2514\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2518${C.reset}`);
    }
  }

  console.log();
  success(`Done in ${((Date.now() - t0) / 1000).toFixed(1)}s`);
  console.log();
}

main().catch(e => { error(e.stack || e.message); process.exit(1); });
