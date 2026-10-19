"""Verify the signed application exposes its real main screen after installation."""
import re
import sys
import xml.etree.ElementTree as ET

nodes = list(ET.parse(sys.argv[1]).getroot().iter("node"))
assert any(node.get("content-desc") == "Ajustes" for node in nodes), "Settings control missing"
assert any(node.get("text") in ("TRABAJO", "DESCANSO") for node in nodes), "Phase label missing"
assert any(re.fullmatch(r"\d{2}:\d{2}", node.get("text", "")) for node in nodes), "Countdown missing"
assert any(node.get("package") == "com.jstihl01.autopomodoro.mobile" for node in nodes), "Wrong app is visible"
print("Signed APK installed and launched with a visible Pomodoro screen")
