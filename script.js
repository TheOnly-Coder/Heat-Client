/* =====================================================
   Heat Client — GitHub Pages site
   ===================================================== */

(function () {
  'use strict';

  /* ---------- Latest release redirect (in case v1.9.0 changes) ---------- */
  // All download links already point to v1.9.0 statically (works without JS).
  // On load, we ask GitHub for the latest release and rewrite download links
  // so the page always serves the newest jar.
  const DOWNLOAD_SELECTOR = 'a[data-track="hero-download"], a[data-track="nav-download"]';

  async function refreshLatestDownload() {
    try {
      const res = await fetch(
        'https://api.github.com/repos/TheOnly-Coder/Heat-Client/releases/latest',
        { headers: { Accept: 'application/vnd.github+json' } }
      );
      if (!res.ok) return;
      const data = await res.json();
      const jarAsset = (data.assets || []).find(a => /\.jar$/i.test(a.name));
      if (!jarAsset) return;
      const url = jarAsset.browser_download_url;
      document.querySelectorAll(DOWNLOAD_SELECTOR).forEach(a => {
        a.setAttribute('href', url);
      });
      // Auto-update the version badge in the hero
      const badge = document.getElementById('versionBadge');
      if (badge && data.tag_name) {
        badge.textContent = data.tag_name;
      }
    } catch (e) {
      /* silent — fall back to static v1.9.0 link */
    }
  }

  /* ---------- Navbar scroll state ---------- */
  const nav = document.getElementById('nav');
  function onScroll() {
    if (window.scrollY > 24) nav.classList.add('scrolled');
    else nav.classList.remove('scrolled');
  }
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();

  /* ---------- Reveal on scroll (IntersectionObserver) ---------- */
  const reveals = document.querySelectorAll('[data-reveal]');
  reveals.forEach(el => {
    const delay = parseInt(el.getAttribute('data-delay') || '0', 10);
    el.style.setProperty('--reveal-delay', delay);
  });

  if ('IntersectionObserver' in window) {
    const io = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible');
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.12, rootMargin: '0px 0px -8% 0px' });

    reveals.forEach(el => io.observe(el));
  } else {
    reveals.forEach(el => el.classList.add('is-visible'));
  }

  /* ---------- Download button cursor glow ---------- */
  const btn = document.getElementById('downloadBtn');
  if (btn) {
    const update = (e) => {
      const rect = btn.getBoundingClientRect();
      const x = (e.clientX ?? rect.left + rect.width / 2) - rect.left;
      const y = (e.clientY ?? rect.top + rect.height / 2) - rect.top;
      btn.style.setProperty('--mx', x + 'px');
      btn.style.setProperty('--my', y + 'px');
    };
    btn.addEventListener('mousemove', update);
    btn.addEventListener('mouseenter', update);
    // Touch — center the glow
    btn.addEventListener('touchstart', update, { passive: true });
  }

  /* ---------- Copy server IP ---------- */
  const copyBtn = document.getElementById('copyIp');
  if (copyBtn) {
    copyBtn.addEventListener('click', async () => {
      const ip = 'willanarchy.exaroton.me';
      const label = copyBtn.querySelector('.copy-text');
      try {
        await navigator.clipboard.writeText(ip);
      } catch (_) {
        const ta = document.createElement('textarea');
        ta.value = ip;
        document.body.appendChild(ta);
        ta.select();
        try { document.execCommand('copy'); } catch (e) {}
        document.body.removeChild(ta);
      }
      copyBtn.classList.add('copied');
      if (label) label.textContent = 'Copied!';
      setTimeout(() => {
        copyBtn.classList.remove('copied');
        if (label) label.textContent = 'Copy';
      }, 1800);
    });
  }

  /* ---------- Ember particle canvas ---------- */
  const canvas = document.getElementById('ember-canvas');
  if (canvas && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    const ctx = canvas.getContext('2d');
    let w = 0, h = 0, dpr = 1;
    const embers = [];
    const MAX = Math.min(70, Math.floor(window.innerWidth / 22));

    function resize() {
      dpr = Math.min(window.devicePixelRatio || 1, 2);
      w = window.innerWidth;
      h = window.innerHeight;
      canvas.width = w * dpr;
      canvas.height = h * dpr;
      canvas.style.width = w + 'px';
      canvas.style.height = h + 'px';
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }
    resize();
    window.addEventListener('resize', resize);

    function spawn() {
      const r = 0.8 + Math.random() * 2.2;
      embers.push({
        x: Math.random() * w,
        y: h + r,
        r,
        vx: (Math.random() - 0.5) * 0.4,
        vy: -(0.3 + Math.random() * 1.1),
        life: 0,
        maxLife: 200 + Math.random() * 260,
        hue: 18 + Math.random() * 22, // 18–40° (orange range)
        flicker: Math.random() * Math.PI * 2,
      });
    }

    function tick() {
      ctx.clearRect(0, 0, w, h);
      while (embers.length < MAX) spawn();

      for (let i = embers.length - 1; i >= 0; i--) {
        const e = embers[i];
        e.life++;
        e.x += e.vx + Math.sin((e.life + e.flicker) * 0.03) * 0.3;
        e.y += e.vy;
        e.vy *= 0.997;
        e.flicker += 0.08;

        const t = e.life / e.maxLife;
        const alpha = Math.max(0, (1 - t) * (0.7 + Math.sin(e.flicker) * 0.3));
        const radius = Math.max(0.1, e.r * (1 - t * 0.4));

        const grad = ctx.createRadialGradient(e.x, e.y, 0, e.x, e.y, radius * 4);
        grad.addColorStop(0, `hsla(${e.hue}, 100%, 65%, ${alpha})`);
        grad.addColorStop(0.4, `hsla(${e.hue}, 100%, 50%, ${alpha * 0.35})`);
        grad.addColorStop(1, `hsla(${e.hue}, 100%, 50%, 0)`);
        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.arc(e.x, e.y, radius * 4, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = `hsla(${e.hue + 10}, 100%, 75%, ${alpha})`;
        ctx.beginPath();
        ctx.arc(e.x, e.y, radius, 0, Math.PI * 2);
        ctx.fill();

        if (e.life >= e.maxLife || e.y < -20) embers.splice(i, 1);
      }

      requestAnimationFrame(tick);
    }
    tick();
  }

  /* ---------- Smooth anchor scroll with navbar offset ---------- */
  document.querySelectorAll('a[href^="#"]').forEach(link => {
    link.addEventListener('click', (e) => {
      const id = link.getAttribute('href');
      if (id.length <= 1) return;
      const target = document.querySelector(id);
      if (!target) return;
      e.preventDefault();
      // If the target is a closed <details>, open it (so users see the answer)
      if (target.tagName === 'DETAILS' && !target.open) {
        target.open = true;
      }
      const top = target.getBoundingClientRect().top + window.scrollY - 70;
      window.scrollTo({ top, behavior: 'smooth' });
    });
  });

  /* ---------- FAQ accordion (one-open-at-a-time) ---------- */
  const faqItems = document.querySelectorAll('.faq-item');
  faqItems.forEach(item => {
    item.addEventListener('toggle', () => {
      if (item.open) {
        faqItems.forEach(other => {
          if (other !== item && other.open) other.open = false;
        });
        // Scroll the opened item into view nicely (below the fixed navbar)
        setTimeout(() => {
          const top = item.getBoundingClientRect().top + window.scrollY - 80;
          if (top < window.scrollY) {
            window.scrollTo({ top, behavior: 'smooth' });
          }
        }, 60);
      }
    });
  });

  // Open the first FAQ by default for discoverability
  if (faqItems.length) faqItems[0].open = true;

  /* ---------- Lightbox ---------- */
  const triggers = Array.from(document.querySelectorAll('[data-lightbox]'));
  const lb = document.getElementById('lightbox');
  const lbImg = document.getElementById('lightboxImg');
  const lbCaption = document.getElementById('lightboxCaption');
  const lbClose = document.getElementById('lightboxClose');
  const lbPrev = document.getElementById('lightboxPrev');
  const lbNext = document.getElementById('lightboxNext');

  let currentIdx = 0;

  // Inject a hover "click to zoom" hint badge into each gallery frame
  triggers.forEach(t => {
    const hint = document.createElement('span');
    hint.className = 'zoom-hint';
    hint.innerHTML = `
      <svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
        <circle cx="11" cy="11" r="7" fill="none" stroke="currentColor" stroke-width="2"/>
        <path d="M11 8v6M8 11h6M20 20l-3.5-3.5" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      Click to expand
    `;
    t.appendChild(hint);
  });

  function openLightbox(idx) {
    if (!triggers.length) return;
    currentIdx = ((idx % triggers.length) + triggers.length) % triggers.length;
    const t = triggers[currentIdx];
    const url = t.getAttribute('href');
    const title = t.getAttribute('data-title') || '';
    const alt = t.querySelector('img')?.getAttribute('alt') || '';
    lbImg.setAttribute('src', url);
    lbImg.setAttribute('alt', alt);
    lbCaption.textContent = title;
    lb.classList.add('is-open');
    lb.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
  }

  function closeLightbox() {
    lb.classList.remove('is-open');
    lb.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
    // Clear src after transition to free memory
    setTimeout(() => { if (!lb.classList.contains('is-open')) lbImg.setAttribute('src', ''); }, 350);
  }

  function navLightbox(delta) {
    openLightbox(currentIdx + delta);
  }

  triggers.forEach((t, i) => {
    t.addEventListener('click', (e) => {
      e.preventDefault();
      openLightbox(i);
    });
  });

  if (lbClose) lbClose.addEventListener('click', closeLightbox);
  if (lbPrev)  lbPrev.addEventListener('click', () => navLightbox(-1));
  if (lbNext)  lbNext.addEventListener('click', () => navLightbox(1));

  // Click on backdrop closes
  if (lb) {
    lb.addEventListener('click', (e) => {
      if (e.target === lb) closeLightbox();
    });
  }

  // Keyboard controls
  document.addEventListener('keydown', (e) => {
    if (!lb.classList.contains('is-open')) return;
    if (e.key === 'Escape')     closeLightbox();
    else if (e.key === 'ArrowLeft')  navLightbox(-1);
    else if (e.key === 'ArrowRight') navLightbox(1);
  });

  /* ---------- Kick off ---------- */
  refreshLatestDownload();
})();
