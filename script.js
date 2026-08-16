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
      const top = target.getBoundingClientRect().top + window.scrollY - 70;
      window.scrollTo({ top, behavior: 'smooth' });
    });
  });

  /* ---------- Kick off ---------- */
  refreshLatestDownload();
})();
