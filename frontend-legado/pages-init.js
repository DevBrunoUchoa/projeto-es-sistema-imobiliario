/* EstudanteLar — Shared init for inner pages */
(function () {

  /* ---- Navbar mobile toggle ---- */
  var toggle = document.getElementById('mobileToggle');
  var links  = document.getElementById('navLinks');

  if (toggle && links) {
    toggle.addEventListener('click', function () {
      var open = links.classList.toggle('open');
      toggle.classList.toggle('open', open);
      toggle.setAttribute('aria-label', open ? 'Fechar menu' : 'Abrir menu');
    });
    document.addEventListener('click', function (e) {
      if (!toggle.contains(e.target) && !links.contains(e.target)) {
        links.classList.remove('open');
        toggle.classList.remove('open');
      }
    });
  }

  /* ---- Sidebar drawer ---- */
  var sidebarBtn = document.getElementById('sidebarToggle');
  var sidebar    = document.querySelector('.dash-sidebar');
  var overlay    = document.getElementById('sidebarOverlay');

  function closeSidebar() {
    if (sidebar) sidebar.classList.remove('open');
    if (overlay) overlay.classList.remove('open');
  }

  if (sidebarBtn && sidebar) {
    sidebarBtn.addEventListener('click', function () {
      var open = sidebar.classList.toggle('open');
      if (overlay) overlay.classList.toggle('open', open);
    });
    if (overlay) {
      overlay.addEventListener('click', closeSidebar);
    }
    /* Close on link tap (mobile UX) */
    sidebar.querySelectorAll('a').forEach(function (a) {
      a.addEventListener('click', function () {
        if (window.innerWidth <= 900) closeSidebar();
      });
    });
  }

})();
