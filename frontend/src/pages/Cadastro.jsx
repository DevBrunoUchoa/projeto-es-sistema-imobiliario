import { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthVisual from '../components/AuthVisual';
import Alert from '../components/Alert';
import { authApi } from '../api/authApi';

const roles = [
  { key:'ESTUDANTE', icon:'fa-graduation-cap', title:'Estudante', text:'Busco moradia próxima à universidade' },
  { key:'LOCADOR', icon:'fa-house', title:'Locador', text:'Tenho imóvel e quero anunciar' },
  { key:'MISTO', icon:'fa-house-user', title:'Misto', text:'Quero buscar moradia e também anunciar imóveis' },
];

export default function Cadastro() {
  const [step, setStep] = useState(1);
  const [role, setRole] = useState('ESTUDANTE');
  const [form, setForm] = useState({ nome:'', sobrenome:'', email:'', senha:'', confirmarSenha:'', aceiteLgpd:false });
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(event) {
    event.preventDefault(); setError('');
    if (!form.nome.trim() || !form.sobrenome.trim()) return setError('Preencha nome e sobrenome.');
    if (form.senha.length < 6) return setError('A senha deve ter no mínimo 6 caracteres.');
    if (form.senha !== form.confirmarSenha) return setError('As senhas não coincidem.');
    if (!form.aceiteLgpd) return setError('É preciso aceitar os termos e a política de privacidade.');
    setLoading(true);
    try {
      await authApi.cadastrar({ nome:`${form.nome.trim()} ${form.sobrenome.trim()}`, email:form.email.trim(), senha:form.senha, aceiteLgpd:true, role });
      setStep(3);
    } catch (err) { setError(err.message); }
    finally { setLoading(false); }
  }

  return (
    <div className="auth-screen">
      <AuthVisual cadastro />

      <div className="auth-panel">
        <div
          className="auth-form-box"
          style={{
            maxWidth: 480,
          }}
        >
          <div className="step-progress">
            {[1, 2, 3].map((stepNumber) => (
              <div
                key={stepNumber}
                className={[
                  "step-item",
                  step === stepNumber ? "active" : "",
                  step > stepNumber ? "done" : "",
                ]
                  .filter(Boolean)
                  .join(" ")}
              >
                <div className="step-dot">
                  {stepNumber === 3 ? (
                    <i className="fa-solid fa-check" />
                  ) : (
                    stepNumber
                  )}
                </div>

                <span className="step-label">
                  {stepNumber === 1 && "Tipo de conta"}
                  {stepNumber === 2 && "Seus dados"}
                  {stepNumber === 3 && "Concluído"}
                </span>
              </div>
            ))}
          </div>

          {step === 1 && (
            <div
              style={{
                display: "flex",
                flexDirection: "column",
              }}
            >
              <h2>Criar conta</h2>

              <p className="auth-sub">
                Qual é o seu perfil?
              </p>

              <div
                className="type-cards"
                style={{
                  marginTop: 4,
                }}
              >
                {roles.map((item) => (
                  <button
                    key={item.key}
                    type="button"
                    className={[
                      "type-card",
                      role === item.key ? "selected" : "",
                    ]
                      .filter(Boolean)
                      .join(" ")}
                    onClick={() => setRole(item.key)}
                  >
                    <div className="type-card-icon">
                      <i
                        className={`fa-solid ${item.icon}`}
                      />
                    </div>

                    <strong>{item.title}</strong>

                    <span>{item.text}</span>
                  </button>
                ))}
              </div>

              <button
                type="button"
                className="btn-full"
                style={{
                  marginTop: 24,
                }}
                onClick={() => setStep(2)}
              >
                Continuar
                <i className="fa-solid fa-arrow-right" />
              </button>

              <p className="auth-footer">
                Já tem conta?{" "}
                <Link to="/login">
                  Entrar
                </Link>
              </p>
            </div>
          )}

          {step === 2 && (
            <form
              onSubmit={submit}
              style={{
                display: "flex",
                flexDirection: "column",
                gap: 18,
              }}
            >
              <div>
                <h2>Seus dados</h2>

                <p className="auth-sub">
                  Preencha as informações abaixo
                </p>
              </div>

              <button
                type="button"
                onClick={() => setStep(1)}
                style={{
                  background: "none",
                  border: 0,
                  color: "var(--text-2)",
                  cursor: "pointer",
                  textAlign: "left",
                }}
              >
                <i className="fa-solid fa-arrow-left" />
                Voltar
              </button>

              <Alert>
                {error}
              </Alert>

              <div className="form-grid">
                <div className="form-group">
                  <label
                    className="form-label"
                    htmlFor="nome"
                  >
                    Nome
                  </label>

                  <input
                    id="nome"
                    className="form-input"
                    type="text"
                    required
                    value={form.nome}
                    onChange={(event) =>
                      setForm({
                        ...form,
                        nome: event.target.value,
                      })
                    }
                  />
                </div>

                <div className="form-group">
                  <label
                    className="form-label"
                    htmlFor="sobrenome"
                  >
                    Sobrenome
                  </label>

                  <input
                    id="sobrenome"
                    className="form-input"
                    type="text"
                    required
                    value={form.sobrenome}
                    onChange={(event) =>
                      setForm({
                        ...form,
                        sobrenome: event.target.value,
                      })
                    }
                  />
                </div>
              </div>

              <div className="form-group">
                <label
                  className="form-label"
                  htmlFor="email"
                >
                  E-mail
                </label>

                <input
                  id="email"
                  className="form-input"
                  type="email"
                  required
                  value={form.email}
                  onChange={(event) =>
                    setForm({
                      ...form,
                      email: event.target.value,
                    })
                  }
                />
              </div>

              <div className="form-group">
                <label
                  className="form-label"
                  htmlFor="senha"
                >
                  Senha
                </label>

                <div className="input-icon-wrap">
                  <i className="fa-solid fa-lock input-icon" />

                  <input
                    id="senha"
                    className="form-input"
                    type={showPassword ? "text" : "password"}
                    required
                    value={form.senha}
                    onChange={(event) =>
                      setForm({
                        ...form,
                        senha: event.target.value,
                      })
                    }
                  />

                  <button
                    className="input-eye"
                    type="button"
                    aria-label={
                      showPassword
                        ? "Ocultar senha"
                        : "Mostrar senha"
                    }
                    onClick={() =>
                      setShowPassword((currentValue) => !currentValue)
                    }
                  >
                    <i
                      className={[
                        "fa-solid",
                        showPassword
                          ? "fa-eye-slash"
                          : "fa-eye",
                      ].join(" ")}
                    />
                  </button>
                </div>
              </div>

              <div className="form-group">
                <label
                  className="form-label"
                  htmlFor="confirmarSenha"
                >
                  Confirmar senha
                </label>

                <input
                  id="confirmarSenha"
                  className="form-input"
                  type="password"
                  required
                  value={form.confirmarSenha}
                  onChange={(event) =>
                    setForm({
                      ...form,
                      confirmarSenha: event.target.value,
                    })
                  }
                />
              </div>

              <label className="fp-check-item">
                <input
                  type="checkbox"
                  checked={form.aceiteLgpd}
                  onChange={(event) =>
                    setForm({
                      ...form,
                      aceiteLgpd: event.target.checked,
                    })
                  }
                />

                <span className="check-box" />

                <span>
                  Aceito os Termos de Uso e a Política de Privacidade
                </span>
              </label>

              <button
                className="btn-full"
                type="submit"
                disabled={loading}
              >
                {loading
                  ? "Criando conta..."
                  : "Criar conta"}
              </button>
            </form>
          )}

          {step === 3 && (
            <div
              style={{
                alignItems: "center",
                display: "flex",
                flexDirection: "column",
                gap: 20,
                textAlign: "center",
              }}
            >
              <div
                style={{
                  color: "#22c55e",
                  fontSize: 48,
                }}
              >
                <i className="fa-solid fa-circle-check" />
              </div>

              <div>
                <h2>Conta criada!</h2>

                <p className="auth-sub">
                  Confirme o e-mail e entre com seus dados.
                </p>
              </div>

              <Link
                to="/login"
                className="btn-full"
                style={{
                  textDecoration: "none",
                }}
              >
                Ir para o login
              </Link>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}