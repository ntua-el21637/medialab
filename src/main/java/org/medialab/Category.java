package org.medialab;

/**
 * Represents a document category within the MediaLab system.
 * Categories are used to organize documents and manage user access levels.
 * * @author Alexandra Soufleri
 * @version 1.0
 */
public class Category {
    private String name;

    /**
     * Constructs a new Category with the specified name.
     * * @param name The unique name of the category (e.g., "Technology", "Science").
     */
    public Category(String name) {

        this.name = name;
    }

    /**
     * Retrieves the name of this category.
     * * @return A String representing the category name.
     */
    public String getName() { return name; }

    /**
     * Updates the name of this category.
     * This is typically used by administrators during a rename operation.
     * * @param name The new name to be assigned to this category.
     */
    public void setName(String name) { this.name = name; }

}
