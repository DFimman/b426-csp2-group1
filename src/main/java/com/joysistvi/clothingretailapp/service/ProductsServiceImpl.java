package com.joysistvi.clothingretailapp.service;

import com.joysistvi.clothingretailapp.model.Categories;
import com.joysistvi.clothingretailapp.model.Products;
import com.joysistvi.clothingretailapp.repository.CategoriesRepository;
import com.joysistvi.clothingretailapp.repository.ProductsRepository;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProductsServiceImpl implements ProductsService {
    private static final int MAX_NAME_LENGTH = 150;
    // MariaDB TEXT supports at most 65,535 bytes, not 255 characters.
    private static final int MAX_DESCRIPTION_BYTES = 65535;

    private final ProductsRepository productsRepository;
    private final CategoriesRepository categoriesRepository;

    public ProductsServiceImpl(ProductsRepository productsRepository, CategoriesRepository categoriesRepository) {
        this.productsRepository = productsRepository;
        this.categoriesRepository = categoriesRepository;
    }

    @Override
    public List<Products> findAllActive() {
        List<Products> products = productsRepository.findAllActive();
        return products != null ? products : new ArrayList<>();
    }

    @Override
    public List<Products> findAllArchived() {
        List<Products> products = productsRepository.findAllArchived();
        return products != null ? products : new ArrayList<>();
    }

    @Override
    public List<Products> findActiveByCategory(int categoryId) {
        if (categoryId <= 0) {
            System.out.println("Validation Error: Category ID must be greater than 0.");
            return new ArrayList<>();
        }
        List<Products> products = productsRepository.findActiveByCategory(categoryId);
        return products != null ? products : new ArrayList<>();
    }

    @Override
    public int countActiveByCategory(int categoryId) {
        if (categoryId <= 0) {
            System.out.println("Validation Error: Category ID must be greater than 0.");
            return 0;
        }
        return productsRepository.countActiveByCategory(categoryId);
    }

    @Override
    public boolean save(Products product) {
        return isValidProduct(product) && productsRepository.save(product);
    }

    @Override
    public boolean update(Products product) {
        if (!isValidProduct(product)) return false;
        if (product.getId() <= 0) {
            System.out.println("Validation Error: Product ID must be greater than 0.");
            return false;
        }
        if (productsRepository.findById(product.getId()) == null) {
            System.out.println("Validation Error: Product with ID " + product.getId() + " was not found.");
            return false;
        }
        return productsRepository.update(product);
    }

    // Permanent deletion is kept only for maintenance; use archive during normal operation.
    @Override
    public boolean deleteById(int id) {
        if (id <= 0) {
            System.out.println("Validation Error: Product ID must be greater than 0.");
            return false;
        }
        if (productsRepository.findById(id) == null) {
            System.out.println("Validation Error: Product with ID " + id + " was not found.");
            return false;
        }
        // Foreign keys prevent deleting products referenced by variants.
        return productsRepository.deleteById(id);
    }

    @Override
    public boolean archiveById(int id) {
        if (id <= 0) {
            System.out.println("Validation Error: Product ID must be greater than 0.");
            return false;
        }
        Products product = productsRepository.findById(id);
        if (product == null) {
            System.out.println("Validation Error: Product with ID " + id + " was not found.");
            return false;
        }
        if (product.isArchived()) {
            System.out.println("Validation Error: Product " + id + " is already archived.");
            return false;
        }
        return productsRepository.archiveById(id);
    }

    @Override
    public boolean restoreById(int id) {
        if (id <= 0) {
            System.out.println("Validation Error: Product ID must be greater than 0.");
            return false;
        }
        Products product = productsRepository.findById(id);
        if (product == null) {
            System.out.println("Validation Error: Product with ID " + id + " was not found.");
            return false;
        }
        if (!product.isArchived()) {
            System.out.println("Validation Error: Product " + id + " is not archived.");
            return false;
        }
        // The parent category must be active before restoring a product.
        if (!isUsableCategory(product.getCategoryId())) return false;
        return productsRepository.restoreById(id);
    }

    @Override
    public List<Products> searchByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            System.out.println("Validation Error: Search keyword cannot be blank.");
            return new ArrayList<>();
        }
        List<Products> result = productsRepository.searchByName(keyword.trim());
        return result != null ? result : new ArrayList<>();
    }

    @Override
    public Products findById(int id) {
        if (id <= 0) {
            System.out.println("Validation Error: Product ID must be greater than 0.");
            return null;
        }
        return productsRepository.findById(id);
    }

    private boolean isValidProduct(Products product) {
        if (product == null) {
            System.out.println("Validation Error: Product cannot be null.");
            return false;
        }
        String name = product.getName() == null ? "" : product.getName().trim();
        String description = product.getDescription() == null ? "" : product.getDescription().trim();
        if (name.isBlank()) {
            System.out.println("Validation Error: Product name cannot be blank.");
            return false;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            System.out.println("Validation Error: Product name cannot be longer than 150 characters.");
            return false;
        }
        if (description.getBytes(StandardCharsets.UTF_8).length > MAX_DESCRIPTION_BYTES) {
            System.out.println("Validation Error: Product description exceeds TEXT column size.");
            return false;
        }
        if (!isUsableCategory(product.getCategoryId())) return false;
        product.setName(name);
        product.setDescription(description);
        return true;
    }

    private boolean isUsableCategory(int categoryId) {
        if (categoryId <= 0) {
            System.out.println("Validation Error: Category ID must be greater than 0.");
            return false;
        }
        Categories category = categoriesRepository.findById(categoryId);
        if (category == null) {
            System.out.println("Validation Error: Category with ID " + categoryId + " was not found.");
            return false;
        }
        if (category.isArchived()) {
            System.out.println("Validation Error: Category " + categoryId + " is archived. Restore it first.");
            return false;
        }
        return true;
    }
}
