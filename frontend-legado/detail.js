/* =============================================
   CampusLiving — Detail Page Script
   ============================================= */

let property = null;
let lbIndex = 0;

/* ---- Get ID from URL ---- */
function getPropertyId() {
  const params = new URLSearchParams(window.location.search);
  return parseInt(params.get('id'));
}

/* ---- Load data ---- */
async function loadDetail() {
  const id = getPropertyId();
  if (!id) { window.location.href = 'index.html'; return; }

  try {
    const res  = await fetch('data.json');
    const data = await res.json();
    property = data.properties.find(p => p.id === id);
    if (!property) { window.location.href = 'index.html'; return; }
    document.title = `${property.title} — CampusLiving`;
    renderGallery();
    renderMain();
    renderSidebar();
  } catch (e) {
    console.error('Erro ao carregar imóvel:', e);
  }
}

/* ---- Gallery ---- */
function renderGallery() {
  const gallery = document.getElementById('gallery');
  const imgs = property.images;

  if (imgs.length === 0) return;

  let html = `
    <div class="gallery-main" onclick="openLightbox(0)">
      <img src="${imgs[0]}" alt="${property.title} — foto principal">
    </div>
  `;

  if (imgs.length > 1) {
    html += `<div class="gallery-thumb" onclick="openLightbox(1)">
      <img src="${imgs[1]}" alt="${property.title} — foto 2">
    </div>`;
  }
  if (imgs.length > 2) {
    const remaining = imgs.length - 2;
    html += `<div class="gallery-thumb gallery-more" data-more="+${remaining} fotos" onclick="openLightbox(2)">
      <img src="${imgs[2]}" alt="${property.title} — foto 3">
    </div>`;
  }

  gallery.innerHTML = html;
}

/* ---- Lightbox ---- */
function openLightbox(index) {
  lbIndex = index;
  updateLightboxImg();
  document.getElementById('lightbox').classList.add('open');
  document.body.style.overflow = 'hidden';
}
function closeLightbox() {
  document.getElementById('lightbox').classList.remove('open');
  document.body.style.overflow = '';
}
function updateLightboxImg() {
  const imgs = property.images;
  document.getElementById('lbImg').src = imgs[lbIndex];
  document.getElementById('lbCounter').textContent = `${lbIndex + 1} / ${imgs.length}`;
}

document.getElementById('lbClose').addEventListener('click', closeLightbox);
document.getElementById('lightbox').addEventListener('click', e => {
  if (e.target === document.getElementById('lightbox')) closeLightbox();
});
document.getElementById('lbPrev').addEventListener('click', (e) => {
  e.stopPropagation();
  lbIndex = (lbIndex - 1 + property.images.length) % property.images.length;
  updateLightboxImg();
});
document.getElementById('lbNext').addEventListener('click', (e) => {
  e.stopPropagation();
  lbIndex = (lbIndex + 1) % property.images.length;
  updateLightboxImg();
});
document.addEventListener('keydown', e => {
  if (!document.getElementById('lightbox').classList.contains('open')) return;
  if (e.key === 'Escape')    closeLightbox();
  if (e.key === 'ArrowLeft') { lbIndex = (lbIndex - 1 + property.images.length) % property.images.length; updateLightboxImg(); }
  if (e.key === 'ArrowRight') { lbIndex = (lbIndex + 1) % property.images.length; updateLightboxImg(); }
});

