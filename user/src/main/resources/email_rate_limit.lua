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

local newCount = redis.call('INCR', attemptCountKey)

--first attempt 60 seconds, next is 300 sec, and finally 1 hour
local backoffSeconds = { 60, 300, 3600 }
--take minimal value between current count and maximum value from array (the last one)
local backoffIndex = math.min(currentCount, 2) + 1

--set expiration for rate limit key
--1 servers like a lock -> user is rate limited
--SETEX set expiration time and value, EXPIRE sets only expiration time
redis.call('SETEX', rateLimitKey, backoffSeconds[backoffIndex], '1')
--86400 -> 24h
redis.call('EXPIRE', attemptCountKey, 86400)

--user is not rate limited
return { newCount, 0 }