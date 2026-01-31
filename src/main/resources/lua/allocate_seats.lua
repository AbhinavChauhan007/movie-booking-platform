-- KEYS[1] = show:{showId}:available_seats
-- KEYS[2] = booking:{bookingId}:locked_seats
-- ARGV[1] = seatCount
-- ARGV[2] = ttlSeconds

local seatCount = tonumber(ARGV[1])
local ttl = tonumber(ARGV[2])

-- 🔒 Defensive argument validation
if not seatCount or seatCount <= 0 then
    return { err = "INVALID_SEAT_COUNT" }
end

if not ttl or ttl <= 0 then
    return { err = "INVALID_TTL" }
end

-- 1. Check if the key exists and is a set
local typeInfo = redis.call("TYPE", KEYS[1])
local keyType = typeInfo["ok"] or typeInfo

if keyType ~= "set" then
    return { err = "SEATS_NOT_INITIALIZED" }
end

-- 2. Get available count
local available = tonumber(redis.call("SCARD", KEYS[1]) or 0)

if available < seatCount then
    return { err = "INSUFFICIENT_SEATS" }
end

-- 3. Pop seats
local seats = redis.call("SPOP", KEYS[1], seatCount)

-- 4. Validate pop result
if not seats or #seats < seatCount then
    if seats and #seats > 0 then
        for _, s in ipairs(seats) do
            redis.call("SADD", KEYS[1], s)
        end
    end
    return { err = "INSUFFICIENT_SEATS" }
end

-- 5. Lock seats
for _, seat in ipairs(seats) do
    redis.call("SADD", KEYS[2], seat)
end

-- 6. Set TTL
redis.call("EXPIRE", KEYS[2], ttl)

return seats
