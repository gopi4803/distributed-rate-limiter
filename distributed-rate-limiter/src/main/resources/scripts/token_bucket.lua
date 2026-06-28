local capacity = tonumber(ARGV[1])
local refillRatePerMillis = tonumber(ARGV[2])

local currentTime = redis.call('TIME')
local currentTimeMillis =
        currentTime[1] * 1000 +
        math.floor(currentTime[2] / 1000)

local bucket = redis.call(
        'HMGET',
        KEYS[1],
        'tokens',
        'last_refill_ms')

local availableTokens = tonumber(bucket[1])
local lastRefillMillis = tonumber(bucket[2])

if availableTokens == nil then
    availableTokens = capacity
    lastRefillMillis = currentTimeMillis
end

local elapsedMillis =
        currentTimeMillis - lastRefillMillis

local refilledTokens =
        elapsedMillis * refillRatePerMillis

availableTokens =
        math.min(
            capacity,
            availableTokens + refilledTokens)

if availableTokens >= 1 then

    availableTokens = availableTokens - 1

    redis.call(
            'HMSET',
            KEYS[1],
            'tokens',
            availableTokens,
            'last_refill_ms',
            currentTimeMillis)

    redis.call(
            'PEXPIRE',
            KEYS[1],
            math.ceil(
                    capacity /
                    refillRatePerMillis))

    return {
        1,
        math.floor(availableTokens),
        0
    }
end

redis.call(
        'HMSET',
        KEYS[1],
        'tokens',
        availableTokens,
        'last_refill_ms',
        currentTimeMillis)

local retryAfterMillis =
        math.ceil(
                (1 - availableTokens)
                / refillRatePerMillis)

return {
    0,
    0,
    retryAfterMillis
}