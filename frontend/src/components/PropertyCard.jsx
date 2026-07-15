export default function PropertyCard({ property }) {
  const genderLabel = property.gender === 'female' ? 'Feminino' : property.gender === 'male' ? 'Masculino' : 'Todos';
  const genderClass = property.gender === 'female' ? 'g-female' : property.gender === 'male' ? 'g-male' : 'g-any';

  return (
    <article className="prop-card">
      <div className="card-img-wrap">
        <img className="card-img" src={property.image} alt={property.title} />
        <span className="badge-type">{property.type}</span>
        <span className="badge-dist"><i className="fa-solid fa-location-dot" /> {property.distance} km</span>
        {property.petFriendly && <span className="badge-pet" title="Aceita pets"><i className="fa-solid fa-paw" /></span>}
      </div>

      <div className="card-body">
        <div className="card-meta">
          <span className="card-travel"><i className="fa-solid fa-person-walking" /> {property.walking} min da universidade</span>
          <span className={`card-gender ${genderClass}`}>{genderLabel}</span>
        </div>

        <h3 className="card-title">{property.title}</h3>
        <p className="card-addr"><i className="fa-solid fa-location-dot" /> {property.address}</p>

        <div className="card-feats">
          {property.furnished && <span className="feat-tag"><i className="fa-solid fa-couch" /> Mobiliado</span>}
          {property.wifi && <span className="feat-tag"><i className="fa-solid fa-wifi" /> Wi-Fi</span>}
          {property.parking && <span className="feat-tag"><i className="fa-solid fa-square-parking" /> Garagem</span>}
        </div>

        <div className="card-footer">
          <div className="card-price">
            <span className="price-val">R$ {property.price.toLocaleString('pt-BR')}</span>
            <span className="price-per">/mês</span>
          </div>
          <button className="btn-details" type="button" onClick={() => window.alert('Detalhes mockados: esta tela será integrada ao backend depois.')}>Ver detalhes <i className="fa-solid fa-arrow-right" /></button>
        </div>
      </div>
    </article>
  );
}
