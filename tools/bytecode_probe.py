#!/usr/bin/env python3
"""bytecode_probe.py <jar> <class> <regex> <context-before>

Runs `javap -c -p` on one class and prints, for every method whose bytecode lines match
<regex>, the method header once followed by each matching line with the requested number of
preceding lines. Used by CI diagnostics to see how one Minecraft method calls another."""
import re, subprocess, sys

jar, cls, rx, ctx = sys.argv[1], sys.argv[2], sys.argv[3], int(sys.argv[4] or 0)
out = subprocess.run(["javap", "-c", "-p", "-cp", jar, cls], capture_output=True, text=True).stdout
header = re.compile(r"^  [A-Za-z<].*\).*;$")
rx = re.compile(rx)
method, lines, cur = None, {}, []
for ln in out.splitlines():
    if header.match(ln):
        method = ln.strip()
        lines.setdefault(method, [])
        cur = []
        continue
    if method is None:
        continue
    cur.append(ln)
    if rx.search(ln):
        lines[method].append("\n".join(x[:230] for x in cur[-(ctx + 1):]) + "\n  --")
for m, hits in lines.items():
    if hits:
        print("METHOD", m[:230])
        print("\n".join(hits))
