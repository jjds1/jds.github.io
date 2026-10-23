# Security policy

This repository holds a static personal website. It has no server code, no
forms, no cookies and no third-party scripts.

## Reporting a problem

If you find a security issue (for example, a way to inject content, a
compromised dependency in the GitHub Actions workflows, or a takeover risk on
the site's domain), please report it privately:

- Use GitHub's **Report a vulnerability** button on the repository's
  **Security** tab (private vulnerability reporting), or
- Send a message through LinkedIn: https://www.linkedin.com/in/jj-ds/

Please don't open a public issue for security problems. You can expect an
acknowledgement within a week.

## What's in place

- A strict Content-Security-Policy in each page: only same-origin scripts,
  styles, fonts and images; no inline scripts; no forms; no framing of other
  content.
- No requests to any third party. Fonts are self-hosted.
- GitHub Actions pinned to full commit SHAs, updated by Dependabot.
- Deployment publishes an explicit allow-list of files only.
