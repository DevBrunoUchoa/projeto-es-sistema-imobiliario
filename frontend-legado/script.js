/* =============================================
   CampusLiving — Main Script
   ============================================= */

let allProperties = [];

const state = {
  search: '',
  type: '',
  maxPrice: 9999,
  maxDistance: 5,
  shared: null,   // null = any, true = only shared, false = only complete
  gender: 'any',
  pet: false,
  smoking: false,
  furnished: false,
  wifi: false,
  parking: false,
  ac: false,
  sort: 'price_asc',
  quickFilters: new Set(),
};

/* ---- Fetch data ---- */
async function loadData() {
  try {
    const res = await fetch('data.json');
    const data = await res.json();
    allProperties = data.properties;
    renderCards();
  } catch (e) {
    document.getElementById('resultCount').textContent = 'Erro ao carregar imóveis.';
    console.error('Erro ao carregar data.json:', e);
  }
}

/* ---- Filter & sort ---- */
function getFiltered() {
  let list = allProperties.filter(p => {
    if (!p.available) return false;

    if (state.search) {
      const q = state.search.toLowerCase();
      const hit = p.title.toLowerCase().includes(q)
        || p.address.toLowerCase().includes(q)
        || p.nearby.universities.some(u => u.name.toLowerCase().includes(q));
      if (!hit) return false;
    }
    if (state.type && p.type !== state.type) return false;
    if (p.price > state.maxPrice) return false;
    if (p.distance_university > state.maxDistance) return false;
    if (state.shared === true  && !p.features.shared) return false;
    if (state.shared === false && p.features.shared)  return false;
    if (state.gender !== 'any' && p.gender !== 'any' && p.gender !== state.gender) return false;
    if (state.pet       && !p.features.pet_friendly)     return false;
    if (state.smoking   && !p.features.smoking)           return false;
    if (state.furnished && !p.features.furnished)         return false;
    if (state.wifi      && !p.features.wifi)              return false;
    if (state.parking   && !p.features.parking)           return false;
    if (state.ac        && !p.features.air_conditioning)  return false;

    // quick filter chips
    for (const f of state.quickFilters) {
      if (!p.features[f]) return false;
    }

    return true;
  });

  list.sort((a, b) => {
    if (state.sort === 'price_asc')  return a.price - b.price;
    if (state.sort === 'price_desc') return b.price - a.price;
    if (state.sort === 'distance')   return a.distance_university - b.distance_university;
    return 0;
  });

  return list;
}

/* ---- Render cards ---- */
function renderCards() {
  const grid  = document.getElementById('propertiesGrid');
  const count = document.getElementById('resultCount');
  const noRes = document.getElementById('noResults');
  const filtered = getFiltered();

  grid.innerHTML = '';

  if (!filtered.length) {
    noRes.style.display = 'block';
    count.textContent = 'Nenhum imóvel encontrado';
    return;
  }

  noRes.style.display = 'none';
  count.textContent = `${filtered.length} imóve${filtered.length === 1 ? 'l' : 'is'} encontrado${filtered.length === 1 ? '' : 's'}`;

  filtered.forEach((p, i) => {
    const card = createCard(p, i);
    grid.appendChild(card);
  });
}

