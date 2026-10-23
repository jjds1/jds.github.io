// Progressive enhancement only. The page works fully without this file.
(function () {
  "use strict";

  // 1. Email link. The address is never written into the HTML. Without
  //    JavaScript the link reads "Email via LinkedIn" and opens LinkedIn.
  var mail = document.querySelector("a[data-email]");
  if (mail) {
    var user = ["desouza", "joshua00"].join("");
    var domain = ["gmail", "com"].join(".");
    mail.href = "mailto:" + user + "@" + domain;
    mail.textContent = "Email";
    mail.removeAttribute("rel");
  }

  // 2. Mobile nav: show a fade on whichever edge has more links hidden.
  var nav = document.querySelector(".site-nav nav");
  if (nav) {
    var updateFade = function () {
      var scrollable = nav.scrollWidth > nav.clientWidth + 1;
      nav.classList.toggle("is-scrollable", scrollable);
      nav.classList.toggle("at-end", scrollable && nav.scrollLeft + nav.clientWidth >= nav.scrollWidth - 1);
    };
    updateFade();
    nav.addEventListener("scroll", updateFade, { passive: true });
    window.addEventListener("resize", updateFade);
  }

  // 3. Mark the section currently in view in the nav.
  if (!("IntersectionObserver" in window) || !nav) return;
  var links = Array.prototype.slice.call(nav.querySelectorAll('a[href^="#"]'));
  var byId = {};
  links.forEach(function (a) { byId[a.getAttribute("href").slice(1)] = a; });
  var sections = links
    .map(function (a) { return document.getElementById(a.getAttribute("href").slice(1)); })
    .filter(Boolean);
  var visible = {};

  var setCurrent = function (id) {
    links.forEach(function (a) {
      if (a === byId[id]) a.setAttribute("aria-current", "true");
      else a.removeAttribute("aria-current");
    });
    var current = byId[id];
    if (current && nav.scrollWidth > nav.clientWidth) {
      var left = current.offsetLeft - (nav.clientWidth - current.offsetWidth) / 2;
      nav.scrollTo({ left: left, behavior: "auto" });
    }
  };

  var observer = new IntersectionObserver(function (entries) {
    entries.forEach(function (e) { visible[e.target.id] = e.isIntersecting; });
    var atBottom = window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 2;
    if (atBottom) { setCurrent(sections[sections.length - 1].id); return; }
    var first = sections.filter(function (s) { return visible[s.id]; })[0];
    setCurrent(first ? first.id : null);
  }, { rootMargin: "-30% 0px -60% 0px" });

  sections.forEach(function (s) { observer.observe(s); });
  window.addEventListener("scroll", function () {
    if (window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 2) {
      setCurrent(sections[sections.length - 1].id);
    }
  }, { passive: true });
})();
