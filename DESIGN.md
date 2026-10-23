# Design: Joshua de Souza portfolio (2026)

Source of truth for the site's look. Tokens live in `assets/css/tokens.css`.
The implementation is `index.html` + `assets/css/site.css`. This file is not
published (the deploy workflow publishes an allow-list of files only).

## Direction: "instrument record"

Joshua's work sits where clinical instruments meet the people they serve:
EEG and continuous monitoring, cataract surgery, preeclampsia triage. The page
borrows from the most trusted artefact in that world, the recorded trace on
chart paper. It does not borrow the look of a monitor UI or a dev portfolio.
The ground is a cool, faintly blue paper with an ECG-style 8/40 px grid in the
hero only. The one memorable element is a single physiological trace that
runs the full width of the viewport and draws itself once on load. It starts
with a calibration pulse, moves into EEG (his Singapore neuro work and his
Medtronic EEG-connectivity work) and settles into an arterial-pressure
waveform (continuous monitoring, preeclampsia). Everything else stays quiet.

The typography is editorial, not technical. Newsreader, a serif with optical
sizes, sets the name at display size, the headings and the body. It gives the
page the voice of a journal article and a senior scientist. Atkinson
Hyperlegible Next, designed with the Braille Institute for low-vision
readers, sets every piece of evidence and metadata: dates, places, patent
numbers, awards. The choice fits the accessibility theme. Atkinson ships only a slashed zero
(no unslashed alternate in its OpenType features), which looked odd in years,
so digits 0–9 in sans text come from Newsreader via a `unicode-range` face
named "Figures" in `tokens.css`. Rule of thumb: **serif makes the claim, sans
carries the evidence.**

We avoided some defaults on purpose. There are no cards, shadows or gradients.
The radius is 0 except for focus rings. There are no all-caps eyebrows, no
`01/02/03` numbering on non-sequential content, no stat-tile row and no arrow
glyphs on links. We also avoided the warm cream and terracotta palette and the
near-black with acid accent. The one accent is a petrol blue taken from
the ink of monitoring printouts. Structure comes from alignment on a 12-column
grid and hairline rules that separate records. The timeline is the only place
with an axis, because it is the only true sequence.

## Type

| Token | Size (375 → 1440) | Family / weight | Use |
|---|---|---|---|
| `--fs-display` | 48 → 96 px | Newsreader 430, opsz 72, −0.022em, lh 0.98, lining nums | Name in hero only |
| `--fs-h2` | 24 → 36 px | Newsreader 480, opsz 48, −0.01em, lh 1.15 | Section headings |
| `--fs-h3` | 21 → 24 px | Newsreader 480, opsz 32, lh 1.25 | Work titles, timeline org |
| `--fs-lead` | 21 → 24 px | Newsreader 400, opsz 28, lh 1.4, max 52ch | Mission line, section intros |
| `--fs-body` | 18 px | Newsreader 400, lh 1.55, max 66ch, old-style nums | Body copy |
| `--fs-ui` | 16 px | Atkinson 430, lh 1.45 | Role line, timeline role, buttons |
| `--fs-small` | 14 px | Atkinson 430, +0.01em | Nav, meta, evidence lists |
| `--fs-meta` | 13 px | Atkinson 430 | Trace labels, footer |

- The steps follow the classic scale 13/14/16/18/21/24/36/48/72(/96).
- Sentence case everywhere. No all caps, no letter-spaced labels.
- Italic (Newsreader 400 italic) is for titles of papers and journals in Publications only.
- `text-wrap: balance` on h1–h3. `text-wrap: pretty` on paragraphs.
- Numerals: serif body uses old-style numerals. The sans uses lining numerals, drawn from Newsreader (see above). The timeline uses tabular numerals.

## Color

All colors are in OKLCH via `light-dark()`. The site follows `prefers-color-scheme`.
`<html data-theme="light|dark">` forces a theme if a toggle is added later.
Contrast ratios are measured against `--c-bg` and `--c-surface`.