/* ---- Create card element ---- */
function createCard(p, index) {
  const el = document.createElement('article');
  el.className = 'prop-card';
  el.style.animationDelay = `${index * 60}ms`;
  el.setAttribute('role', 'button');
  el.setAttribute('tabindex', '0');
  el.setAttribute('aria-label', `Ver detalhes: ${p.title}`);

  const genderClass = p.gender === 'female' ? 'g-female' : p.gender === 'male' ? 'g-male' : 'g-any';
  const genderLabel = p.gender === 'female' ? 'Feminino' : p.gender === 'male' ? 'Masculino' : 'Qualquer gênero';

  const feats = [];
  if (p.features.furnished)        feats.push(`<span class="feat-tag"><i class="fa-solid fa-couch"></i> Mobiliado</span>`);
  if (p.features.wifi)              feats.push(`<span class="feat-tag"><i class="fa-solid fa-wifi"></i> Wi-Fi</span>`);
  if (p.features.air_conditioning)  feats.push(`<span class="feat-tag"><i class="fa-solid fa-snowflake"></i> A/C</span>`);
  if (p.features.parking)           feats.push(`<span class="feat-tag"><i class="fa-solid fa-square-parking"></i> Garagem</span>`);
  if (p.features.smoking)           feats.push(`<span class="feat-tag"><i class="fa-solid fa-smoking"></i> Fumantes</span>`);

  const addressShort = p.address.split('—')[1]?.trim() || p.address;

  el.innerHTML = `
    <div class="card-img-wrap">
      <img src="${p.images[0]}" alt="${p.title}" class="card-img" loading="lazy">
      <span class="badge-type">${p.typeLabel}</span>
      <span class="badge-dist">
        <i class="fa-solid fa-location-dot"></i> ${p.distance_university} km
      </span>
      ${p.features.pet_friendly ? '<span class="badge-pet"><i class="fa-solid fa-paw"></i></span>' : ''}
    </div>
    <div class="card-body">
      <div class="card-meta">
        <span class="card-travel">
          <i class="fa-solid fa-person-walking"></i> ${p.travel_time.walking} min a pé
        </span>
        <span class="card-gender ${genderClass}">${genderLabel}</span>
      </div>
      <h3 class="card-title">${p.title}</h3>
      <p class="card-addr">
        <i class="fa-solid fa-map-pin"></i>
        <span>${addressShort}</span>
      </p>
      <div class="card-feats">${feats.slice(0, 3).join('')}</div>
      <div class="card-footer">
        <div class="card-price">
          <span class="price-val">R$ ${p.price.toLocaleString('pt-BR')}</span>
          <span class="price-per">/mês</span>
        </div>
        <button class="btn-details">
          Ver detalhes <i class="fa-solid fa-arrow-right"></i>
        </button>
      </div>
    </div>
  `;

  const navigate = () => { window.location.href = `detail.html?id=${p.id}`; };
  el.addEventListener('click', navigate);
  el.addEventListener('keydown', e => { if (e.key === 'Enter' || e.key === ' ') navigate(); });
  el.querySelector('.btn-details').addEventListener('click', e => { e.stopPropagation(); navigate(); });

  return el;
}

/* ---- Reset filters ---- */
function resetFilters() {
  state.search = '';
  state.type = '';
  state.maxPrice = 9999;
  state.maxDistance = 5;
  state.shared = null;
  state.gender = 'any';
  state.pet = false;
  state.smoking = false;
  state.furnished = false;
  state.wifi = false;
  state.parking = false;
  state.ac = false;
  state.quickFilters.clear();

  document.getElementById('searchInput').value = '';
  document.getElementById('typeFilter').value = '';
  document.getElementById('priceFilter').value = '9999';
  document.getElementById('minPrice').value = '';
  document.getElementById('maxPrice').value = '';
  document.getElementById('distanceRange').value = 5;
  document.getElementById('distanceDisplay').textContent = 'até 5 km';
  document.querySelectorAll('.fp-check-item input').forEach(cb => cb.checked = false);
  document.querySelectorAll('.tg-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.value === '' || btn.dataset.value === 'any');
  });
  document.querySelectorAll('.qf-chip').forEach(c => c.classList.remove('active'));

  updateFilterCount();
  renderCards();
  closeFilterPanel();
}

/* ---- Count active filters ---- */
function updateFilterCount() {
  let n = 0;
  if (state.maxPrice < 9999) n++;
  if (state.maxDistance < 5) n++;
  if (state.shared !== null) n++;
  if (state.gender !== 'any') n++;
  if (state.pet) n++;
  if (state.smoking) n++;
  if (state.furnished) n++;
  if (state.wifi) n++;
  if (state.parking) n++;
  if (state.ac) n++;
  n += state.quickFilters.size;

  const badge = document.getElementById('filterCount');
  badge.textContent = n;
  badge.style.display = n > 0 ? 'inline-grid' : 'none';
}

/* ---- Filter panel ---- */
function openFilterPanel() {
  document.getElementById('filterPanel').classList.add('open');
  document.getElementById('filterOverlay').classList.add('open');
  document.body.style.overflow = 'hidden';
}
function closeFilterPanel() {
  document.getElementById('filterPanel').classList.remove('open');
  document.getElementById('filterOverlay').classList.remove('open');
  document.body.style.overflow = '';
}

