export class ApiError extends Error {
  constructor(message, status, data) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

export async function apiRequest(endpoint, options = {}) {
  const { method = 'GET', body, headers = {}, signal } = options;
  const response = await fetch(endpoint, {
    method,
    credentials: 'include',
    signal,
    headers: {
      ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
      ...headers,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  const contentType = response.headers.get('content-type') ?? '';
  let data = null;
  if (response.status !== 204) {
    data = contentType.includes('application/json')
      ? await response.json().catch(() => null)
      : await response.text().catch(() => '');
  }

  if (!response.ok) {
    const validation = Array.isArray(data?.errors) ? data.errors.join(' ') : null;
    const message = validation || data?.message || data?.mensagem || (typeof data === 'string' ? data : null) || 'Não foi possível concluir a solicitação.';
    throw new ApiError(message, response.status, data);
  }

  return data;
}