/* ---- Main content ---- */
function renderMain() {
  const el = document.getElementById('detailMain');
  const p  = property;

  const genderLabel = { any: 'Qualquer gênero', female: 'Somente feminino', male: 'Somente masculino' };
  const availClass  = p.available ? 'avail-yes' : 'avail-no';
  const availLabel  = p.available ? 'Disponível' : 'Indisponível';

  const features = [
    { key: 'pet_friendly',     label: 'Pet friendly',     icon: 'fa-paw' },
    { key: 'smoking',          label: 'Aceita fumantes',  icon: 'fa-smoking' },
    { key: 'furnished',        label: 'Mobiliado',        icon: 'fa-couch' },
    { key: 'wifi',             label: 'Wi-Fi incluso',    icon: 'fa-wifi' },
    { key: 'parking',          label: 'Estacionamento',   icon: 'fa-square-parking' },
    { key: 'air_conditioning', label: 'Ar-condicionado',  icon: 'fa-snowflake' },
    { key: 'laundry',          label: 'Lavanderia',       icon: 'fa-shirt' },
  ];

  const featsHtml = features.map(f => `
    <div class="feature-item ${p.features[f.key] ? 'yes' : 'no'}">
      <i class="fa-solid ${f.icon}"></i>
      ${f.label}
    </div>
  `).join('');

  const travelItems = [
    { mode: 'A pé',    icon: 'fa-person-walking', time: p.travel_time.walking },
    { mode: 'Ônibus',  icon: 'fa-bus',             time: p.travel_time.bus    },
    { mode: 'Carro',   icon: 'fa-car',             time: p.travel_time.car    },
    { mode: 'Moto',    icon: 'fa-motorcycle',      time: p.travel_time.bike   },
  ];
  const travelHtml = travelItems.map(t => `
    <div class="travel-item">
      <div class="travel-icon"><i class="fa-solid ${t.icon}"></i></div>
      <div class="travel-info">
        <span class="travel-time">${t.time} min</span>
        <span class="travel-mode">${t.mode} até a universidade</span>
      </div>
    </div>
  `).join('');

  const nearbyCategories = [
    { key: 'universities', label: 'Universidades', icon: 'fa-graduation-cap' },
    { key: 'markets',      label: 'Mercados',      icon: 'fa-basket-shopping' },
    { key: 'gyms',         label: 'Academias',     icon: 'fa-dumbbell' },
    { key: 'bus_stops',    label: 'Pontos de ônibus', icon: 'fa-bus' },
  ];
  const nearbyHtml = nearbyCategories.map(cat => {
    const items = p.nearby[cat.key];
    if (!items || !items.length) return '';
    const itemsHtml = items.map(item => `
      <div class="nearby-item">
        <span class="nearby-name">${item.name}</span>
        <div class="nearby-dist-row">
          <span class="nearby-dist">${item.distance} km</span>
          <span class="nearby-walk">
            <i class="fa-solid fa-person-walking"></i> ${item.walking} min
          </span>
        </div>
      </div>
    `).join('');
    return `
      <div class="nearby-category">
        <p class="nearby-cat-title">
          <i class="fa-solid ${cat.icon}"></i> ${cat.label}
        </p>
        <div class="nearby-items">${itemsHtml}</div>
      </div>
    `;
  }).join('');

  const floor = p.floor === 0 ? 'Térreo' : `${p.floor}º andar`;

  el.innerHTML = `
    <div class="detail-header">
      <div class="detail-type-row">
        <span class="detail-type-tag">${p.typeLabel}</span>
        <span class="detail-avail ${availClass}">
          <span class="avail-dot"></span> ${availLabel}
        </span>
        <span class="card-gender ${p.gender === 'female' ? 'g-female' : p.gender === 'male' ? 'g-male' : 'g-any'}" style="font-size:11px">
          ${genderLabel[p.gender]}
        </span>
      </div>
      <h1 class="detail-title">${p.title}</h1>
      <p class="detail-address">
        <i class="fa-solid fa-map-pin"></i>
        ${p.address}
      </p>
    </div>

    <div class="specs-row">
      <div class="spec-item">
        <i class="fa-solid fa-bed spec-icon"></i>
        <span class="spec-val">${p.rooms}</span>
        <span class="spec-lbl">Quarto${p.rooms > 1 ? 's' : ''}</span>
      </div>
      <div class="spec-item">
        <i class="fa-solid fa-bath spec-icon"></i>
        <span class="spec-val">${p.bathrooms}</span>
        <span class="spec-lbl">Banheiro${p.bathrooms > 1 ? 's' : ''}</span>
      </div>
      <div class="spec-item">
        <i class="fa-solid fa-vector-square spec-icon"></i>
        <span class="spec-val">${p.area} m²</span>
        <span class="spec-lbl">Área</span>
      </div>
      <div class="spec-item">
        <i class="fa-solid fa-building spec-icon"></i>
        <span class="spec-val">${floor}</span>
        <span class="spec-lbl">Andar</span>
      </div>
    </div>

    <div class="detail-block">
      <h2 class="detail-block-title">Sobre o imóvel</h2>
      <p class="description-text">${p.description}</p>
    </div>

    <div class="detail-block">
      <h2 class="detail-block-title">Comodidades</h2>
      <div class="features-grid">${featsHtml}</div>
    </div>

    <div class="detail-block">
      <h2 class="detail-block-title">Tempo de deslocamento até a universidade</h2>
      <div class="travel-grid">${travelHtml}</div>
    </div>

    <div class="detail-block">
      <h2 class="detail-block-title">Localização e proximidades</h2>
      ${nearbyHtml}
    </div>
  `;
}