| Token | Light | Dark | Role / contrast |
|---|---|---|---|
| `--c-bg` | `oklch(97.6% .006 230)` #f3f8fa | `oklch(20.5% .022 252)` #101821 | Page ground |
| `--c-surface` | `oklch(99.3% .003 230)` #fbfdfe | `oklch(24% .024 252)` #17202a | Rare raised areas (skip link, future dialogs) |
| `--c-ink` | `oklch(25% .03 252)` #172330 | `oklch(94% .008 230)` #e6ecef | Primary text, 14.9 / 15.0:1 |
| `--c-ink-2` | `oklch(44% .025 250)` #485460 | `oklch(80% .015 235)` #b5bfc6 | Secondary text, 7.2 / 9.6:1 |
| `--c-ink-3` | `oklch(53% .02 250)` #636d77 | `oklch(70% .02 240)` #94a0aa | Meta and labels, 4.9 / 6.2:1 (AA at 13 px) |
| `--c-accent` | `oklch(47% .105 225)` #006687 | `oklch(80% .105 215)` #62cfe8 | Links, focus ring, 6.1 / 9.9:1 |
| `--c-accent-hover` | `oklch(38% .1 228)` | `oklch(88% .08 212)` | Link hover |
| `--c-trace` | `oklch(40% .09 232)` #004f6f | `oklch(82% .11 205)` #5ed9e6 | Trace line, ticks, active nav, 8.4 / 10.7:1 |
| `--c-rule` | `oklch(87% .012 240)` | `oklch(34% .025 250)` | Hairlines (decorative, non-text) |
| `--c-grid` / `--c-grid-major` | `oklch(90.5% .016 228)` / `oklch(85% .022 228)` | `oklch(27.5% .026 250)` / `oklch(32% .03 248)` | Chart paper (hero only) |
| `--c-select` | `oklch(88% .05 220)` | `oklch(36% .07 225)` | `::selection` |

Dark mode is the same paper under night-ward lighting: a deep blue-slate,
not black. The trace becomes a cyan phosphor tone. No other hues are used. If
a status color is ever needed, add it as a token. Don't introduce one ad hoc.

## Space and grid

- Spacing uses a 4 px base: `--sp-1..10` = 4, 8, 12, 16, 24, 32, 48, 64, 96, 128 px. Gaps between sections use `--sp-section` (64 → 128 px).
- Page: `--page-max` is 1280 px of content. `--page-gutter` runs 16 px at 375 to 48 px at 1440.
- Grid: 12 columns with a `--grid-gap` of 16 → 24 px, from 64rem (1024 px) up. Below that the page is a single column.
- **Desktop column map, used by every section:**
  - cols 1–3 hold the margin: section titles, meta (where/when/stage), dates
  - cols 4–10 hold the main text column (about 66ch at 18 px)
  - cols 11–12 hold the aside: the work glyph, an optional headshot
- Text is left aligned throughout. Nothing is centred except inside glyphs.

## Components

### Nav (`.site-nav`)
- Sticky top bar: background is `--c-bg` at 88% with an 8 px backdrop blur and a 1 px `--c-rule` bottom border. Height is about 46 px.
- Left: the name in Atkinson 640 at 14 px, linking to `#top`. Right: Work, Recognition, Career, Publications, Contact.
- Links use `--c-ink-2`. Hover uses `--c-ink`. The current section gets `aria-current="true"`, `--c-ink` and a 2 px `--c-trace` underline that sits on the bar's border.
- Mobile (<48rem): two rows. The name sits on top. The links sit below, spread across the row; at 390 px all five fit. On narrower screens the row scrolls horizontally inside itself, and `site.js` adds a fade on the edge that has more links. No hamburger, since there are only five links.
- Scrollspy is optional (an IntersectionObserver in an external JS file). Without JS, nothing is marked current. That is fine.

### Hero
- Chart paper (`.hero::before`) covers the hero only. A mask fades it out over the bottom 45%.
- The name runs across cols 1–12 at `--fs-display`. It gets a −0.04em optical indent so the J aligns with the gutter.
- The role line (sans, `--c-ink-2`) sits in cols 1–3. The mission line (`--fs-lead` serif) sits in cols 4–10, baseline-aligned with the role.
- **The trace** (`.trace`, an inline `<svg viewBox="0 0 1440 160" preserveAspectRatio="none">`) bleeds the full viewport width. It uses `stroke: var(--c-trace)`, a 1.6 px `non-scaling-stroke` and `pathLength="1"` for the draw-on. The labels "EEG" and "Arterial pressure" use `--fs-meta` `--c-ink-3` and sit above their segments. The SVG is `aria-hidden`. Its meaning is decorative.
  - Below 48rem, the SVG is drawn at 200% width and shifted so the viewport shows viewBox x≈420–1140: the tail of the EEG, the transition and about 4 beats. This keeps the waveform from being squashed.
  - The path was generated by a script, not hand-drawn. The engineer can reuse the `d` string as is. Don't add a second trace anywhere else on the page.
