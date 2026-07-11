const expectedLimit = Number(__ENV.BENCHMARK_LIMIT);

if (Number.isNaN(expectedLimit)) {
    throw new Error("BENCHMARK_LIMIT must be a valid number.");
}

if (!__ENV.BENCHMARK_ALGORITHM) {
    throw new Error("BENCHMARK_ALGORITHM environment variable is required.");
}

if (!__ENV.BASE_URL) {
    throw new Error("BASE_URL environment variable is required.");
}

export const config = {

    baseUrl: __ENV.BASE_URL,

    endpoint: "/api/v1/rate-limit/benchmark",

    expectedLimit,

    expectedAlgorithm: __ENV.BENCHMARK_ALGORITHM,

    defaultHeaders: {
        Accept: "application/json"
    }

};