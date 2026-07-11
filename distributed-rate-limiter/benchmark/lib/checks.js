import { check } from 'k6';
import { config } from './config.js';

/*
 * Validation benchmark
 *
 * One request.
 * Everything should be correct.
 */
export function validateRateLimiterResponse(response) {

    return check(response, {

        'status is 200':
            (r) => r.status === 200,

        'limit header correct':
            (r) =>
                Number(r.headers['X-RateLimit-Limit']) === config.expectedLimit,

        'remaining header valid':
            (r) =>
                Number(r.headers['X-RateLimit-Remaining']) >= 0,

        'algorithm correct':
            (r) =>
                r.headers['X-RateLimit-Algorithm'] === config.expectedAlgorithm

    });

}

/*
 * Infrastructure benchmark
 *
 * Only HTTP 200 responses are expected.
 */
export function validateInfrastructureResponse(response) {

    if (__VU === 1 && __ITER === 0) {
        console.log(JSON.stringify({
            expectedLimit: config.expectedLimit,
            actualLimit: Number(response.headers["X-Ratelimit-Limit"]),
            expectedAlgorithm: config.expectedAlgorithm,
            actualAlgorithm: response.headers["X-Ratelimit-Algorithm"]
        }, null, 2));
    }
    return check(response, {

        'status is 200':
            (r) => r.status === 200,

        'limit header correct':
            (r) =>
                Number(r.headers['X-RateLimit-Limit']) === config.expectedLimit,

        'remaining header valid':
            (r) =>
                Number(r.headers['X-RateLimit-Remaining']) >= 0,

        'algorithm correct':
            (r) =>
                r.headers['X-RateLimit-Algorithm'] === config.expectedAlgorithm

    });

}

/*
 * Behavioral benchmark
 *
 * HTTP 200 and HTTP 429 are both expected.
 */
export function validateBehavioralResponse(response) {

    return check(response, {

        'status valid':
            (r) => r.status === 200 || r.status === 429,

        'limit header exists':
            (r) =>
                r.headers['X-RateLimit-Limit'] !== undefined,

        'remaining header exists':
            (r) =>
                r.headers['X-RateLimit-Remaining'] !== undefined,

        'algorithm correct':
            (r) =>
                r.headers['X-RateLimit-Algorithm'] === config.expectedAlgorithm

    });

}