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
                Number(r.headers['X-Ratelimit-Limit']) === config.expectedLimit,

        'remaining header valid':
            (r) =>
                Number(r.headers['X-Ratelimit-Remaining']) >= 0,

        'algorithm correct':
            (r) =>
                r.headers['X-Ratelimit-Algorithm'] === config.expectedAlgorithm

    });

}

/*
 * Infrastructure benchmark
 *
 * Only HTTP 200 responses are expected.
 */
export function validateInfrastructureResponse(response) {

    return check(response, {

        'status is 200':
            (r) => r.status === 200,

        'limit header correct':
            (r) =>
                Number(r.headers['X-Ratelimit-Limit']) === config.expectedLimit,

        'remaining header valid':
            (r) =>
                Number(r.headers['X-Ratelimit-Remaining']) >= 0,

        'algorithm correct':
            (r) =>
                r.headers['X-Ratelimit-Algorithm'] === config.expectedAlgorithm

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
                r.headers['X-Ratelimit-Limit'] !== undefined,

        'remaining header exists':
            (r) =>
                r.headers['X-Ratelimit-Remaining'] !== undefined,

        'algorithm correct':
            (r) =>
                r.headers['X-Ratelimit-Algorithm'] === config.expectedAlgorithm

    });

}