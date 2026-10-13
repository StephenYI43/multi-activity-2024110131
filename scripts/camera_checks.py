import json
import time
from device_checks import adb, dump, click, rotate, screenshot, find_id, OUT

def visible_text():
    return "\n".join(n.get("text", "") for n in dump().iter("node"))

auto = adb("shell", "settings", "get", "system", "accelerometer_rotation")
original = adb("shell", "settings", "get", "system", "user_rotation")
try:
    assert "拍摄成功" in visible_text()
    adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
    rotate(1)
    find_id("take_photo")
    text = visible_text()
    assert "拍摄成功" in text and "1392 × 1856" in text, text
    screenshot("07-photo-landscape.png")
    rotate(0)
    click("take_photo")
    adb("shell", "input", "keyevent", "4")
    time.sleep(.6)
    text = visible_text()
    assert "已取消拍摄" in text and "1392 × 1856" in text, text
    screenshot("07b-photo-cancel.png")
    data = json.loads((OUT / "device-checks.json").read_text(encoding="utf-8"))
    data["implicit_camera_launch"] = "PASS"
    data["full_photo_return_rotation_and_cancel"] = {"result": "PASS", "width": 1392, "height": 1856, "size_kb": 73}
    (OUT / "device-checks.json").write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(data, ensure_ascii=False, indent=2))
finally:
    adb("shell", "settings", "put", "system", "user_rotation", original)
    adb("shell", "settings", "put", "system", "accelerometer_rotation", auto)
