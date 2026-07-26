import http from 'k6/http';
import { check } from 'k6';
import { Rate } from 'k6/metrics';

// --- Config (override with -e on the command line) ---
const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const POOL_SIZE = parseInt(__ENV.POOL_SIZE || '20');
const HOT_FRACTION = 0.2;   // the top 20% of codes...
const HOT_TRAFFIC = 0.8;    // ...receive 80% of the traffic

// Custom metric: fraction of redirects that returned 302.
const redirectOk = new Rate('redirect_ok');

export const options = {
    scenarios: {
        load: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '20s', target: 50 },  // ramp up to 50 virtual users
                { duration: '60s', target: 50 },  // hold at 50 for a minute
                { duration: '10s', target: 0 },   // ramp down
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<50', 'p(99)<100'],
        redirect_ok: ['rate>0.99'],
    },
};

// setup() runs once before the load. It creates the pool of short
// URLs and returns their codes to every virtual user.
export function setup() {
    const codes = [];
    for (let i = 0; i < POOL_SIZE; i++) {
        const res = http.post(
            `${BASE}/api/shorten`,
            JSON.stringify({ longUrl: `https://example.com/load-test/${i}` }),
            { headers: { 'Content-Type': 'application/json' } },
        );
        codes.push(JSON.parse(res.body).shortCode);
    }
    console.log(`Created ${codes.length} short codes`);
    return { codes };
}

// The main loop — each virtual user runs this repeatedly.
export default function (data) {
    const codes = data.codes;
    const hotCount = Math.max(1, Math.floor(codes.length * HOT_FRACTION));

    let idx;
    if (Math.random() < HOT_TRAFFIC) {
        idx = Math.floor(Math.random() * hotCount);                       // hot subset
    } else {
        idx = hotCount + Math.floor(Math.random() * (codes.length - hotCount)); // cold tail
    }

    const code = codes[idx % codes.length];

    // redirects: 0 → do NOT follow the 302 to example.com.
    // We want to measure OUR redirect latency, not the internet.
    const res = http.get(`${BASE}/${code}`, { redirects: 0 });

    const ok = check(res, { 'status is 302': (r) => r.status === 302 });
    redirectOk.add(ok);
}