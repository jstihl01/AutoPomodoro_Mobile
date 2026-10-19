"""Verify the signed application exposes its real main screen after installation."""
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

nodes = list(ET.parse(sys.argv[1]).getroot().iter("node"))
if any(node.get("resource-id") == "android:id/immersive_cling_title" for node in nodes):
    button = next(node for node in nodes if node.get("resource-id") == "android:id/ok")
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", button.get("bounds")))
    subprocess.run(["adb", "shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2)], check=True)
    time.sleep(1)
    subprocess.run(["adb", "shell", "uiautomator", "dump", "/sdcard/production.xml"], check=True)
    subprocess.run(["adb", "pull", "/sdcard/production.xml", sys.argv[1]], check=True)
    nodes = list(ET.parse(sys.argv[1]).getroot().iter("node"))
assert any(node.get("content-desc") == "Ajustes" for node in nodes), "Settings control missing"
assert any(node.get("text") in ("TRABAJO", "DESCANSO") for node in nodes), "Phase label missing"
assert any(re.fullmatch(r"\d{2}:\d{2}", node.get("text", "")) for node in nodes), "Countdown missing"
assert any(node.get("package") == "com.jstihl01.autopomodoro.mobile" for node in nodes), "Wrong app is visible"
print("Signed APK installed and launched with a visible Pomodoro screen")
