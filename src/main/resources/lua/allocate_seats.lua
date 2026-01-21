-- KEYS[1] = show:{showId}:available_seats
-- KEYS[2] = booking:{bookingId}:locked_seats
-- ARGV[1] = seatCount
-- ARGV[2] = ttlSeconds

local seatCount = tonumber(ARGV[1])
local ttl = tonumber(ARGV[2])

-- Check availability
local available = redis.call("SCARD", KEYS[1])
if available < seatCount then
    return nil
end

-- Pop seats atomically
local seats = redis.call("SPOP", KEYS[1], seatCount)

-- Lock seats for booking
for _, seat in ipairs(seats) do
    redis.call("SADD", KEYS[2], seat)
end

-- Set expiry for booking
redis.call("EXPIRE", KEYS[2], ttl)

return seats
