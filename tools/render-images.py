"""Render og-image.png and the PNG favicons with Playwright.

Run from anywhere:  python3 tools/render-images.py
Needs: pip install playwright && playwright install chromium
It starts a throwaway local server so the self-hosted fonts load.
"""
import functools, http.server, pathlib, threading
from playwright.sync_api import sync_playwright

ROOT = pathlib.Path(__file__).resolve().parent.parent

handler = functools.partial(http.server.SimpleHTTPRequestHandler, directory=str(ROOT))
server = http.server.ThreadingHTTPServer(("127.0.0.1", 0), handler)
threading.Thread(target=server.serve_forever, daemon=True).start()
base = f"http://127.0.0.1:{server.server_port}"

with sync_playwright() as p:
    browser = p.chromium.launch()
    page = browser.new_page(viewport={"width": 1200, "height": 630}, color_scheme="light")
    page.goto(f"{base}/tools/og-image.html")
    page.evaluate("document.fonts.ready")
    page.wait_for_timeout(300)
    page.screenshot(path=str(ROOT / "og-image.png"))

    svg = (ROOT / "favicon.svg").read_text()
    for size, name in [(32, "favicon-32.png"), (180, "apple-touch-icon.png")]:
        pg = browser.new_page(viewport={"width": size, "height": size}, color_scheme="light")
        pg.set_content(
            "<style>html,body{margin:0}svg{display:block;width:%dpx;height:%dpx}</style>%s"
            % (size, size, svg)
        )
        pg.screenshot(path=str(ROOT / name), omit_background=False)
        pg.close()
    browser.close()
server.shutdown()
print("wrote og-image.png, favicon-32.png, apple-touch-icon.png")