- Evidence row (`.hero__evidence`): four short facts in sans at 14 px, each with a 2 px `--c-rule` left border. It uses 1 column on mobile, 2 at 48rem and 4 at 64rem. This replaces a stat-tile row. Keep them as sentences, not big numbers.
- **Headshot slot (optional, later):** put a `<figure class="hero__portrait">` in cols 11–12 on the row with the role and mission. Aspect 4:5, `--radius-portrait`, `filter: grayscale(1) contrast(1.05)`. With `mix-blend-mode: multiply` in light mode it sits on the paper. Max 176 px wide. On mobile it goes above the name at 72 px square. Leave it out entirely if no photo exists.

### Work entry (`.work`)
- An open record, not a card: a 1 px `--c-rule` top border and generous padding (32 / 48 px). No background, shadow or radius.
- Parts:
  - `.work__meta`: a `<dl>` of Where / When / Stage. `dt` is `--c-ink-3`, `dd` is `--c-ink`, sans at 14 px. Include only the facts that are known.
  - `.work__body`: an h3 title in sentence case that says what the thing is, 2–3 sentences of body, then `.work__evidence`. The evidence list starts with a dashed rule, uses sans at 14 px, and each item has a 0.45em × 2 px `--c-trace` tick. Links go on the noun ("Patent WO2022241322 / EP4337150", "MIT Solve Finalist").
  - `.work__glyph`: an optional 160×160 line diagram that encodes one real fact about the project. Strokes use `--c-ink-3`, faint guides use `--c-rule`, and one highlighted element uses `--c-trace`. It is `role="img"` with a `<title>`. Planned glyphs:
    - SILC: eye front view, fragmented lens, 3 mm limbal incision (built)
    - Preeclampsia: blood-pressure cuff arc or a 140/90 threshold line
    - Glaucoma: optic disc with cup-to-disc ratio
    - Continuous monitoring: 4-node connectivity graph
    - Urology: none (keep it generic)
    - Delirium: none
    - Singapore neuro: a 10–20 EEG electrode layout
    Projects without a glyph simply omit the SVG. The grid copes.
- Layout uses a container query on `.work-list`. At container ≥56rem: meta in cols 1–3, body in 4–10, glyph in 11–12. Below that: body first, then meta and glyph side by side on one row (meta left, glyph right, bottom-aligned).
- Order of entries: SILC, Preeclampsia (Gates), Glaucoma (NAM 2024), Continuous monitoring (Medtronic), Urology, Post-operative delirium, Neuro research (Singapore). Later entries can be shorter (title plus one sentence). The earlier entries get the full treatment.

### Timeline item (`.timeline__item`)
- An `<ol>`, most recent first. Each item has `.timeline__when` (sans at 14 px, `--c-ink-3`, tabular numerals, using `<time>`) and `.timeline__body` (the org in h3 serif, the role in sans 16 px `--c-ink-2`, then one serif paragraph).
- The axis is a 1 px `--c-rule` vertical line. Each entry has a 9 × 2 px `--c-trace` horizontal tick, like a scale division. The last item's axis fades out.
- Desktop: the date is right-aligned in cols 1–2, the axis sits in col 3 and the body in cols 4–10. Mobile: the axis sits on the left, with the date above the org.
- Exact years are not confirmed (see TODO). Use "2024 to present" style ranges. Write "to", not an en dash, to match the copy style.

### Recognition (no prototype; spec)
- A plain `<ul>` in cols 4–10. Each row has the award in serif at 18 px and the year/role in sans at 14 px `--c-ink-3`, right-aligned on desktop, with 1 px rules between rows. Rows: NAM Healthy Longevity Catalyst Award (2023, 2024), Gates Foundation Grand Challenges (Principal investigator), MIT Solve Finalist (Equitable Health Systems), SEMI Semiconductor MedTech Ecosystem Summit (Invited speaker), Peer reviewer for scientific journals.
- "750+ citations, h-index 7" goes in the Publications intro as a sentence. Don't make it a giant numeral. A `--fs-stat` token exists if a single figure is ever needed.

### Publications and patents (spec)
- Use two groups, Patents and Selected publications, each as an `<ol>` in cols 4–10.
- Each item: the title in Newsreader italic for papers or roman for patents, then a sans 14 px line with authors (bold his name variant with `<strong>`, Atkinson 640), venue, year and identifier link. Venue and year use `--c-ink-3`.
- End with "All publications on Google Scholar" linking `#` (**TODO: real URL**).

