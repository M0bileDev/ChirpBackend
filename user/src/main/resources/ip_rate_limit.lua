local key = KEYS[1]

--optional extra arguments
local max_requests = tonumber(ARGV[1])
local ttl = tonumber(ARGV[2])

local current = redis.call('GET', key)
-- if the ip rate limiting is not set, create a new one with expiration time
if current == false then
    redis.call('SET', key, 1, 'EX', ttl)
    return { 1, ttl }
end