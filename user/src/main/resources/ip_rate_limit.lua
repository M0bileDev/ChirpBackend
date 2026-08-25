local key = KEYS[1]

--optional extra arguments
local max_requests = tonumber(ARGV[1])
local ttl = tonumber(ARGV[2])

local current = redis.call('GET', key)
-- if the ip rate limiting is not set, create a new one with expiration time
if current == false then
    redis.call('SET', key, 1, 'EX', ttl)
    return { 1, ttl }
else
    local count = tonumber(current)
    local remaining_ttl = redis.call('TTL', key)

    -- client did not reach rate limit, so increment request count
    if count < max_requests then
        local new_count = redis.call('INCR', key)
        return { new_count, remaining_ttl }
    else
        return { count + 1, remaining_ttl }
    end
end