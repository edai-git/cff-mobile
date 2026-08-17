const https = require('https');
const fs = require('fs');
const path = require('path');
const url = require('url');
const os = require('os');

// Configuration
const DEFAULT_PORT = 443;
const HOST = process.env.HOST || '0.0.0.0';

// Parse command line arguments for port (e.g. node host.js 8443 or node host.js -p 8443)
function getPort() {
  const args = process.argv.slice(2);
  for (let i = 0; i < args.length; i++) {
    if (args[i] === '--port' || args[i] === '-p') {
      const p = parseInt(args[i + 1], 10);
      if (!isNaN(p)) return p;
    }
    const p = parseInt(args[i], 10);
    if (!isNaN(p)) return p;
  }
  return parseInt(process.env.PORT, 10) || DEFAULT_PORT;
}

const PORT = getPort();
const CERT_FILE = path.join(__dirname, 'localhost.pem');

// MIME types dictionary for WebKit exploit hosting
const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.htm': 'text/html; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.mjs': 'application/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.bin': 'application/octet-stream',
  '.manifest': 'text/cache-manifest; charset=utf-8',
  '.appcache': 'text/cache-manifest; charset=utf-8',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.png': 'image/png',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.wasm': 'application/wasm',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.otf': 'font/otf',
  '.txt': 'text/plain; charset=utf-8',
  '.xml': 'application/xml; charset=utf-8'
};

// Check SSL certificate
if (!fs.existsSync(CERT_FILE)) {
  console.error(`[ERROR] SSL certificate file not found: ${CERT_FILE}`);
  process.exit(1);
}

let sslOptions;
try {
  const pem = fs.readFileSync(CERT_FILE);
  sslOptions = {
    key: pem,
    cert: pem
  };
} catch (err) {
  console.error(`[ERROR] Failed to read certificate: ${err.message}`);
  process.exit(1);
}

// Helper to find existing file path (resolving between public folder and root)
function resolveFilePath(requestUrl) {
  const parsedUrl = url.parse(requestUrl);
  let pathname = decodeURIComponent(parsedUrl.pathname || '/');

  // Prevent path traversal attacks
  const safePath = path.normalize(pathname).replace(/^(\.\.[\/\\])+/, '');

  const searchRoots = [
    path.join(__dirname, 'public'),
    __dirname
  ];

  if (safePath === '/' || safePath === '\\') {
    for (const root of searchRoots) {
      const indexPath = path.join(root, 'index.html');
      if (fs.existsSync(indexPath) && fs.statSync(indexPath).isFile()) {
        return indexPath;
      }
    }
  }

  for (const root of searchRoots) {
    const candidatePath = path.join(root, safePath);
    if (fs.existsSync(candidatePath)) {
      const stat = fs.statSync(candidatePath);
      if (stat.isFile()) {
        return candidatePath;
      } else if (stat.isDirectory()) {
        const candidateIndex = path.join(candidatePath, 'index.html');
        if (fs.existsSync(candidateIndex) && fs.statSync(candidateIndex).isFile()) {
          return candidateIndex;
        }
      }
    }
  }

  return null;
}

// Request handler
const server = https.createServer(sslOptions, (req, res) => {
  const start = Date.now();
  const filePath = resolveFilePath(req.url);

  // Set CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, HEAD, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', '*');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  if (req.method !== 'GET' && req.method !== 'HEAD') {
    res.writeHead(405, { 'Content-Type': 'text/plain' });
    res.end('Method Not Allowed');
    return;
  }

  if (!filePath) {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('404 Not Found');
    console.log(`[404] ${req.method} ${req.url} - ${Date.now() - start}ms`);
    return;
  }

  const ext = path.extname(filePath).toLowerCase();
  const contentType = MIME_TYPES[ext] || 'application/octet-stream';

  fs.stat(filePath, (err, stats) => {
    if (err) {
      res.writeHead(500, { 'Content-Type': 'text/plain' });
      res.end('500 Internal Server Error');
      console.error(`[500] ${req.method} ${req.url} - Error: ${err.message}`);
      return;
    }

    res.writeHead(200, {
      'Content-Type': contentType,
      'Content-Length': stats.size,
      'Cache-Control': 'no-cache'
    });

    if (req.method === 'HEAD') {
      res.end();
      console.log(`[200] HEAD ${req.url} (${stats.size} bytes) - ${Date.now() - start}ms`);
      return;
    }

    const stream = fs.createReadStream(filePath);
    stream.pipe(res);
    stream.on('error', (streamErr) => {
      console.error(`[STREAM ERROR] ${req.url} - ${streamErr.message}`);
    });
    res.on('finish', () => {
      console.log(`[200] GET ${req.url} -> ${path.relative(__dirname, filePath)} (${stats.size} bytes) - ${Date.now() - start}ms`);
    });
  });
});

// Display network interfaces
function getLocalIPs() {
  const interfaces = os.networkInterfaces();
  const addresses = [];
  for (const name of Object.keys(interfaces)) {
    for (const net of interfaces[name]) {
      if (net.family === 'IPv4' || net.family === 4) {
        addresses.push({ name, address: net.address, internal: net.internal });
      }
    }
  }
  return addresses;
}

// Error handling on server listen
server.on('error', (err) => {
  if (err.code === 'EACCES') {
    console.error(`\n[ERROR] Permission denied to bind to port ${PORT}.`);
    console.error(`Ports below 1024 often require administrator / root privileges.`);
    console.error(`Try running as Administrator, or specify another port: node host.js 8443\n`);
  } else if (err.code === 'EADDRINUSE') {
    console.error(`\n[ERROR] Port ${PORT} is already in use by another application.\n`);
  } else {
    console.error(`\n[ERROR] Server error: ${err.message}\n`);
  }
  process.exit(1);
});

// Start listening
server.listen(PORT, HOST, () => {
  console.log('='.repeat(55));
  console.log('   CSSFontFace Exploit Host Server (Node.js)');
  console.log('='.repeat(55));
  console.log(`HTTPS server listening on port ${PORT}...`);
  console.log('\nAvailable URLs for PS4 Web Browser / User\'s Guide:');

  const ips = getLocalIPs();
  const portSuffix = PORT === 443 ? '' : `:${PORT}`;

  ips.forEach(ip => {
    console.log(`  - [${ip.name}] https://${ip.address}${portSuffix}/`);
  });

  console.log('='.repeat(55));
  console.log('Press Ctrl+C to stop the server.\n');
});
