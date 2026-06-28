local limit = tonumber(ARGV[1])
local windowSizeMillis = tonumber(ARGV[2])

local redisTime = redis.call('TIME')

local nowMillis =
        redisTime[1] * 1000 +
        math.floor(redisTime[2] / 1000)

local currentWindowStart =
        math.floor(nowMillis / windowSizeMillis)
        * windowSizeMillis

local bucket =
        redis.call(
                'HMGET',
                KEYS[1],
                'current_window_start',
                'current_count',
                'previous_count')

local storedWindowStart =
        tonumber(bucket[1])

local currentCount =
        tonumber(bucket[2])

local previousCount =
        tonumber(bucket[3])

if storedWindowStart == nil then
    storedWindowStart = currentWindowStart
    currentCount = 0
    previousCount = 0
end

if storedWindowStart ~= currentWindowStart then

    local windowsPassed =
            (currentWindowStart
             - storedWindowStart)
             / windowSizeMillis

    if windowsPassed == 1 then
        previousCount = currentCount
    else
        previousCount = 0
    end

    currentCount = 0
    storedWindowStart = currentWindowStart
end

local elapsedMillis =
        nowMillis - storedWindowStart

local elapsedFraction =
        elapsedMillis / windowSizeMillis

local previousWeight =
        1.0 - elapsedFraction

local effectiveCount =
        previousCount * previousWeight
        + currentCount

if effectiveCount >= limit then

    local retryAfter =
            windowSizeMillis
            - elapsedMillis

    return {
        0,
        0,
        retryAfter
    }
end

currentCount = currentCount + 1

redis.call(
        'HMSET',
        KEYS[1],
        'current_window_start',
        storedWindowStart,
        'current_count',
        currentCount,
        'previous_count',
        previousCount)

redis.call(
        'PEXPIRE',
        KEYS[1],
        windowSizeMillis * 2)

local remaining =
        limit - currentCount

return {
    1,
    remaining,
    0
}