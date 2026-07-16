import { useEffect, useState } from 'react';
import Header from '../components/Header';
import Alert from '../components/Alert';
import { roommateApi } from '../api/roommateApi';
import { userApi } from '../api/userApi';
import { useAuth } from '../contexts/AuthContext';

const NIVEL_BARULHO = [
  { value: 'SILENCIOSO', label: 'Silencioso' },
  { value: 'MODERADO', label: 'Moderado' },
  { value: 'AGITADO', label: 'Agitado' },
];

function initials(nome) {
  return nome?.split(/\s+/).filter(Boolean).slice(0, 2).map((p) => p[0]).join('').toUpperCase() || 'U';
}

export default function Roommates() {
  const { user } = useAuth();
  const [tab, setTab] = useState('compativeis');

  return <>
    <Header />
    <div className="page-wrap">
      <div className="page-header"><div className="container"><div className="page-header-inner">
        <div><h1>Roommates</h1><p>Encontre colegas compatíveis para dividir moradia perto da sua universidade</p></div>
      </div></div></div>

      <div className="container" style={{ paddingTop: 32, paddingBottom: 60 }}>
        <div className="tabs">
          <button className={`tab-btn ${tab === 'compativeis' ? 'active' : ''}`} onClick={() => setTab('compativeis')}>Compatíveis</button>
          <button className={`tab-btn ${tab === 'meu-perfil' ? 'active' : ''}`} onClick={() => setTab('meu-perfil')}>Meu perfil roommate</button>
          <button className={`tab-btn ${tab === 'solicitacoes' ? 'active' : ''}`} onClick={() => setTab('solicitacoes')}>Solicitações</button>
        </div>

        {tab === 'compativeis' && <AbaCompativeis />}
        {tab === 'meu-perfil' && <AbaMeuPerfil userId={user.id} />}
        {tab === 'solicitacoes' && <AbaSolicitacoes />}
      </div>
    </div>
  </>;
}

function AbaCompativeis() {
  const [candidatos, setCandidatos] = useState([]);
  const [enviados, setEnviados] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    roommateApi.listarCompativeis()
      .then(setCandidatos)
      .catch((err) => setError(err.status === 404
        ? 'Ative seu perfil de roommate na aba "Meu perfil roommate" para ver candidatos compatíveis.'
        : err.message))
      .finally(() => setLoading(false));
  }, []);

  async function solicitar(userId) {
    try {
      await roommateApi.solicitarMatch({ destinatarioId: userId });
      setEnviados((current) => ({ ...current, [userId]: true }));
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <p>Carregando candidatos...</p>;
  return <>
    <Alert>{error}</Alert>
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 16 }}>
      {candidatos.map((c) => <div className="roommate-card" key={c.userId}>
        <div className="rm-header">
          <div className="rm-avatar">{initials(c.nome)}</div>
          <div><div className="rm-name">{c.nome}</div></div>
          <div className="rm-match">{c.scoreCompatibilidade}%<br /><span>match</span></div>
        </div>
        <div className="rm-tags">
          {c.orcamentoMax != null && <span className="rm-tag">Até R$ {Number(c.orcamentoMax).toFixed(0)}</span>}
          {c.nivelBarulhoPreferido && <span className="rm-tag">{NIVEL_BARULHO.find((n) => n.value === c.nivelBarulhoPreferido)?.label ?? c.nivelBarulhoPreferido}</span>}
          {c.jaPossuiCasa && <span className="rm-tag"><i className="fa-solid fa-house" /> Já tem moradia</span>}
        </div>
        {c.descricao && <p style={{ fontSize: 13, color: 'var(--text-2)', lineHeight: 1.55 }}>"{c.descricao}"</p>}
        <div className="rm-actions">
          <button
            className="btn-sm btn-primary"
            style={{ flex: 1, ...(enviados[c.userId] ? { background: 'rgba(34,197,94,.15)', color: '#22c55e', boxShadow: 'none' } : {}) }}
            disabled={!!enviados[c.userId]}
            onClick={() => solicitar(c.userId)}
          >
            <i className={`fa-solid ${enviados[c.userId] ? 'fa-check' : 'fa-heart'}`} style={{ marginRight: 6 }} />
            {enviados[c.userId] ? 'Solicitado!' : 'Solicitar match'}
          </button>
        </div>
      </div>)}
    </div>
    {!candidatos.length && !error && <p style={{ color: 'var(--text-2)' }}>Nenhum candidato compatível encontrado por enquanto.</p>}
  </>;
}

