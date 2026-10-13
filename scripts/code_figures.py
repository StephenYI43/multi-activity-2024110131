"""Render exact source excerpts with line numbers for the report, without altering code."""
from pathlib import Path
import textwrap
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "app/src/main/java/com/songjunyi/multiactivity"
OUT = ROOT / "report-assets"
FONT = ImageFont.truetype(r"C:\Windows\Fonts\consola.ttf", 23)
SMALL = ImageFont.truetype(r"C:\Windows\Fonts\consola.ttf", 20)

def figure(name, blocks):
    lines = []
    for filename, start_marker, end_marker in blocks:
        source = (SRC / filename).read_text(encoding="utf-8").splitlines()
        start = next(i for i, s in enumerate(source) if start_marker in s)
        end = next(i for i, s in enumerate(source) if i > start and end_marker in s)
        lines += [("", filename), ("", "")]
        for i in range(start, end):
            wrapped = textwrap.wrap(source[i], width=67, replace_whitespace=False,
                                    drop_whitespace=False) or [""]
            lines.append((str(i+1), wrapped[0]))
            lines += [("", "    "+s) for s in wrapped[1:]]
        lines += [("", "")]
    image = Image.new("RGB", (1050, len(lines)*29+40), "#0D1726")
    draw = ImageDraw.Draw(image)
    for i,(number, text) in enumerate(lines):
        draw.text((18,20+i*29), number, font=SMALL, fill="#7893AF")
        draw.text((85,20+i*29), text, font=FONT, fill="#E8F3FF")
    image.save(OUT / name)

figure("code-01-intents.png", [
    ("MainActivity.kt", "val message = messageInput", "label(messageCard"),
    ("ResultActivity.kt", "val value = field", "button(panel, R.string.cancel"),
    ("MainActivity.kt", "override fun onActivityResult", "override fun onSaveInstanceState"),
])
figure("code-02-camera.png", [
    ("CameraActivity.kt", "private fun capture()", "override fun onActivityResult"),
])
figure("code-03-stopwatch.png", [
    ("StopwatchActivity.kt", "savedInstanceState?.let", "screen(R.string"),
    ("StopwatchActivity.kt", "private fun elapsed()", "private fun formatTime"),
    ("StopwatchActivity.kt", "override fun onSaveInstanceState", "        super.onSaveInstanceState"),
])