/* ---- Apply panel filters ---- */
function applyPanelFilters() {
  const minP = parseInt(document.getElementById('minPrice').value) || 0;
  const maxP = parseInt(document.getElementById('maxPrice').value) || 9999;
  state.maxPrice = maxP || 9999;

  state.maxDistance = parseFloat(document.getElementById('distanceRange').value);

  state.pet       = document.getElementById('fPet').checked;
  state.smoking   = document.getElementById('fSmoking').checked;
  state.furnished = document.getElementById('fFurnished').checked;
  state.wifi      = document.getElementById('fWifi').checked;
  state.parking   = document.getElementById('fParking').checked;
  state.ac        = document.getElementById('fAC').checked;

  updateFilterCount();
  renderCards();
  closeFilterPanel();
}

/* ---- Navbar scroll ---- */
const navbar = document.getElementById('navbar');
window.addEventListener('scroll', () => {
  navbar.classList.toggle('scrolled', window.scrollY > 12);
}, { passive: true });

/* ---- Mobile nav ---- */
document.getElementById('mobileToggle').addEventListener('click', () => {
  document.getElementById('navLinks').classList.toggle('open');
});

/* ---- Search ---- */
document.getElementById('searchBtn').addEventListener('click', () => {
  state.search = document.getElementById('searchInput').value.trim();
  state.type   = document.getElementById('typeFilter').value;
  state.maxPrice = parseInt(document.getElementById('priceFilter').value) || 9999;
  renderCards();
  document.getElementById('properties').scrollIntoView({ behavior: 'smooth', block: 'start' });
});
document.getElementById('searchInput').addEventListener('keydown', e => {
  if (e.key === 'Enter') document.getElementById('searchBtn').click();
});

/* ---- Sort ---- */
document.getElementById('sortSelect').addEventListener('change', e => {
  state.sort = e.target.value;
  renderCards();
});

/* ---- Quick filters ---- */
document.querySelectorAll('.qf-chip').forEach(chip => {
  chip.addEventListener('click', () => {
    const f = chip.dataset.filter;
    chip.classList.toggle('active');
    if (chip.classList.contains('active')) {
      state.quickFilters.add(f);
    } else {
      state.quickFilters.delete(f);
    }
    updateFilterCount();
    renderCards();
    document.getElementById('properties').scrollIntoView({ behavior: 'smooth', block: 'start' });
  });
});

/* ---- Filter panel toggle ---- */
document.getElementById('filterToggle').addEventListener('click', openFilterPanel);
document.getElementById('filterClose').addEventListener('click', closeFilterPanel);
document.getElementById('filterOverlay').addEventListener('click', closeFilterPanel);
document.getElementById('applyFiltersBtn').addEventListener('click', applyPanelFilters);

/* ---- Distance range ---- */
document.getElementById('distanceRange').addEventListener('input', e => {
  const v = parseFloat(e.target.value);
  document.getElementById('distanceDisplay').textContent = `até ${v.toFixed(1)} km`;
});

/* ---- Toggle groups (type/gender) ---- */
document.querySelectorAll('#typeToggleGroup .tg-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('#typeToggleGroup .tg-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    const v = btn.dataset.value;
    if (v === 'shared')   state.shared = true;
    else if (v === 'complete') state.shared = false;
    else state.shared = null;
  });
});
document.querySelectorAll('#genderToggleGroup .tg-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('#genderToggleGroup .tg-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    state.gender = btn.dataset.value;
  });
});

/* ---- Card entrance animation via IntersectionObserver ---- */
function observeCards() {
  if (!('IntersectionObserver' in window)) return;
  const obs = new IntersectionObserver((entries) => {
    entries.forEach(e => {
      if (e.isIntersecting) {
        e.target.style.opacity = '1';
        e.target.style.transform = 'translateY(0)';
        obs.unobserve(e.target);
      }
    });
  }, { threshold: .08 });

  document.querySelectorAll('.hiw-card, .cta-inner').forEach(el => {
    el.style.opacity = '0';
    el.style.transform = 'translateY(24px)';
    el.style.transition = 'opacity .5s ease, transform .5s ease';
    obs.observe(el);
  });
}

/* ---- Init ---- */
document.addEventListener('DOMContentLoaded', () => {
  loadData();
  observeCards();
});