/* ---- Sidebar ---- */
function renderSidebar() {
  const el = document.getElementById('sidebarCard');
  const p  = property;

  const memberYear = p.landlord.member_since;
  const stars = '★'.repeat(Math.round(p.landlord.rating)) + '☆'.repeat(5 - Math.round(p.landlord.rating));

  el.innerHTML = `
    <div class="sidebar-price-block">
      <div class="sidebar-price">
        <span class="price-val">R$ ${p.price.toLocaleString('pt-BR')}</span>
        <span class="price-per">/mês</span>
      </div>
      <p class="sidebar-tagline">Valor total mensal · sem taxa de intermediação</p>
    </div>
    <div class="sidebar-body">
      <div class="sidebar-landlord">
        <img src="${p.landlord.avatar}" alt="${p.landlord.name}" class="landlord-avatar">
        <div>
          <p class="landlord-name">${p.landlord.name}</p>
          <p class="landlord-type">${p.landlord.typeLabel}</p>
          <div class="landlord-rating">
            <i class="fa-solid fa-star"></i>
            ${p.landlord.rating.toFixed(1)}
            <span style="color:var(--text-3);font-weight:400">(${p.landlord.reviews} avaliações)</span>
          </div>
          <p style="font-size:11px;color:var(--text-3);margin-top:2px">Membro desde ${memberYear}</p>
        </div>
      </div>

      <a href="tel:${p.landlord.phone.replace(/\D/g,'')}" class="sidebar-phone">
        <div class="phone-icon"><i class="fa-solid fa-phone"></i></div>
        <div>
          <p class="phone-label">Telefone / WhatsApp</p>
          <p class="phone-number">${p.landlord.phone}</p>
        </div>
      </a>

      ${p.available ? `
        <a href="mensagens.html" class="btn-contact" style="text-decoration:none;display:flex;align-items:center;gap:8px;justify-content:center">
          <i class="fa-brands fa-whatsapp"></i> Enviar mensagem
        </a>
        <a href="mensagens.html" class="btn-schedule" style="text-decoration:none;display:flex;align-items:center;gap:8px;justify-content:center">
          <i class="fa-solid fa-calendar-plus"></i> Agendar visita
        </a>
      ` : `
        <button class="btn-contact" disabled style="background:var(--text-3);cursor:not-allowed;">
          <i class="fa-solid fa-ban"></i> Imóvel indisponível
        </button>
      `}

      <p class="sidebar-note">
        <i class="fa-solid fa-shield-check" style="color:var(--forest)"></i>
        Perfil verificado pela CampusLiving
      </p>
    </div>
  `;
}

/* ---- Navbar scroll ---- */
window.addEventListener('scroll', () => {
  document.getElementById('navbar').classList.toggle('scrolled', window.scrollY > 12);
}, { passive: true });

/* ---- Mobile nav ---- */
document.getElementById('mobileToggle').addEventListener('click', () => {
  document.getElementById('navLinks').classList.toggle('open');
});

/* ---- Init ---- */
document.addEventListener('DOMContentLoaded', loadDetail);
