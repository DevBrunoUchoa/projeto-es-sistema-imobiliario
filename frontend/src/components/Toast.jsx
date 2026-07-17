export default function Toast({ type = 'success', message, onClose }) {
  if (!message) return null;
  return (
    <div className={`app-toast ${type}`} role="status">
      <i className={`fa-solid ${type === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'}`} />
      <span>{message}</span>
      <button type="button" aria-label="Fechar aviso" onClick={onClose}><i className="fa-solid fa-xmark" /></button>
    </div>
  );
}
