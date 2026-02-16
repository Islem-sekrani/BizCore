package com.gestion.interfaces;

import com.gestion.entities.Product;
import java.util.List;

/**
 * Interface for Product Service operations
 * Defines the contract for product management
 */
public interface IProductService {

    /**
     * Get all products from the database
     * 
     * @return List of all products
     */
    List<Product> getAllProducts();

    /**
     * Add a new product to the database
     * 
     * @param product Product to add
     * @return true if successful, false otherwise
     */
    boolean addProduct(Product product);

    /**
     * Update an existing product in the database
     * 
     * @param product Product to update
     * @return true if successful, false otherwise
     */
    boolean updateProduct(Product product);

    /**
     * Delete a product from the database
     * 
     * @param idProduit ID of the product to delete
     * @return true if successful, false otherwise
     */
    boolean deleteProduct(int idProduit);

    /**
     * Get a product by ID
     * 
     * @param idProduit ID of the product
     * @return Product object or null if not found
     */
    Product getProductById(int idProduit);
}