function AbaMeuPerfil({ userId }) {
  const [form, setForm] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  useEffect(() => {
    roommateApi.meuPerfil()
      .then(setForm)
      .catch((err) => setMessage({ type: 'error', text: err.message }))
      .finally(() => setLoading(false));
  }, []);

  async function salvar() {
    setSaving(true); setMessage({ type: '', text: '' });
    try {
      await Promise.all([
        roommateApi.ativarPerfil({
          descricao: form.descricao,
          orcamentoMax: form.orcamentoMax === '' ? null : form.orcamentoMax,
          dataEntradaDesejada: form.dataEntradaDesejada || null,
          periodoMinMeses: form.periodoMinMeses === '' ? null : form.periodoMinMeses,
          jaPossuiCasa: form.jaPossuiCasa,
          perfilVisivel: form.perfilVisivel,
        }),
        roommateApi.salvarPreferencias(userId, {
          horarioDorme: form.horarioDorme || null,
          horarioAcorda: form.horarioAcorda || null,
          nivelBarulho: form.nivelBarulhoPreferido || null,
          fumante: form.fumante,
          aceitaPets: form.aceitaPets,
        }),
      ]);
      setMessage({ type: 'success', text: 'Perfil de roommate atualizado com sucesso.' });
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    } finally { setSaving(false); }
  }

  if (loading || !form) return <p>Carregando perfil...</p>;
  return <div style={{ maxWidth: 600 }}>
    <div className="card-section">
      <div className="card-section-title">Meu perfil de roommate</div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
        <div className="alert alert-info"><i className="fa-solid fa-users" /><span>Ativar a visibilidade exibe seu perfil para outros estudantes na aba "Compatíveis".</span></div>
        <Alert type={message.type === 'success' ? 'success' : 'error'}>{message.text}</Alert>

        <div className="form-group">
          <label className="form-label">Apresentação</label>
          <textarea className="form-textarea" value={form.descricao || ''} onChange={(e) => setForm({ ...form, descricao: e.target.value })} placeholder="Escreva um pouco sobre você, seus hábitos e o que busca em um roommate..." />
        </div>

        <div className="form-group">
          <label className="form-label">Orçamento máximo (R$)</label>
          <input type="number" min="0" className="form-input" value={form.orcamentoMax ?? ''} onChange={(e) => setForm({ ...form, orcamentoMax: e.target.value })} />
        </div>

        <div className="form-group">
          <label className="form-label">Data de entrada desejada</label>
          <input type="date" className="form-input" value={form.dataEntradaDesejada || ''} onChange={(e) => setForm({ ...form, dataEntradaDesejada: e.target.value })} />
        </div>

        <div className="form-group">
          <label className="form-label">Período mínimo (meses)</label>
          <input type="number" min="0" className="form-input" value={form.periodoMinMeses ?? ''} onChange={(e) => setForm({ ...form, periodoMinMeses: e.target.value })} />
        </div>

        <div className="form-group">
          <label className="form-label">Horário que costuma dormir</label>
          <input type="time" className="form-input" value={form.horarioDorme || ''} onChange={(e) => setForm({ ...form, horarioDorme: e.target.value })} />
        </div>
        <div className="form-group">
          <label className="form-label">Horário que costuma acordar</label>
          <input type="time" className="form-input" value={form.horarioAcorda || ''} onChange={(e) => setForm({ ...form, horarioAcorda: e.target.value })} />
        </div>

        <div className="form-group">
          <label className="form-label">Nível de barulho preferido</label>
          <div className="toggle-group">
            {NIVEL_BARULHO.map((n) => (
              <button key={n.value} type="button" className={`tg-btn ${form.nivelBarulhoPreferido === n.value ? 'active' : ''}`} onClick={() => setForm({ ...form, nivelBarulhoPreferido: n.value })}>{n.label}</button>
            ))}
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Sobre você</label>
          <div className="fp-checks">
            <label className="fp-check-item"><input type="checkbox" checked={!form.fumante} onChange={(e) => setForm({ ...form, fumante: !e.target.checked })} /><span className="check-box" /><i className="fa-solid fa-ban-smoking" /> Não fumo</label>
            <label className="fp-check-item"><input type="checkbox" checked={form.aceitaPets} onChange={(e) => setForm({ ...form, aceitaPets: e.target.checked })} /><span className="check-box" /><i className="fa-solid fa-paw" /> Aceito/tenho pet</label>
            <label className="fp-check-item"><input type="checkbox" checked={form.jaPossuiCasa} onChange={(e) => setForm({ ...form, jaPossuiCasa: e.target.checked })} /><span className="check-box" /><i className="fa-solid fa-house" /> Já possuo moradia (procuro alguém para dividir)</label>
            <label className="fp-check-item"><input type="checkbox" checked={form.perfilVisivel} onChange={(e) => setForm({ ...form, perfilVisivel: e.target.checked })} /><span className="check-box" /><i className="fa-solid fa-eye" /> Visível para outros estudantes</label>
          </div>
        </div>

        <button className="btn-full" disabled={saving} onClick={salvar}>{saving ? 'Salvando...' : 'Salvar perfil'}</button>
      </div>
    </div>
  </div>;
}

