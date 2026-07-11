import http from 'k6/http';

import { config } from '../lib/config.js';
import { urls } from '../lib/urls.js';
import { validateInfrastructureResponse } from '../lib/checks.js';

const vus = Number(__ENV.VUS || 5);
const duration = __ENV.DURATION || '30s';

export const options = {
    scenarios: {
        infrastructure: {
            executor: 'constant-vus',
            exec: 'infrastructureScenario',
            vus: vus,
            duration: duration,
            gracefulStop: '5s',
            tags: {
                benchmark: 'infrastructure'
            }
        }
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<100']
    }
};

export function infrastructureScenario() {

    const response = http.get(
        urls.benchmark(),
        {
            headers: config.defaultHeaders
        }
    );

    validateInfrastructureResponse(response);

}