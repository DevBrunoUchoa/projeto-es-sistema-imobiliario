const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');
const { URL } = require('url');

const PORT = 3000;

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8085';


const PROXY_PREFIXES = ['/auth'];


function proxyToBackend(req, res) {
  const target = new URL(req.url, BACKEND_URL);
  const client = target.protocol === 'https:' ? https : http;

  const proxyReq = client.request(
    target,
    {
      method: req.method,
      headers: { ...req.headers, host: target.host },
    },
    (proxyRes) => {
      res.writeHead(proxyRes.statusCode, proxyRes.headers);
      proxyRes.pipe(res);
    }
  );

  proxyReq.on('error', (err) => {
    console.error('Erro ao repassar requisição para o backend:', err.message);
    res.writeHead(502, { 'Content-Type': 'application/json; charset=utf-8' });
    res.end(JSON.stringify({
      message: 'Não foi possível conectar ao backend. Verifique se ele está rodando em ' + BACKEND_URL,
    }));
  });

  req.pipe(proxyReq);
}

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css':  'text/css; charset=utf-8',
  '.js':   'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png':  'image/png',
  '.jpg':  'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.svg':  'image/svg+xml',
  '.ico':  'image/x-icon',
  '.webp': 'image/webp',
};

const server = http.createServer((req, res) => {
  let urlPath = req.url.split('?')[0];

  if (PROXY_PREFIXES.some((prefix) => urlPath === prefix || urlPath.startsWith(prefix + '/'))) {
    proxyToBackend(req, res);
    return;
  }

  if (urlPath === '/') urlPath = '/index.html';

  const filePath = path.join(__dirname, urlPath);
  const ext = path.extname(filePath).toLowerCase();
  const contentType = MIME[ext] || 'text/plain';

  fs.readFile(filePath, (err, content) => {
    if (err) {
      if (err.code === 'ENOENT') {
        res.writeHead(404, { 'Content-Type': 'text/html; charset=utf-8' });
        res.end('<h1>404 — Página não encontrada</h1>');
      } else {
        res.writeHead(500, { 'Content-Type': 'text/plain' });
        res.end('500 — Erro interno do servidor');
      }
      return;
    }
    res.writeHead(200, {
      'Content-Type': contentType,
      'Cache-Control': 'no-cache, no-store, must-revalidate',
    });
    res.end(content);
  });
});

server.listen(PORT, () => {
  console.log('\n┌─────────────────────────────────────────┐');
  console.log(`│  🏠  CampusLiving rodando em:            │`);
  console.log(`│      http://localhost:${PORT}               │`);
  console.log('└─────────────────────────────────────────┘\n');
  console.log(`  Proxy /auth/* → ${BACKEND_URL}`);
  console.log('  Pressione Ctrl+C para encerrar.\n');
});

process.on('SIGINT', () => {
  console.log('\n⛔  Servidor encerrado.\n');
  process.exit(0);
});