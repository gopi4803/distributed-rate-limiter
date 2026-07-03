import { check } from 'k6';

export function validateRateLimiterResponse(response) {

    return check(response, {

        'status is 200':
            (r) => r.status === 200,

        'limit header correct':
            (r) => Number(r.headers['X-Ratelimit-Limit']) === 100000,

        'remaining header valid':
            (r) => Number(r.headers['X-Ratelimit-Remaining']) >= 0,

        'algorithm correct':
            (r) => r.headers['X-Ratelimit-Algorithm'] === 'TOKEN_BUCKET'

    });

}