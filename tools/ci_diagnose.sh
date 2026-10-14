#!/usr/bin/env bash
# Run by CI after a failed build. Collects the full build log, the compile
# classpath, a class listing of the Minecraft jar and javap dumps of the classes
# named in tools/ci_probe.txt, then force-pushes them to the `ci-logs` branch so
# they can be read without downloading Actions logs.
set +e
out=$(mktemp -d)
cp build.log "$out/build.log" 2>/dev/null

(cd mod && ./gradlew dependencies --configuration compileClasspath --no-daemon > "$out/deps.log" 2>&1)

# Find the Minecraft jar (the one that holds net/minecraft/client/Minecraft.class).
MC=""
while IFS= read -r j; do
  if unzip -l "$j" 2>/dev/null | grep -q 'net/minecraft/client/Minecraft.class'; then MC="$j"; break; fi
done < <(find "$HOME/.gradle" "$GITHUB_WORKSPACE" -name '*.jar' -size +5M 2>/dev/null)
echo "MC jar: $MC" > "$out/mc_jar.txt"

if [ -n "$MC" ]; then
  unzip -l "$MC" | awk '{print $4}' | grep '\.class$' | grep -E '^(net/minecraft/client|com/mojang/blaze3d)' | sort > "$out/classes.txt"
  : > "$out/javap.txt"
  while IFS= read -r cls; do
    [ -z "$cls" ] && continue
    case "$cls" in \#*) continue;; esac
    echo "===== $cls" >> "$out/javap.txt"
    javap -p -cp "$MC" "$cls" >> "$out/javap.txt" 2>&1
  done < tools/ci_probe.txt
fi

# Where is GLFW (lwjgl) coming from, if anywhere?
find "$HOME/.gradle" -name 'lwjgl*glfw*.jar' 2>/dev/null > "$out/glfw_jars.txt"

cd "$out" || exit 0
git init -q -b ci-logs
git config user.name "ci"
git config user.email "ci@users.noreply.github.com"
git add -A
git commit -q -m "ci diagnostics for ${GITHUB_SHA}"
git remote add origin "https://x-access-token:${GH_TOKEN}@github.com/${GITHUB_REPOSITORY}.git"
git push -q -f origin ci-logs
exit 0
