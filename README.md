# Joshua de Souza: personal site

Source for the personal website of Joshua de Souza, MSE, Senior Principal
Scientist at Medtronic (Acute Care & Monitoring) and medical-device inventor.
The site covers selected device and monitoring work, recognition, career,
patents and publications.

Live site: https://jjds1.github.io/jds.github.io/

## How it's built

A single hand-written HTML page with two stylesheets and one small script.
There is no framework, no build step and no package to install.

| Path | What it is |
|---|---|
| `index.html` | All page content |
| `404.html` | "Page not found" page |
| `assets/css/tokens.css` | Design tokens: color, type, spacing, fonts |
| `assets/css/site.css` | Layout and components |
| `assets/js/site.js` | Optional enhancements (email link, current-section highlight) |
| `assets/fonts/` | Self-hosted Newsreader and Atkinson Hyperlegible Next (SIL OFL 1.1) |
| `og-image.png`, `favicon*` | Social preview image and icons (made by `tools/render-images.py`) |
| `tools/` | Maintenance scripts; not published |
| `DESIGN.md` | Design rationale and component specs; not published |
| `DEPLOY.md` | How to publish, secure and maintain the site |

Preview locally from the folder that contains this repo:

```sh
python3 -m http.server 8000
# then open http://localhost:8000/jds.github.io/
```

See [DEPLOY.md](DEPLOY.md) for publishing and maintenance, and
[SECURITY.md](SECURITY.md) for reporting security issues.

## License

Site content and text © Joshua de Souza. Fonts are under the SIL Open Font
License 1.1 (see `assets/fonts/`).
