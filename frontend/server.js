const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;

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
  console.log(`│  🏠  EstudanteLar rodando em:            │`);
  console.log(`│      http://localhost:${PORT}               │`);
  console.log('└─────────────────────────────────────────┘\n');
  console.log('  Pressione Ctrl+C para encerrar.\n');
});

process.on('SIGINT', () => {
  console.log('\n⛔  Servidor encerrado.\n');
  process.exit(0);
});
