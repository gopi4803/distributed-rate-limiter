// benchmark/lib/config.js

export const config = {

    baseUrl: __ENV.BASE_URL || 'http://localhost:8080',

    endpoint: '/api/v1/rate-limit/benchmark',

    expectedLimit:
        Number(__ENV.BENCHMARK_LIMIT || 100000),

    expectedAlgorithm:
        __ENV.BENCHMARK_ALGORITHM || 'TOKEN_BUCKET',

    defaultHeaders: {
        Accept: 'application/json'
    }

};