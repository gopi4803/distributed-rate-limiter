import http from 'k6/http';

import { config } from '../lib/config.js';
import { urls } from '../lib/urls.js';
import { validateBehavioralResponse } from '../lib/checks.js';

// Treat both HTTP 200 and HTTP 429 as expected responses.
// This prevents k6 from counting legitimate rate-limited
// requests as http_req_failed.
http.setResponseCallback(
    http.expectedStatuses(200, 429)
);

const vus = Number(__ENV.VUS || 5);
const duration = __ENV.DURATION || '30s';

export const options = {

    scenarios: {

        behavioral: {

            executor: 'constant-vus',

            exec: 'behavioralScenario',

            vus: vus,

            duration: duration,

            gracefulStop: '5s',

            tags: {
                benchmark: 'behavioral'
            }

        }

    },

    thresholds: {

        // 95% of requests should complete within 100ms.
        http_req_duration: ['p(95)<100'],

        // All behavioral checks must pass.
        checks: ['rate==1.0']

    }

};

export function behavioralScenario() {

    const response = http.get(
        urls.benchmark(),
        {
            headers: config.defaultHeaders
        }
    );

    validateBehavioralResponse(response);

}