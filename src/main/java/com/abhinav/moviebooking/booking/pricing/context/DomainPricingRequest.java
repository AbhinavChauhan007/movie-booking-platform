package com.abhinav.moviebooking.booking.pricing.context;

public class DomainPricingRequest {

    private final double basePrice;
    private final boolean isWeekend;
    private final boolean isPremium;

    public DomainPricingRequest(double basePrice, boolean isWeekend, boolean isPremium) {
        this.basePrice = basePrice;
        this.isWeekend = isWeekend;
        this.isPremium = isPremium;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public boolean isWeekend() {
        return isWeekend;
    }

    public boolean isPremiumUser() {
        return isPremium;
    }
}
