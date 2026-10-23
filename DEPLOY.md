# Publishing and looking after the site

This guide is written for someone comfortable with technology but not a web
developer. Each step says where to click.

---

## (a) Merge the pull request

1. On GitHub, open the repository **jjds1/jds.github.io**, then the
   **Pull requests** tab.
2. Open the pull request from the `portfolio-2026` branch.
3. Wait for the **Checks** to turn green. These validate the HTML and confirm
   every internal link and file exists.
4. Click **Merge pull request**, then **Confirm merge**.

## (b) Tell GitHub Pages to use the deploy workflow

You only need to do this once.

1. Go to **Settings → Pages**.
2. Under **Build and deployment → Source**, choose **GitHub Actions**. (The
   old setting was probably "Deploy from a branch". Change it.)
3. Go to the **Actions** tab. Open **Deploy to GitHub Pages**, then click
   **Run workflow** (or just push to `main`).
4. When it finishes, the site is live at
   `https://jjds1.github.io/jds.github.io/`.

**Why this matters:** the workflow publishes only an explicit list of files:
`index.html`, `404.html`, `assets/`, the icons, `og-image.png`, `robots.txt`,
`sitemap.xml` and `.nojekyll`. Files like `DESIGN.md`, `README.md`, this file,
`tools/` and `.github/` are never published.

## (c) Get a clean address (recommended)

Right now the address ends in `/jds.github.io/`. Choose **one** of the options
below.

### Option 1: rename the repository (free, five minutes)

1. Go to **Settings → General → Repository name**. Rename it to exactly
   `jjds1.github.io`.
2. The site moves to `https://jjds1.github.io/`. GitHub redirects the old
   repository URL, but **not** the old Pages URL, so update links you've
   shared (for example, on LinkedIn).
3. Update the addresses in the files (see "Lines to change" below), with
   `https://jjds1.github.io/` as the new address and `/` as the new path.

### Option 2: a custom domain (about $10–20 a year)

1. **Buy a domain** from a registrar such as Cloudflare Registrar, Porkbun or
   Namecheap (for example `joshuadesouza.com`). Turn on the registrar's
   two-factor login and its "registrar lock".
