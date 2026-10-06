package com.mediamarktsaturn.azubi;

import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart implements Cart {
    // Speichert alle Produkte im Warenkorb
    private final List<SalesProduct> salesProducts;

    public ShoppingCart() {
        this.salesProducts = new ArrayList<>();
    }

    // Produkt hinzufügen (mit Null-Check)
    @Override
    public void add(SalesProduct salesProduct) {
        if (salesProduct != null) {
            salesProducts.add(salesProduct);
        }
    }

    // Produkt aus Warenkorb entfernen
    @Override
    public void remove(SalesProduct salesProduct) {
        salesProducts.remove(salesProduct);
    }

    // Gesamtpreis aller Produkte berechnen
    @Override
    public double getTotalPrice() {
        double total = 0.0;
        for (SalesProduct product : salesProducts) {
            if (product != null) {
                total += product.getPrice();
            }

        }
        return total;
    }

    // Defensive Kopie der Produktliste zurückgeben
    @Override
    public List<SalesProduct> getSalesProducts() {
        return new ArrayList<>(salesProducts);
    }

    @Override
    public String getItemCount() {
        return "";
    }

    // Debug-Ausgabe: Zeigt Warenkorb-Inhalt und Gesamtpreis
    public void printCartContent() {
        System.out.println("=== Shopping Cart Content ===");
        if (salesProducts.isEmpty()) {
            System.out.println("Cart is empty");
        } else {
            for (SalesProduct product : salesProducts) {
                System.out.println(product);
            }
            System.out.printf("Total Price: %.2f€%n", getTotalPrice());
        }
        System.out.println("=============================");
    }
}
