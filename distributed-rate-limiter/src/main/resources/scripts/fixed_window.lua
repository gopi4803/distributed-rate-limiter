local current = redis.call('GET', KEYS[1])

if current == false then
    current = 0
else
    current = tonumber(current)
end

local limit = tonumber(ARGV[1])
local ttlMillis = tonumber(ARGV[2])

if current >= limit then
    local retryAfterMillis = redis.call('PTTL', KEYS[1])

    if retryAfterMillis < 0 then
        retryAfterMillis = ttlMillis
    end

    return {0, 0, retryAfterMillis}
end

current = redis.call('INCR', KEYS[1])

if current == 1 then
    redis.call('PEXPIRE', KEYS[1], ttlMillis)
end

local remaining = limit - current

return {1, remaining, 0}