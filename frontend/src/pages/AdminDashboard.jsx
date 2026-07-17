import { useEffect, useMemo, useState } from 'react';
import Header from '../components/Header';
import { adminApi } from '../api/adminApi';
import { MOTIVO_LABELS } from '../utils/denuncia';

const TIPO_CONTA_LABELS = { ESTUDANTE: 'Estudante', LOCADOR: 'Locador', MISTO: 'Misto', ADMIN: 'Admin' };
const STATUS_LABELS = { PENDENTE: 'Pendente', EM_ANALISE: 'Em análise', RESOLVIDA: 'Resolvida', REJEITADA: 'Rejeitada/Arquivada' };
const PERIODOS = [7, 30, 90];

function formatarData(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('pt-BR');
}

export default function AdminDashboard() {
  const [tab, setTab] = useState('visao-geral');

  const [usuarios, setUsuarios] = useState([]);
  const [usuariosLoading, setUsuariosLoading] = useState(true);
  const [usuariosErro, setUsuariosErro] = useState(null);

  const [denuncias, setDenuncias] = useState([]);
  const [denunciasLoading, setDenunciasLoading] = useState(true);
  const [denunciasErro, setDenunciasErro] = useState(null);
  const [moderandoId, setModerandoId] = useState(null);

  const [verificacoes, setVerificacoes] = useState([]);
  const [verificacoesLoading, setVerificacoesLoading] = useState(true);
  const [verificacoesErro, setVerificacoesErro] = useState(null);
  const [verificandoId, setVerificandoId] = useState(null);

  const [dias, setDias] = useState(30);
  const [relatorio, setRelatorio] = useState(null);
  const [relatorioLoading, setRelatorioLoading] = useState(false);

  useEffect(() => {
    adminApi.listarUsuarios()
      .then(setUsuarios)
      .catch(() => setUsuariosErro('Não foi possível carregar os usuários.'))
      .finally(() => setUsuariosLoading(false));
  }, []);

  useEffect(() => {
    adminApi.listarDenuncias()
      .then(setDenuncias)
      .catch(() => setDenunciasErro('Não foi possível carregar as denúncias.'))
      .finally(() => setDenunciasLoading(false));
  }, []);

  useEffect(() => {
    adminApi.listarVerificacoes()
      .then(setVerificacoes)
      .catch(() => setVerificacoesErro('Não foi possível carregar as verificações pendentes.'))
      .finally(() => setVerificacoesLoading(false));
  }, []);

  useEffect(() => {
    setRelatorioLoading(true);
    adminApi.relatorio(dias).then(setRelatorio).catch(() => {}).finally(() => setRelatorioLoading(false));
  }, [dias]);

  const usuariosPorId = useMemo(() => new Map(usuarios.map((u) => [u.id, u])), [usuarios]);
  const denunciasPendentes = useMemo(() => denuncias.filter((d) => d.status === 'PENDENTE' || d.status === 'EM_ANALISE'), [denuncias]);
  const cadastrosPorTipo = useMemo(() => {
    const contagem = {};
    usuarios.forEach((u) => { contagem[u.tipoConta] = (contagem[u.tipoConta] ?? 0) + 1; });
    return Object.entries(contagem).sort((a, b) => b[1] - a[1]);
  }, [usuarios]);

  async function resolverVerificacao(verificacao, aprovado) {
    setVerificandoId(verificacao.id);
    try {
      await adminApi.verificarLocador(verificacao.userId, aprovado);
      setVerificacoes((current) => current.filter((v) => v.id !== verificacao.id));
      setUsuarios((current) => current.map((u) => u.id === verificacao.userId ? { ...u, verificado: aprovado } : u));
    } catch (err) {
      setVerificacoesErro(err.message);
    } finally {
      setVerificandoId(null);
    }
  }

  async function moderar(denuncia, acao) {
    setModerandoId(denuncia.id);
    try {
      const atualizada = await adminApi.moderarDenuncia(denuncia.id, acao);
      setDenuncias((current) => current.map((d) => d.id === denuncia.id ? atualizada : d));
    } catch (err) {
      setDenunciasErro(err.message);
    } finally {
      setModerandoId(null);
    }
  }

  return (
    <>
      <Header />
      <div className="page-wrap">
        <div className="container" style={{ paddingTop: 32, paddingBottom: 60 }}>
          <div style={{ marginBottom: 28 }}>
            <h2 style={{ fontFamily: 'var(--font-display)', fontSize: 24, fontWeight: 500, color: 'var(--text-1)' }}>Painel Administrativo</h2>
            <p style={{ fontSize: 14, color: 'var(--text-2)', marginTop: 4 }}>Visão geral da plataforma CampusLiving</p>
          </div>

          <div className="stats-strip">
            <div className="stat-card"><span className="stat-card-label">Usuários totais</span><span className="stat-card-val clr-primary">{relatorio?.totalUsuarios ?? '—'}</span></div>
            <div className="stat-card"><span className="stat-card-label">Anúncios ativos</span><span className="stat-card-val clr-green">{relatorio?.totalAnunciosAtivos ?? '—'}</span></div>
            <div className="stat-card"><span className="stat-card-label">Locadores verificados</span><span className="stat-card-val clr-accent">{relatorio?.totalLocadoresVerificados ?? '—'}</span></div>
            <div className="stat-card"><span className="stat-card-label">Denúncias pendentes</span><span className="stat-card-val" style={{ color: '#fbbf24' }}>{relatorio?.totalDenunciasPendentes ?? '—'}</span></div>
          </div>

          <div className="tabs">
            <button className={`tab-btn ${tab === 'visao-geral' ? 'active' : ''}`} type="button" onClick={() => setTab('visao-geral')}>Usuários</button>
            <button className={`tab-btn ${tab === 'verificacoes' ? 'active' : ''}`} type="button" onClick={() => setTab('verificacoes')}>Verificações {verificacoes.length > 0 && `(${verificacoes.length})`}</button>
            <button className={`tab-btn ${tab === 'denuncias' ? 'active' : ''}`} type="button" onClick={() => setTab('denuncias')}>Denúncias {denunciasPendentes.length > 0 && `(${denunciasPendentes.length})`}</button>
            <button className={`tab-btn ${tab === 'relatorios' ? 'active' : ''}`} type="button" onClick={() => setTab('relatorios')}>Relatórios</button>
          </div>

          <div style={{ marginTop: 16 }}>
            {tab === 'visao-geral' && (
              <div className="card-section">
                <div className="card-section-title">Usuários ({usuarios.length})</div>
                {usuariosLoading && <p style={{ color: 'var(--text-3)' }}>Carregando...</p>}
                {usuariosErro && <div className="alert alert-info"><i className="fa-solid fa-triangle-exclamation" /><span>{usuariosErro}</span></div>}
                {!usuariosLoading && !usuariosErro && (
                  <div style={{ overflowX: 'auto' }}>
                    <table className="admin-table">
                      <thead><tr><th>Usuário</th><th>Tipo</th><th>Cadastro</th><th>Status</th></tr></thead>
                      <tbody>
                        {usuarios.map((u) => (
                          <tr key={u.id}>
                            <td>
                              <div className="table-user">
                                <div className="table-avatar">{u.nome?.charAt(0)?.toUpperCase() ?? '?'}</div>
                                <div><div className="table-name">{u.nome}{u.verificado && <i className="fa-solid fa-circle-check" style={{ color: 'var(--forest, #3b6e5c)', marginLeft: 6, fontSize: 12 }} title="Verificado" />}</div><div className="table-email">{u.email}</div></div>
                              </div>
                            </td>
                            <td><span className="chip">{TIPO_CONTA_LABELS[u.tipoConta] ?? u.tipoConta}</span></td>
                            <td>{formatarData(u.dataCriacao)}</td>
                            <td><span className={`chip ${u.ativo ? 'chip-approved' : 'chip-pending'}`}><i className="fa-solid fa-circle" style={{ fontSize: 7 }} /> {u.ativo ? 'Ativo' : 'Inativo'}</span></td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            )}

            {tab === 'verificacoes' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                {verificacoesLoading && <p style={{ color: 'var(--text-3)' }}>Carregando...</p>}
                {verificacoesErro && <div className="alert alert-info"><i className="fa-solid fa-triangle-exclamation" /><span>{verificacoesErro}</span></div>}
                {!verificacoesLoading && !verificacoesErro && !verificacoes.length && (
                  <div className="no-results"><div className="no-results-emoji">✅</div><h3>Nenhuma verificação pendente</h3></div>
                )}
                {verificacoes.map((v) => {
                  const usuario = usuariosPorId.get(v.userId);
                  return (
                    <div key={v.id} className="card-section" style={{ marginBottom: 0 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 14, flexWrap: 'wrap' }}>
                        <div className="table-avatar" style={{ width: 48, height: 48, fontSize: 16 }}>{usuario?.nome?.charAt(0)?.toUpperCase() ?? '?'}</div>
                        <div style={{ flex: 1 }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                            <span style={{ fontWeight: 700, fontSize: 15, color: 'var(--text-1)' }}>{usuario?.nome ?? 'Usuário'}</span>
                            <span className="chip chip-pending">Pendente</span>
                          </div>
                          <div style={{ fontSize: 13, color: 'var(--text-3)', marginTop: 2 }}>{usuario?.email} · solicitado em {formatarData(v.dataCriacao)}</div>
                          <a href={v.documentoUrl} target="_blank" rel="noreferrer" style={{ fontSize: 12.5, color: 'var(--primary)' }}><i className="fa-solid fa-file" style={{ marginRight: 4 }} />Ver documento enviado</a>
                        </div>
                        <div style={{ display: 'flex', gap: 8, flexShrink: 0 }}>
                          <button className="btn-sm btn-outline" type="button" disabled={verificandoId === v.id} onClick={() => resolverVerificacao(v, false)}>Reprovar</button>
                          <button className="btn-sm btn-primary" type="button" disabled={verificandoId === v.id} onClick={() => resolverVerificacao(v, true)}>
                            <i className="fa-solid fa-check" style={{ marginRight: 4 }} />{verificandoId === v.id ? 'Aprovando...' : 'Aprovar'}
                          </button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}

            {tab === 'denuncias' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                {denunciasLoading && <p style={{ color: 'var(--text-3)' }}>Carregando...</p>}
                {denunciasErro && <div className="alert alert-info"><i className="fa-solid fa-triangle-exclamation" /><span>{denunciasErro}</span></div>}
                {!denunciasLoading && !denuncias.length && (
                  <div className="no-results"><div className="no-results-emoji">🚩</div><h3>Nenhuma denúncia registrada</h3></div>
                )}
                {denuncias.map((d) => (
                  <div key={d.id} className="card-section" style={{ marginBottom: 0, borderLeft: d.status === 'PENDENTE' ? '3px solid #f87171' : '3px solid var(--border)' }}>
                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: 14 }}>
                      <div style={{ width: 40, height: 40, borderRadius: 'var(--r-sm)', background: 'rgba(248,113,113,.1)', color: '#f87171', display: 'grid', placeItems: 'center', fontSize: 18, flexShrink: 0 }}><i className="fa-solid fa-flag" /></div>
                      <div style={{ flex: 1 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap', marginBottom: 6 }}>
                          <span style={{ fontWeight: 700, color: 'var(--text-1)' }}>{MOTIVO_LABELS[d.motivo] ?? d.motivo} — {d.tipoAlvo === 'ANUNCIO' ? 'Anúncio' : 'Usuário'}</span>
                          <span className={`chip ${d.status === 'PENDENTE' ? 'chip-pending' : d.status === 'RESOLVIDA' ? 'chip-approved' : ''}`}>{STATUS_LABELS[d.status] ?? d.status}</span>
                          {d.contadorDenuncias > 1 && <span className="chip">{d.contadorDenuncias}x reportado</span>}
                        </div>
                        <div style={{ fontSize: 13, color: 'var(--text-2)', lineHeight: 1.5 }}>Denunciado por <strong>{d.denuncianteNome}</strong>: "{d.descricao}"</div>
                        <div style={{ fontSize: 12, color: 'var(--text-3)', marginTop: 6 }}>Reportado em {formatarData(d.dataCriacao)}</div>
                      </div>
                    </div>
                    {(d.status === 'PENDENTE' || d.status === 'EM_ANALISE') && (
                      <div style={{ display: 'flex', gap: 8, marginTop: 14, justifyContent: 'flex-end' }}>
                        <button className="btn-sm btn-outline" type="button" disabled={moderandoId === d.id} onClick={() => moderar(d, 'ARQUIVAR')}>Arquivar</button>
                        {d.tipoAlvo === 'ANUNCIO' && (
                          <button className="btn-sm btn-danger" type="button" disabled={moderandoId === d.id} onClick={() => moderar(d, 'BANIR_ANUNCIO')}>
                            <i className="fa-solid fa-ban" style={{ marginRight: 4 }} />Banir anúncio
                          </button>
                        )}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}

            {tab === 'relatorios' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                  {PERIODOS.map((p) => (
                    <button key={p} className={`tab-btn ${dias === p ? 'active' : ''}`} type="button" onClick={() => setDias(p)}>{p} dias</button>
                  ))}
                  <a className="btn-sm btn-outline" href={adminApi.relatorioCsvUrl(dias)} download style={{ textDecoration: 'none', marginLeft: 'auto' }}>
                    <i className="fa-solid fa-file-csv" style={{ marginRight: 6 }} />Exportar CSV
                  </a>
                </div>

                {relatorioLoading && <p style={{ color: 'var(--text-3)' }}>Carregando...</p>}

                {relatorio && (
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                    <div className="card-section" style={{ marginBottom: 0 }}>
                      <div className="card-section-title">Cadastros por tipo</div>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginTop: 4 }}>
                        {cadastrosPorTipo.map(([tipo, total]) => (
                          <div key={tipo} style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <span style={{ width: 90, fontSize: 13, color: 'var(--text-2)' }}>{TIPO_CONTA_LABELS[tipo] ?? tipo}</span>
                            <div style={{ flex: 1, height: 10, background: 'var(--bg-2)', borderRadius: 5, overflow: 'hidden' }}>
                              <div style={{ width: `${(total / usuarios.length) * 100}%`, height: '100%', background: 'var(--primary)', borderRadius: 5 }} />
                            </div>
                            <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-1)', width: 30, textAlign: 'right' }}>{total}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                    <div className="card-section" style={{ marginBottom: 0 }}>
                      <div className="card-section-title">Últimos {relatorio.periodoDias} dias</div>
                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
                        <div style={{ padding: 14, background: 'var(--bg-2)', borderRadius: 'var(--r-sm)', textAlign: 'center' }}>
                          <div style={{ fontFamily: 'var(--font-display)', fontSize: 22, fontWeight: 700, color: 'var(--primary)' }}>{relatorio.novosCadastrosPeriodo}</div>
                          <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 2 }}>Novos cadastros</div>
                        </div>
                        <div style={{ padding: 14, background: 'var(--bg-2)', borderRadius: 'var(--r-sm)', textAlign: 'center' }}>
                          <div style={{ fontFamily: 'var(--font-display)', fontSize: 22, fontWeight: 700, color: '#22c55e' }}>{relatorio.anunciosPublicadosPeriodo}</div>
                          <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 2 }}>Anúncios publicados</div>
                        </div>
                        <div style={{ padding: 14, background: 'var(--bg-2)', borderRadius: 'var(--r-sm)', textAlign: 'center' }}>
                          <div style={{ fontFamily: 'var(--font-display)', fontSize: 22, fontWeight: 700, color: 'var(--accent)' }}>{relatorio.denunciasPeriodo}</div>
                          <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 2 }}>Denúncias</div>
                        </div>
                        <div style={{ padding: 14, background: 'var(--bg-2)', borderRadius: 'var(--r-sm)', textAlign: 'center' }}>
                          <div style={{ fontFamily: 'var(--font-display)', fontSize: 22, fontWeight: 700, color: 'var(--primary)' }}>{relatorio.totalDenunciasResolvidas}</div>
                          <div style={{ fontSize: 11, color: 'var(--text-3)', marginTop: 2 }}>Denúncias resolvidas</div>
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