function AbaSolicitacoes() {
  const [pendentes, setPendentes] = useState([]);
  const [nomes, setNomes] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    roommateApi.listarPendentes()
      .then(async (matches) => {
        setPendentes(matches);
        const entries = await Promise.all(matches.map(async (m) => {
          try {
            const perfil = await userApi.publico(m.solicitanteId);
            return [m.solicitanteId, perfil.nome];
          } catch {
            return [m.solicitanteId, null];
          }
        }));
        setNomes(Object.fromEntries(entries));
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  async function responder(id, status) {
    try {
      await roommateApi.responderMatch(id, status);
      setPendentes((current) => current.filter((m) => m.id !== id));
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <p>Carregando solicitações...</p>;
  return <div style={{ maxWidth: 600, display: 'flex', flexDirection: 'column', gap: 12 }}>
    <Alert>{error}</Alert>
    {pendentes.map((m) => <div className="card-section" style={{ marginBottom: 0 }} key={m.id}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
        <div className="rm-avatar">{initials(nomes[m.solicitanteId])}</div>
        <div style={{ flex: 1 }}>
          <div style={{ fontWeight: 700, fontSize: 15, color: 'var(--text-1)' }}>{nomes[m.solicitanteId] || 'Usuário'}</div>
          {m.mensagemInicial && <div style={{ fontSize: 13, color: 'var(--text-2)', marginTop: 4 }}>"{m.mensagemInicial}"</div>}
        </div>
      </div>
      <div style={{ display: 'flex', gap: 8, marginTop: 16, justifyContent: 'flex-end' }}>
        <button className="btn-sm btn-danger" onClick={() => responder(m.id, 'RECUSADO')}><i className="fa-solid fa-xmark" style={{ marginRight: 4 }} />Recusar</button>
        <button className="btn-sm btn-primary" onClick={() => responder(m.id, 'ACEITO')}><i className="fa-solid fa-check" style={{ marginRight: 4 }} />Aceitar match</button>
      </div>
    </div>)}
    {!pendentes.length && !error && <p style={{ color: 'var(--text-2)' }}>Nenhuma solicitação pendente no momento.</p>}
  </div>;
}
