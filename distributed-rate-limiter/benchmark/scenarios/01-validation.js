import http from 'k6/http';

import { config } from '../lib/config.js';
import { urls } from '../lib/urls.js';
import { validateRateLimiterResponse } from '../lib/checks.js';

export const options = {

    scenarios: {

        validation: {

            executor: 'shared-iterations',

            exec: 'validationScenario',

            vus: 1,

            iterations: 1

        }

    }

};

export function validationScenario() {

    const response = http.get(
        urls.benchmark(),
        {
            headers: config.defaultHeaders
        }
    );

    console.log(JSON.stringify(response.headers));

    validateRateLimiterResponse(response);

}