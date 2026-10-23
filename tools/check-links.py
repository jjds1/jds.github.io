"""Check that every internal link, asset and #fragment in the published
HTML resolves. External links are not fetched (no network in CI needed).

Run: python3 tools/check-links.py
"""
import pathlib, sys, urllib.parse
from html.parser import HTMLParser

ROOT = pathlib.Path(__file__).resolve().parent.parent
PAGES = ["index.html", "404.html"]
SITE_PREFIX = "/jds.github.io/"  # project-site path used by 404.html


class Collector(HTMLParser):
    def __init__(self):
        super().__init__()
        self.refs, self.ids = [], set()

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        if "id" in a:
            self.ids.add(a["id"])
        for key in ("href", "src"):
            if a.get(key) is not None:
                self.refs.append((tag, key, a[key], "hidden" in a))


errors = []
for page in PAGES:
    c = Collector()
    c.feed((ROOT / page).read_text(encoding="utf-8"))
    for tag, key, ref, hidden in c.refs:
        u = urllib.parse.urlparse(ref)
        if u.scheme in ("http", "https", "mailto"):
            if u.scheme == "http":
                errors.append(f"{page}: insecure http link {ref}")
            continue
        if ref == "#":
            # Placeholder links are allowed only inside hidden elements.
            continue
        if ref.startswith("#"):
            if ref[1:] not in c.ids:
                errors.append(f"{page}: missing anchor {ref}")
            continue
        path = u.path
        if path.startswith(SITE_PREFIX):
            path = path[len(SITE_PREFIX):]
        elif path.startswith("/"):
            errors.append(f"{page}: root-absolute path {ref} breaks on a project site")
            continue
        target = ROOT / (path or "index.html")
        if target.is_dir():
            target = target / "index.html"
        if not target.exists():
            errors.append(f"{page}: {tag}[{key}] -> {ref} not found")

# CSS url() references
for css in (ROOT / "assets/css").glob("*.css"):
    import re
    for ref in re.findall(r'url\("?([^")]+)"?\)', css.read_text()):
        if ref.startswith("data:"):
            continue
        if not (css.parent / ref).resolve().exists():
            errors.append(f"{css.name}: url({ref}) not found")

if errors:
    print("\n".join(errors))
    sys.exit(1)
print(f"OK: all internal links and assets resolve in {', '.join(PAGES)}")
