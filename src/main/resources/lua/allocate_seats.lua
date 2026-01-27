-- KEYS[1] = show:{showId}:available_seats
-- KEYS[2] = booking:{bookingId}:locked_seats
-- ARGV[1] = seatCount
-- ARGV[2] = ttlSeconds

local seatCount = tonumber(ARGV[1])
local ttl = tonumber(ARGV[2])

-- 1. Ensure availability set exists
if redis.call("EXISTS", KEYS[1]) == 0 then
    return { err = "SEATS_NOT_INITIALIZED" }
end

-- 2. Check availability
local available = redis.call("SCARD", KEYS[1])
if available < seatCount then
    return { err = "INSUFFICIENT_SEATS" }
end

-- 3. Pop seats
local seats = redis.call("SPOP", KEYS[1], seatCount)

-- Normalize return type (important!)
if type(seats) ~= "table" then
    seats = { seats }
end

-- 4. Lock seats
for _, seat in ipairs(seats) do
    redis.call("SADD", KEYS[2], seat)
end

-- 5. Set TTL
redis.call("EXPIRE", KEYS[2], ttl)

-- 6. Return locked seats
return seats