2. **Verify the domain with GitHub first.** This stops anyone else from
   pointing their GitHub site at your domain (a "domain takeover"):
   - Click your avatar → **Settings → Pages** (your *account* settings, not
     the repository's) → **Add a domain**.
   - GitHub shows a `TXT` record. Add it at your registrar's DNS page, then
     click **Verify**.
3. **Add DNS records** at your registrar:
   - For the bare domain (`joshuadesouza.com`), add four **A** records:
     `185.199.108.153`, `185.199.109.153`, `185.199.110.153`,
     `185.199.111.153`.
   - Also add four **AAAA** records (IPv6): `2606:50c0:8000::153`,
     `2606:50c0:8001::153`, `2606:50c0:8002::153`, `2606:50c0:8003::153`.
   - For `www`, add a **CNAME** record pointing to `jjds1.github.io`.
   - Don't use wildcard records (`*`). They make takeover easier.
4. In the repository, go to **Settings → Pages → Custom domain**. Enter the
   domain and click **Save**. Wait for the DNS check to pass (minutes to a
   few hours).
5. Tick **Enforce HTTPS** once GitHub allows it. The certificate can take up
   to an hour.
6. Update the addresses in the files (see below), with your domain as the
   new address and `/` as the new path.

With the Actions workflow, you don't need a `CNAME` file in the repository.
The setting in step 4 is enough.

### Lines to change after either option

Replace `https://jjds1.github.io/jds.github.io/` with the new address
(for example `https://joshuadesouza.com/`):

| File | What to change |
|---|---|
| `index.html` | `<link rel="canonical" href="…">` |
| `index.html` | `og:url`, `og:image` and `twitter:image` (the `og-image.png` address) |
| `index.html` | In the JSON-LD block: `"url"` and `"image"` |
| `sitemap.xml` | `<loc>…</loc>` |
| `robots.txt` | The `Sitemap:` line |

Then in **`404.html`**, replace every `/jds.github.io/` with `/`. That covers
the two icons, the two stylesheets, the name link and "Go to the home page".
Also change `SITE_PREFIX` in `tools/check-links.py` to `"/"`. Run a search for
`jds.github.io` afterwards to confirm nothing is left.

### Optional: Cloudflare in front for real security headers

GitHub Pages can't send custom HTTP headers. The page's built-in policy
covers most protections, but some only work as headers. The main one is
**stopping other sites from embedding your page in a frame**
(`frame-ancestors`), which browsers ignore in a `<meta>` tag. A free
Cloudflare account can add these headers. This only works with a custom
domain (Option 2).

1. Add the domain to Cloudflare and switch the registrar's nameservers to the
   two Cloudflare gives you. Recreate the DNS records from step 3, **proxied**
   (orange cloud).
2. **SSL/TLS → Overview:** set the mode to **Full (strict)**.
3. **SSL/TLS → Edge Certificates:** turn on **Always Use HTTPS**. Turn on
   **HSTS** with max-age 6 months, include subdomains and no preload at
   first. Raise it to 12 months and preload once you're sure.
4. **Rules → Transform Rules → Modify Response Header → Create rule.** Name it
   "Security headers", apply it to **All incoming requests**, and **Set
   static** these headers:

   | Header | Value |
   |---|---|
   | `Content-Security-Policy` | `default-src 'none'; script-src 'self'; style-src 'self'; img-src 'self' data:; font-src 'self'; base-uri 'none'; form-action 'none'; frame-ancestors 'none'; upgrade-insecure-requests` |
   | `X-Frame-Options` | `DENY` |
   | `X-Content-Type-Options` | `nosniff` |
   | `Referrer-Policy` | `strict-origin-when-cross-origin` |
   | `Permissions-Policy` | `camera=(), microphone=(), geolocation=(), payment=(), usb=(), interest-cohort=()` |
   | `Cross-Origin-Opener-Policy` | `same-origin` |

5. Check the result at https://securityheaders.com. It should score an A.

If you later change the CSP in the HTML, change it in Cloudflare too. When
both exist, the browser enforces both.

## (d) Keep the account and repository secure

- **Two-factor login with a passkey or security key.** In your GitHub
  account, go to **Settings → Password and authentication**. Add a
  **passkey** (your phone or laptop) and, ideally, a hardware **security
  key**. Save the recovery codes offline. Remove SMS as a method if you can.
- **Protect `main`.** In the repository, go to **Settings → Rules →
  Rulesets → New branch ruleset**. Set the target to the default branch and
  turn on:
  - Require a pull request before merging
  - Require status checks to pass (add **Checks / html**)
  - Block force pushes
  - Restrict deletions
- **Turn off what you don't use.** In **Settings → General → Features**,
  untick Wiki, Projects and Discussions (Issues are optional). In
  **Settings → Actions → General**, set **Workflow permissions** to
  **Read repository contents** and untick "Allow GitHub Actions to create
  and approve pull requests".
- **Security features.** In **Settings → Code security**, turn on
  Dependabot alerts, secret scanning and push protection, and private
  vulnerability reporting.
- **Dependabot pull requests.** Each week, Dependabot may open pull requests
  that update the pinned GitHub Actions. Open each one, check that it comes
  from `dependabot[bot]` and only changes a version line plus its commit SHA
  in `.github/workflows/`, wait for the checks, then merge.
- **Never commit secrets.** That includes passwords, API keys, private notes,
  unpublished projects and personal documents. The repository is public, and
  anything committed stays in the history even after you delete it.
  `.gitignore` blocks common secret files, but it's only a safety net.
- **Review third-party access.** In **Settings → Applications** (account),
  revoke any apps or OAuth tokens you don't recognise.

## (e) Editing the content

All text is in **`index.html`**. Each section is labelled with an `id`:

| Section | Look for |
|---|---|
| Name, role line, mission | `<section class="hero"` |
| The four short facts under the trace | `<ul class="hero__evidence">` |
| Selected work | `<section … id="work"`. Each project is an `<article class="work">`. Copy one to add another. |
| Recognition | `id="recognition"`, one `<li>` per row |
| Career | `id="career"`, one `<li class="timeline__item">` per role, most recent first |
| Publications and patents | `id="publications"` |
| Contact | `id="contact"` |
| Footer date | `<footer class="site-footer">`. Update "Last updated" when you edit. |

Things to fill in later:

- **Google Scholar link:** in the Publications section, find
  `<!-- Google Scholar URL: set href and remove hidden -->`. Put the profile URL in the `href`
  and delete the word `hidden` from the `<p class="pubs__more" hidden>`
  line so the link appears.
- **Selected publications:** add `<li>` items to a new list under Patents,
  following the pattern of the patent item. Use
  `<span class="pubs__title pubs__title--paper">` for paper titles (they show
  in italics).
- **Email address:** the address is assembled in `assets/js/site.js` and never
  written into the HTML. To change it, edit the two lines that set `user` and
  `domain`.

**Preview before you publish.** In a terminal, go to the folder that
*contains* the `jds.github.io` folder and run:

```sh
python3 -m http.server 8000
```

Open http://localhost:8000/jds.github.io/ in a browser. Press Ctrl+C to stop.
(Opening the HTML file directly doesn't load the fonts, so always use the
server.)

**Publish:** make the edit on a new branch, open a pull request, wait for the
checks, then merge. The site updates about a minute after the merge.

If you change the name or role, regenerate the social preview image:
`python3 tools/render-images.py`. This needs
`pip install playwright && playwright install chromium`.

## (f) Maintenance checklist

**Monthly (10 minutes)**
- Merge or close any Dependabot pull requests.
- Open the live site on your phone and a laptop. Check that the email link,
  LinkedIn and the Johns Hopkins profile link all work.
- Look at **Actions** for any failed runs.

**Quarterly (30 minutes)**
- Update citations, awards and the career section. Change the footer's
  "Last updated" date and `<lastmod>` in `sitemap.xml`.
- Check external links still resolve (patent, MIT Solve, LinkedIn, JHU
  profile).
- Review **Settings → Rules**, **Collaborators** and **Deploy keys**. Remove
  anything you don't recognise.
- Check your GitHub security log (**Settings → Security log**) for
  unexpected sign-ins.
- With a custom domain: confirm the domain is still verified and auto-renew
  is on at the registrar. Run https://securityheaders.com again.

**Yearly**
- Update the footer copyright year.
- Replace your security key or passkey if a device was lost, and confirm the
  recovery codes are still where you saved them.
