package com.mediamarktsaturn.azubi.core.product.model;


import com.mediamarktsaturn.azubi.Category;

// Erstellt id, name, category
public class Product {
    private int id;
    private String name;
    private Category category;

    // Variablen bestimmen
    public Product(int id, String name, Category category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public Product() {

    }

    // Bestimmt von den bestimmten variablen die genauen Produktnamen
    public void setCategory(Category category) {
        this.category = category;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }
}