### Contact (spec)
- One serif lead sentence, then 2–3 links set as larger sans (16 px, 640 weight, accent underline): LinkedIn, Email, Johns Hopkins profile.
- The email link is assembled by an external JS file from parts. Without JS, show "Email via LinkedIn" instead (`<noscript>` is fine, but don't use inline script).

### Links
- Body links use `--c-accent`, a 1px underline at 0.2em offset, with the underline color at 45% accent. On hover the underline goes solid and the color becomes `--c-accent-hover`. The transition uses `--dur-fast`.
- External links carry no icon and no arrow. Open in the same tab.
- Focus: a 2 px `--c-accent` outline with a 3 px offset and 2 px radius on every interactive element. Never remove it.
- A skip link to `#main` is the first focusable element.

## Motion

1. **One orchestrated moment.** On first load the trace draws left to right (`stroke-dashoffset` 1 → 0, 2.4 s, `--ease-trace`, 200 ms delay). Its labels fade in at 1.8 s. Nothing else animates on load.
2. **Reactive only after that.** Link underline and color run at 120 ms. Disclosure elements, if any (e.g. "more publications" as `<details>`), run at 220 ms.
3. No scroll-triggered reveals or parallax, and no hover lift on entries.
4. Smooth anchor scroll is allowed. `scroll-padding-top: 5rem` clears the sticky nav.
5. View Transitions: `@view-transition { navigation: auto; }` may be added if a second page is ever introduced. It is not needed for a single page.
6. `prefers-reduced-motion: reduce` sets all durations to 0. The trace renders fully drawn and the scroll is instant. The prototype puts the animation inside `@media (prefers-reduced-motion: no-preference)`.

## Section layout summary

| Section | Desktop (≥64rem) | Mobile (375–390) |
|---|---|---|
| Nav | One row, name left, links right | Name row, scrollable link row below |
| Hero | Paper grid; name full width; role cols 1–3 and mission 4–10; full-bleed trace; 4-up evidence | Name 48 px; role; mission; trace at 2× crop; evidence stacked |
| Selected work | Head: title 1–3, intro 4–10. Entries: meta 1–3, body 4–10, glyph 11–12 | Title, intro, then each entry: body, then meta and glyph row |
| Recognition | Title 1–3; ruled list 4–10, year right-aligned | Title; list, year under award |
| Career | Date 1–2 (right-aligned), axis col 3, body 4–10 | Left axis, date over org |
| Publications | Title and citation sentence 1–3 / 4–10; patents then papers | Stacked |
| Contact | Lead sentence 4–10; links in one row | Stacked links, 44 px tap targets |
| Footer | `--fs-meta`, `--c-ink-3`: "© 2026 Joshua de Souza", plus "Last updated" | Same |

## Engineering notes

- **Paths must be relative.** The site is served from `/jds.github.io/`. Fonts are referenced from `tokens.css` as `../fonts/…`, so keep `tokens.css` in `assets/css/`.
- **CSP:** the prototype has no inline `<script>`, no `style=""` and no inline `<style>`. Inline `<svg>` with presentation done by classes is fine. The trace `d` attribute is about 7 KB inline. It could move to an external `.svg` via `<img>`, but CSS can't reach the stroke color there. Keep it inline.
- **Fonts** (120 KB total, all SIL OFL 1.1): `newsreader-roman-var.woff2` (65 KB, wght 400–560, opsz 20–72), `newsreader-italic-400.woff2` (23 KB), `atkinson-hyperlegible-next-var.woff2` (32 KB, wght 200–800). All are Latin subsets including curly quotes, dashes, ×, ≤ ≥ and arrows. Preload the two roman files with `crossorigin`. Any weight outside these ranges will be synthesised, so don't use them.
- The page uses `light-dark()` (Baseline 2024). Older browsers fall back to the browser default colors. If that matters, add plain light values before each `light-dark()` declaration.
- Previewing over `file://` blocks the fonts (CORS). Serve the repo over HTTP, e.g. `python3 -m http.server` from the parent directory, and open `/jds.github.io/`.
- `frame-ancestors` is ignored in a `<meta>` CSP, so framing protection needs a real header (see DEPLOY.md, Cloudflare).

## TODO (copy to confirm with Joshua)
- Google Scholar URL (`#` placeholder).
- Exact career years, which are currently shown as ranges.
- Public wording for the Gates preeclampsia project.
- Whether to use a dedicated contact email address.
