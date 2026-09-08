package main.service;

import main.domain.Customer;
import main.domain.Planet;

import java.time.LocalDate;
import java.time.Month;

public class PricingService {

    private static final double WEIGHT_RATE = 2.25;

    private static final double HIGH_VALUE_THRESHOLD = 10000;
    private static final double HIGH_VALUE_SURCHARGE_RATE = 0.015;

    private static final double HAZARDOUS_SURCHARGE_PERCENT = 0.20;

    private static final int HIGH_SECURITY_LEVEL = 4;
    private static final double HIGH_SECURITY_SURCHARGE = 125;

    private static final double SECTOR_CHANGE_SURCHARGE = 80;

    private static final double PEAK_SEASON_SURCHARGE = 45;

    private static final int LOYALTY_DISCOUNT_YEARS = 5;
    private static final double LOYALTY_DISCOUNT_MULTIPLIER = 0.90;

    private static final double INSURANCE_RATE = 0.02;
    private static final double HAZARDOUS_INSURANCE_SURCHARGE = 75;
    private static final int INSURANCE_LOYALTY_YEARS = 10;
    private static final double INSURANCE_LOYALTY_DISCOUNT = 10;

    private static final double PRIORITY_THRESHOLD = 2000;

    public double increaseByPercent(double price, double percent) {
        return price + price * percent;
    }

    public double calculatePrice(double weight, double declaredValue, boolean hazardous,
                                 Planet origin, Planet destination,
                                 Customer customer, LocalDate departureDate) {
        double result = weight * WEIGHT_RATE;

        if (declaredValue > HIGH_VALUE_THRESHOLD) {
            result += declaredValue * HIGH_VALUE_SURCHARGE_RATE;
        }
        if (hazardous) {
            result = increaseByPercent(result, HAZARDOUS_SURCHARGE_PERCENT);
        }
        if (isHighSecurityRoute(origin, destination)) {
            result += HIGH_SECURITY_SURCHARGE;
        }
        if (isSectorChange(origin, destination)) {
            result += SECTOR_CHANGE_SURCHARGE;
        }
        if (isPeakSeason(departureDate)) {
            result += PEAK_SEASON_SURCHARGE;
        }
        if (isEligibleForLoyaltyDiscount(customer)) {
            result *= LOYALTY_DISCOUNT_MULTIPLIER;
        }
        return result;
    }

    public double calculateInsurance(double declaredValue, boolean hazardous, Customer customer) {
        double insurance = declaredValue * INSURANCE_RATE;
        if (hazardous) {
            insurance += HAZARDOUS_INSURANCE_SURCHARGE;
        }
        if (customer.getLoyaltyYears() >= INSURANCE_LOYALTY_YEARS) {
            insurance -= INSURANCE_LOYALTY_DISCOUNT;
        }
        return Math.max(insurance, 0);
    }

    public String priceCategory(double price) {
        return price > 1000 ? "HIGH" : "STANDARD";
    }

    public String pricingSummary(double total) {
        return total >= PRIORITY_THRESHOLD ? "PRIORITY" : "REGULAR";
    }

    public double calculateRouteSurcharge(Planet origin, Planet destination) {
        double securitySurcharge = (origin.getSecurityLevel() + destination.getSecurityLevel()) * 12.5;
        double sectorSurcharge = isSectorChange(origin, destination) ? SECTOR_CHANGE_SURCHARGE : 0;
        return securitySurcharge + sectorSurcharge;
    }

    private boolean isHighSecurityRoute(Planet origin, Planet destination) {
        return origin.getSecurityLevel() >= HIGH_SECURITY_LEVEL
                || destination.getSecurityLevel() >= HIGH_SECURITY_LEVEL;
    }

    private boolean isSectorChange(Planet origin, Planet destination) {
        return !origin.getSector().equals(destination.getSector());
    }

    private boolean isPeakSeason(LocalDate departureDate) {
        Month month = departureDate.getMonth();
        return month == Month.DECEMBER || month == Month.JANUARY || month == Month.FEBRUARY;
    }

    private boolean isEligibleForLoyaltyDiscount(Customer customer) {
        return customer.getLoyaltyYears() >= LOYALTY_DISCOUNT_YEARS
                && customer.isActive()
                && !customer.isSuspended();
    }
}