export class ApiError extends Error {
  constructor(message, status, data) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

async function parseResponse(response) {
  const contentType = response.headers.get('content-type') ?? '';
  if (response.status === 204) return null;
  return contentType.includes('application/json')
    ? response.json().catch(() => null)
    : response.text().catch(() => '');
}

async function execute(endpoint, options = {}) {
  const { method = 'GET', body, headers = {}, signal } = options;
  const isFormData = body instanceof FormData;

  return fetch(endpoint, {
    method,
    credentials: 'include',
    signal,
    headers: {
      ...(body !== undefined && !isFormData ? { 'Content-Type': 'application/json' } : {}),
      ...headers,
    },
    body: body === undefined ? undefined : (isFormData ? body : JSON.stringify(body)),
  });
}

export async function apiRequest(endpoint, options = {}) {
  let response = await execute(endpoint, options);

  const canRefresh = response.status === 401
    && endpoint !== '/auth/login'
    && endpoint !== '/auth/refresh'
    && endpoint !== '/auth/logout';

  if (canRefresh) {
    const refreshResponse = await execute('/auth/refresh', { method: 'POST' });
    if (refreshResponse.ok) {
      response = await execute(endpoint, options);
    }
  }

  const data = await parseResponse(response);

  if (!response.ok) {
    const validation = Array.isArray(data?.errors) ? data.errors.join(' ') : null;
    const message = validation || data?.message || data?.mensagem
      || (typeof data === 'string' ? data : null)
      || 'Não foi possível concluir a solicitação.';
    throw new ApiError(message, response.status, data);
  }

  return data;
}
