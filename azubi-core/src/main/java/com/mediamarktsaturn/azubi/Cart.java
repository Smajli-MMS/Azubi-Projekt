package com.mediamarktsaturn.azubi;

import java.util.List;

public interface Cart {

    /**
     * Parameter und sagt welches Produkt hinzugefügt wird
     */
    void add(SalesProduct salesProduct);

    /**
     * entfernt Produkt
     */
    void remove(SalesProduct salesProduct);

    double getTotalPrice();

    List<SalesProduct> getSalesProducts();


    // Zählt die Produkte zusammen
    String getItemCount();
}
