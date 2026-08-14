#!/bin/bash
set -e

JAVA=/home/z/.forge-build/jdk8/bin/java
JAVAC=/home/z/.forge-build/jdk8/bin/javac
JAR_TOOL=/home/z/.forge-build/jdk8/bin/jar

SPECIAL_SOURCE=/home/z/.forge-build/SpecialSource-1.7.3.jar
MCP_TO_SRG=/home/z/.gradle/caches/minecraft/de/oceanlabs/mcp/mcp_stable/22/srgs/mcp-srg.srg
FORGE_SRC=/home/z/.gradle/caches/minecraft/net/minecraftforge/forge/1.8.9-11.15.1.2318-1.8.9/stable/22/forgeSrc-1.8.9-11.15.1.2318-1.8.9.jar

# Build classpath from all cached jars
CP=""
while IFS= read -r jar; do
  if echo "$jar" | grep -qv 'natives\|sources\|javadoc'; then
    CP="$CP:$jar"
  fi
done < <(find /home/z/.gradle/caches/modules-2 -name '*.jar' -not -name '*natives*' -not -name '*sources*' -not -name '*javadoc*' 2>/dev/null | sort)
CP="$FORGE_SRC$CP"

SRC=/home/z/my-project/heat-client/src/main/java
RES=/home/z/my-project/heat-client/src/main/resources
OUT=/home/z/my-project/heat-client/build-classes
DIST=/home/z/my-project/heat-client/dist
MOD_JAR=$DIST/HeatClient-1.5.3.jar

echo "=== Step 1: Compile ==="
rm -rf "$OUT" "$DIST"
mkdir -p "$OUT" "$DIST"

$JAVAC -source 1.8 -target 1.8 \
  -cp "$CP" \
  -d "$OUT" \
  $(find "$SRC" -name '*.java')

echo "=== Step 2: Reobfuscate MCP -> SRG ==="
REOBF_CP="$SPECIAL_SOURCE:$(find /home/z/.gradle/caches/modules-2 -name 'jopt-simple-4.6.jar' | head -1)"$(find /home/z/.gradle/caches/modules-2 -name '*.jar' -not -name '*natives*' -not -name '*sources*' -not -name '*javadoc*' 2>/dev/null | sort | while read jar; do echo ":$jar"; done | tr -d '\n')

TMP_REOBF=$(mktemp -d)
$JAR_TOOL cf "$TMP_REOBF/input.jar" -C "$OUT" .
$JAVA -cp "$REOBF_CP" \
  net.md_5.specialsource.SpecialSource \
  --in-jar "$TMP_REOBF/input.jar" \
  --out-jar "$TMP_REOBF/output.jar" \
  --srg-in "$MCP_TO_SRG"
rm -rf "$OUT"/*
cd "$OUT" && $JAR_TOOL xf "$TMP_REOBF/output.jar"
rm -rf "$TMP_REOBF"

echo "=== Step 3: Package ==="
cp "$RES/mcmod.info" "$OUT/mcmod.info"
sed -i 's/\${version}/1.5.3/g; s/\${mcversion}/1.8.9-11.15.1.2318-1.8.9/g' "$OUT/mcmod.info"

echo "Manifest-Version: 1.0" > /tmp/manifest.mf
echo "Created-By: 1.8.0 (Heat Client Build)" >> /tmp/manifest.mf

cd "$OUT" && $JAR_TOOL cfm "$MOD_JAR" /tmp/manifest.mf .

rm -f /tmp/manifest.mf

SIZE=$(stat -c%s "$MOD_JAR")
echo ""
echo "BUILD SUCCESS!"
echo "  Output: $MOD_JAR"
echo "  Size:   $SIZE bytes"
