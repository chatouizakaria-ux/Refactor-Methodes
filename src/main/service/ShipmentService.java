package main.service;

import main.domain.Cargo;
import main.domain.Customer;
import main.domain.Shipment;

public class ShipmentService {

    private static final double PRIORITY_THRESHOLD = 2000;

    private final PricingService pricingService;
    private final PermissionService permissionService;
    private final ManifestRepository repository;
    private final NotificationService notificationService;

    public ShipmentService(PricingService pricingService, PermissionService permissionService,
                           ManifestRepository repository, NotificationService notificationService) {
        this.pricingService = pricingService;
        this.permissionService = permissionService;
        this.repository = repository;
        this.notificationService = notificationService;
    }

    public String validateCalculatePrintSaveAndNotify(Shipment shipment) {
        String eligibilityError = validateCustomerEligibility(shipment.getCustomer());
        if (eligibilityError != null) {
            return eligibilityError;
        }
        if (shipment.getCargo().isEmpty()) {
            return "ERROR_EMPTY";
        }

        double totalWeight = totalWeight(shipment.getCargo());
        double totalValue = totalDeclaredValue(shipment.getCargo());
        boolean hazardous = hasHazardousCargo(shipment.getCargo());

        if (totalWeight > shipment.getShip().getCapacity()) {
            return "ERROR_CAPACITY";
        }
        if (hazardous && !permissionService.canCarryHazardous(shipment.getShip())) {
            return "ERROR_PERMISSION";
        }

        double total = calculateTotal(shipment, totalWeight, totalValue, hazardous);
        shipment.setTotal(total);
        shipment.setStatus("READY");

        return finalizeShipment(shipment, total);
    }

    private String validateCustomerEligibility(Customer customer) {
        if (!customer.isActive() || customer.isSuspended()) {
            return "ERROR_CUSTOMER";
        }
        return null;
    }

    private double totalWeight(Iterable<Cargo> cargo) {
        double total = 0;
        for (Cargo item : cargo) {
            total += item.getWeight();
        }
        return total;
    }

    private double totalDeclaredValue(Iterable<Cargo> cargo) {
        double total = 0;
        for (Cargo item : cargo) {
            total += item.getDeclaredValue();
        }
        return total;
    }

    private boolean hasHazardousCargo(Iterable<Cargo> cargo) {
        for (Cargo item : cargo) {
            if (item.isHazardous()) {
                return true;
            }
        }
        return false;
    }

    private double calculateTotal(Shipment shipment, double totalWeight, double totalValue, boolean hazardous) {
        double total = pricingService.calculatePrice(
                totalWeight, totalValue, hazardous,
                shipment.getOrigin(), shipment.getDestination(),
                shipment.getCustomer(), shipment.getDepartureDate());
        total += pricingService.calculateInsurance(totalValue, hazardous, shipment.getCustomer());
        return total;
    }

    private String finalizeShipment(Shipment shipment, double total) {
        String category = total > PRIORITY_THRESHOLD ? "PRIORITY" : "REGULAR";
        repository.save(shipment);
        String confirmation = notificationService.confirmationFor(shipment);
        return category + " | " + shipment.getReference() + " | " + String.format("%.2f", total) + " | " + confirmation;
    }
}