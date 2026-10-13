"""ADB end-to-end checks against the real emulator; preserves rotation settings."""
import json
import re
import subprocess
import time
import xml.etree.ElementTree as ET
from pathlib import Path

ADB = r"C:\Users\48604\AppData\Local\Android\Sdk\platform-tools\adb.exe"
PACKAGE = "com.songjunyi.multiactivity"
OUT = Path(__file__).resolve().parents[1] / "report-assets"

def adb(*args, binary=False):
    result = subprocess.run([ADB, *args], check=True, capture_output=True, timeout=30)
    return result.stdout if binary else result.stdout.decode("utf-8", errors="replace").strip()

def dump():
    result = adb("shell", "uiautomator", "dump", "/sdcard/lab3-ui.xml")
    if "ERROR" in result:
        raise AssertionError(result)
    text = adb("shell", "cat", "/sdcard/lab3-ui.xml")
    return ET.fromstring(text[text.index("<?xml"):])

def find_id(name):
    for _ in range(5):
        root = dump()
        for n in root.iter("node"):
            if n.get("resource-id") == PACKAGE + ":id/" + name:
                return n
        top = next(root.iter("node"))
        _, _, width, height = map(int, re.findall(r"\d+", top.get("bounds")))
        adb("shell", "input", "swipe", str(width//2), str(height*4//5),
            str(width//2), str(height//4), "350")
        time.sleep(.3)
    raise AssertionError("Missing view: " + name)

def click(name):
    n = find_id(name)
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", n.get("bounds")))
    adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
    time.sleep(.45)

def screenshot(name):
    OUT.mkdir(exist_ok=True)
    (OUT / name).write_bytes(adb("exec-out", "screencap", "-p", binary=True))

def rotate(value):
    adb("shell", "settings", "put", "system", "user_rotation", str(value))
    time.sleep(1.3)

def timer():
    value = find_id("timer_value").get("text")
    h, m, s, cs = map(int, re.split("[:.]", value))
    return ((h*60+m)*60+s)*1000+cs*10, value

def main():
    auto = adb("shell", "settings", "get", "system", "accelerometer_rotation")
    rotation = adb("shell", "settings", "get", "system", "user_rotation")
    checks = {}
    try:
        adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
        rotate(0)
        adb("shell", "am", "force-stop", PACKAGE)
        adb("shell", "am", "start", "-n", PACKAGE+"/.MainActivity")
        time.sleep(1)
        screenshot("01-main.png")
        click("open_receive")
        message = find_id("received_message").get("text")
        assert message == "你好，我是宋珺熠，学号2024110131。", message
        screenshot("02-forward.png")
        rotate(1)
        assert find_id("received_message").get("text") == message
        checks["forward_parameter_and_rotation"] = "PASS"
        rotate(0)
        adb("shell", "input", "keyevent", "4")
        click("open_result")
        click("submit_result")
        assert find_id("result_input") is not None
        checks["blank_result_validation"] = "PASS"
        click("result_input")
        adb("shell", "input", "text", "Activity%stest%spassed%s2024110131")
        adb("shell", "input", "keyevent", "4")
        screenshot("03-result-entry.png")
        rotate(1)
        value = find_id("result_input").get("text")
        assert "Activity" in value and "2024110131" in value, value
        rotate(0)
        click("submit_result")
        returned = find_id("returned_result").get("text")
        assert "Activity" in returned and "2024110131" in returned
        screenshot("04-returned.png")
        rotate(1)
        assert find_id("returned_result").get("text") == returned
        checks["result_return_and_rotation"] = "PASS"
        rotate(0)
        click("open_result")
        adb("shell", "input", "keyevent", "4")
        assert find_id("returned_result").get("text") == returned
        checks["cancel_preserves_previous_result"] = "PASS"
        click("open_stopwatch")
        click("timer_toggle")
        time.sleep(1.5)
        screenshot("08-timer-portrait.png")
        adb("shell", "input", "keyevent", "62")
        before, _ = timer()
        adb("shell", "input", "keyevent", "62")
        rotate(1)
        screenshot("09-timer-landscape.png")
        adb("shell", "input", "keyevent", "62")
        after, _ = timer()
        assert after > before, (before, after)
        adb("shell", "input", "keyevent", "62")
        rotate(0)
        adb("shell", "input", "keyevent", "62")
        restored, _ = timer()
        assert restored > after, (after, restored)
        click("timer_toggle")
        adb("shell", "input", "keyevent", "62")
        paused, text = timer()
        time.sleep(1.3)
        assert timer()[0] == paused
        rotate(1)
        assert timer()[0] == paused
        screenshot("10-timer-paused.png")
        rotate(0)
        click("timer_toggle")
        time.sleep(.8)
        adb("shell", "input", "keyevent", "62")
        assert timer()[0] > paused
        click("timer_reset")
        assert timer()[0] == 0
        screenshot("11-timer-reset.png")
        checks["stopwatch_running_rotation"] = {"before_ms": before, "landscape_ms": after, "restored_ms": restored}
        checks["stopwatch_pause_resume_reset"] = "PASS"
        adb("shell", "input", "keyevent", "4")
        click("open_camera")
        screenshot("05-camera-ready.png")
        click("take_photo")
        print("CAMERA_UI", ET.tostring(dump(), encoding="unicode"), flush=True)
        checks["implicit_camera_launch"] = "launched; capture validation follows"
    finally:
        adb("shell", "settings", "put", "system", "user_rotation", rotation)
        adb("shell", "settings", "put", "system", "accelerometer_rotation", auto)
        (OUT / "device-checks.json").write_text(json.dumps(checks, ensure_ascii=False, indent=2), encoding="utf-8")
        (OUT / "lifecycle-log.txt").write_text(adb("logcat", "-d", "-s", "ActivityLifecycle:D", "*:S"), encoding="utf-8")
        print(json.dumps(checks, ensure_ascii=False, indent=2), flush=True)

if __name__ == "__main__":
    main()
