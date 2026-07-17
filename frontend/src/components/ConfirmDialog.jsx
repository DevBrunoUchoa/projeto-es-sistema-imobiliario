export default function ConfirmDialog({ open, title, message, confirmLabel = 'Confirmar', cancelLabel = 'Cancelar', danger = false, loading = false, onConfirm, onCancel }) {
  if (!open) return null;

  return (
    <div className="dialog-backdrop" role="presentation" onMouseDown={(event) => {
      if (event.target === event.currentTarget && !loading) onCancel?.();
    }}>
      <section className="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="confirm-dialog-title">
        <div className={`dialog-icon ${danger ? 'danger' : ''}`}>
          <i className={`fa-solid ${danger ? 'fa-triangle-exclamation' : 'fa-circle-question'}`} />
        </div>
        <h2 id="confirm-dialog-title">{title}</h2>
        <p>{message}</p>
        <div className="dialog-actions">
          <button type="button" className="btn-sm btn-outline" disabled={loading} onClick={onCancel}>{cancelLabel}</button>
          <button type="button" className={`btn-sm ${danger ? 'btn-danger' : 'btn-primary'}`} disabled={loading} onClick={onConfirm}>
            {loading ? 'Aguarde...' : confirmLabel}
          </button>
        </div>
      </section>
    </div>
  );
}
