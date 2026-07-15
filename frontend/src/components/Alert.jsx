export default function Alert({ type = 'error', children }) {
  if (!children) return null;
  return <div className={`alert alert-${type}`}><i className={`fa-solid ${type === 'error' ? 'fa-circle-exclamation' : 'fa-circle-check'}`}/><span>{children}</span></div>;
}
