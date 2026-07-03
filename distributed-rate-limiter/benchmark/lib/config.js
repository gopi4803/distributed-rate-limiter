// benchmark/lib/config.js

export const config = {

    baseUrl: __ENV.BASE_URL || 'http://localhost:8080',

    endpoint: '/api/v1/rate-limit/benchmark',

    defaultHeaders: {
        'Accept': 'application/json'
    }

};