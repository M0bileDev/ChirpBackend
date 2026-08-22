local rateLimitKey = KEYS[1]
local attemptCountKey = KEYS[2]

--check if some value has been already saved by that key (specific email), if not fired that means user is not rate limited
if redis.call('EXISTS', rateLimitKey) == 1 then
    --ttl -> time to live
    --get remaining time to live for limit key (and specific email), remaining seconds until this key expires
    local ttl = redis.call('TTL', rateLimitKey)
    --    in lua {} <- array
    --    -1 is limited
    --  ttl > 0 and ttl or 60 -> if ttl is greater than 0 return ttl or 60 seconds (default)
    return { -1, ttl > 0 and ttl or 60 }
end

-- user not rate limited
local currentCount = redis.call('GET', attemptCountKey)
--convert to number, 0 as default
currentCount = currentCount and tonumber(currentCount) or 0

