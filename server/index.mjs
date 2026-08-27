import http from "node:http";
import { Readable } from "node:stream";

const port = Number(process.env.PORT || 8787);
const plantNetApiKey = process.env.PLANTNET_API_KEY;
const proxyToken = process.env.PLANT_PROXY_TOKEN;
const rateLimitWindowMs = 60 * 1000;
const rateLimitMax = 30;
const requestsByIp = new Map();

if (!plantNetApiKey) {
  throw new Error("PLANTNET_API_KEY environment variable is required");
}

function readBody(request, maxBytes = 15 * 1024 * 1024) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    let size = 0;
    request.on("data", (chunk) => {
      size += chunk.length;
      if (size > maxBytes) {
        reject(new Error("Request body is too large"));
        request.destroy();
        return;
      }
      chunks.push(chunk);
    });
    request.on("end", () => resolve(Buffer.concat(chunks)));
    request.on("error", reject);
  });
}

function isRateLimited(ip) {
  const now = Date.now();

  for (const [knownIp, entry] of requestsByIp) {
    if (now - entry.startedAt >= rateLimitWindowMs) {
      requestsByIp.delete(knownIp);
    }
  }

  const current = requestsByIp.get(ip);
  if (!current || now - current.startedAt >= rateLimitWindowMs) {
    requestsByIp.set(ip, { startedAt: now, count: 1 });
    return false;
  }

  current.count += 1;
  return current.count > rateLimitMax;
}

const server = http.createServer(async (request, response) => {
  request.setTimeout(60 * 1000, () => request.destroy());

  if (request.method === "GET" && request.url === "/healthz") {
    response.writeHead(200, { "content-type": "application/json" });
    response.end(JSON.stringify({ ok: true }));
    return;
  }

  if (request.method !== "POST" || request.url !== "/v2/identify/all") {
    response.writeHead(404);
    response.end("Not found");
    return;
  }

  if (proxyToken && request.headers["x-plant-proxy-token"] !== proxyToken) {
    response.writeHead(401, { "content-type": "application/json" });
    response.end(JSON.stringify({ message: "Unauthorized" }));
    return;
  }

  const clientIp = request.socket.remoteAddress || "unknown";
  if (isRateLimited(clientIp)) {
    response.writeHead(429, {
      "content-type": "application/json",
      "retry-after": "60",
    });
    response.end(JSON.stringify({ message: "Too many requests" }));
    return;
  }

  const contentType = request.headers["content-type"] || "";
  if (!contentType.toLowerCase().startsWith("multipart/form-data;")) {
    response.writeHead(415, { "content-type": "application/json" });
    response.end(JSON.stringify({ message: "Multipart image upload required" }));
    return;
  }

  try {
    const requestedLanguage = request.headers["x-app-language"];
    const language = ["tr", "en"].includes(requestedLanguage) ? requestedLanguage : "en";
    console.log(`Identify request received; organ filter: ${request.headers["x-plant-organ"] || "none"}; language: ${language}`);
    const body = await readBody(request);
    const upstream = await fetch(
      `https://my-api.plantnet.org/v2/identify/all?api-key=${encodeURIComponent(plantNetApiKey)}&lang=${language}`,
      {
        method: "POST",
        headers: { "content-type": request.headers["content-type"] || "" },
        body,
        signal: AbortSignal.timeout(45 * 1000),
      },
    );

    console.log(`PlantNet response: ${upstream.status} ${upstream.statusText}`);

    response.writeHead(upstream.status, {
      "content-type": upstream.headers.get("content-type") || "application/json",
      "cache-control": "no-store",
    });

    if (upstream.body) {
      Readable.fromWeb(upstream.body).pipe(response);
    } else {
      response.end();
    }
  } catch (error) {
    console.error(error);
    response.writeHead(502, { "content-type": "application/json" });
    response.end(JSON.stringify({ message: "Plant service unavailable" }));
  }
});

server.listen(port, () => console.log(`Plant AI proxy listening on port ${port}`));
