package com.joysistvi.clothingretailapp.service;

import com.joysistvi.clothingretailapp.model.Categories;
import com.joysistvi.clothingretailapp.repository.CategoriesRepository;
import com.joysistvi.clothingretailapp.repository.ProductsRepository;

import java.util.ArrayList;
import java.util.List;

public class CategoriesServiceImpl implements CategoriesService {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 255;

    private final CategoriesRepository categoriesRepository;
    private final ProductsRepository productsRepository;

    public CategoriesServiceImpl(CategoriesRepository categoriesRepository,
                                 ProductsRepository productsRepository) {
        this.categoriesRepository = categoriesRepository;
        this.productsRepository = productsRepository;
    }

    @Override
    public List<Categories> findAllActive() {
        List<Categories> categories = categoriesRepository.findAllActive();
        return categories != null ? categories : new ArrayList<>();
    }

    @Override
    public List<Categories> findAllArchived() {
        List<Categories> categories = categoriesRepository.findAllArchived();
        return categories != null ? categories : new ArrayList<>();
    }


    @Override
    public boolean save(Categories category) {
        if (!isValidCategory(category)) {
            return false;
        }

        return categoriesRepository.save(category);
    }

    @Override
    public boolean update(Categories category) {
        if (!isValidCategory(category)) {
            return false;
        }

        if (category.getId() <= 0) {
            System.out.println("Validation Error: Category ID must be greater than 0.");
            return false;
        }

        if (categoriesRepository.findById(category.getId()) == null) {
            System.out.println("Validation Error: Category with ID " + category.getId() + " was not found.");
            return false;
        }

        return categoriesRepository.update(category);
    }

    @Override
    public boolean deleteById(int id) {
        if (id <= 0) {
            System.out.println("Validation Error: Category ID must be greater than 0.");
            return false;
        }

        if (categoriesRepository.findById(id) == null) {
            System.out.println("Validation Error: Category with ID " + id + " was not found.");
            return false;
        }

        if (hasActiveProducts(id)) {
            return false;
        }

        return categoriesRepository.deleteById(id);
    }


    @Override
    public boolean archiveById(int id) {
        if (id <= 0) {
            System.out.println("Validation Error: Category ID must be greater than 0.");
            return false;
        }

        Categories category = categoriesRepository.findById(id);
        if (category == null) {
            System.out.println("Validation Error: Category with ID " + id + " was not found.");
            return false;
        }

        if (category.isArchived()) {
            System.out.println("Validation Error: Category " + id + " is already archived.");
            return false;
        }

        if (hasActiveProducts(id)) {
            return false;
        }

        return categoriesRepository.archiveById(id);
    }

    @Override
    public boolean restoreById(int id) {
        if (id <= 0) {
            System.out.println("Validation Error: Category ID must be greater than 0.");
            return false;
        }

        Categories category = categoriesRepository.findById(id);
        if (category == null) {
            System.out.println("Validation Error: Category with ID " + id + " was not found.");
            return false;
        }

        if (!category.isArchived()) {
            System.out.println("Validation Error: Category " + id + " is not archived.");
            return false;
        }

        return categoriesRepository.restoreById(id);
    }


    @Override
    public List<Categories> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("Validation Error: Search keyword cannot be blank.");
            return new ArrayList<>();
        }

        List<Categories> results = categoriesRepository.searchByName(keyword.trim());
        return results != null ? results : new ArrayList<>();
    }


    @Override
    public Categories findById(int id) {
        if (id <= 0) {
            System.out.println("Validation Error: Category ID must be greater than 0.");
            return null;
        }

        return categoriesRepository.findById(id);
    }


    private boolean isValidCategory(Categories category) {
        if (category == null) {
            System.out.println("Validation Error: Category cannot be null.");
            return false;
        }

        String name = category.getName() == null ? "" : category.getName().trim();
        String description = category.getDescription() == null ? "" : category.getDescription().trim();

        if (name.isEmpty()) {
            System.out.println("Validation Error: Category name cannot be blank.");
            return false;
        }

        if (name.length() > MAX_NAME_LENGTH) {
            System.out.println("Validation Error: Category name cannot be longer than "
                    + MAX_NAME_LENGTH + " characters.");
            return false;
        }

        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            System.out.println("Validation Error: Description cannot be longer than "
                    + MAX_DESCRIPTION_LENGTH + " characters.");
            return false;
        }

        category.setName(name);
        category.setDescription(description);
        return true;
    }

    private boolean hasActiveProducts(int categoryId) {
        int total = productsRepository.countActiveByCategory(categoryId);
        if (total > 0) {
            System.out.println("Validation Error: Category " + categoryId + " still has "
                    + total + " active product(s). Archive or move them first.");
            return true;
        }
        return false;
    }
}