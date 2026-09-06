package org.example.librarymanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagement.entity.Category;
import org.example.librarymanagement.exception.DuplicateResourceException;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category create(Category category) {

        log.debug(
                "Creating category with name={}",
                category.getName()
        );

        categoryRepository.findByName(category.getName())
                .ifPresent(existing -> {
                    log.error(
                            "Cannot create category. Category with name '{}' already exists",
                            category.getName()
                    );

                    throw new DuplicateResourceException(
                            "Category with name '" +
                                    category.getName() +
                                    "' already exists"
                    );
                });

        Category savedCategory = categoryRepository.save(category);

        log.info(
                "Category created successfully. id={}",
                savedCategory.getId()
        );

        return savedCategory;
    }

    @Transactional(readOnly = true)
    public Category getById(Long id) {

        log.debug("Searching for category with id={}", id);

        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(
                            "Category with id {} was not found",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Category with id " + id + " was not found"
                    );
                });
    }

    @Transactional(readOnly = true)
    public List<Category> getAll() {

        log.debug("Retrieving all categories");

        return categoryRepository.findAll();
    }

    public Category update(Long id, Category updatedCategory) {

        log.debug("Updating category with id={}", id);

        Category existing = getById(id);

        categoryRepository
                .findByName(updatedCategory.getName())
                .filter(category -> !category.getId().equals(id))
                .ifPresent(category -> {
                    log.error(
                            "Cannot update category with id {}. Another category with name '{}' already exists",
                            id,
                            updatedCategory.getName()
                    );

                    throw new DuplicateResourceException(
                            "Another category with this name already exists"
                    );
                });

        existing.setName(updatedCategory.getName());
        existing.setDescription(updatedCategory.getDescription());

        Category savedCategory = categoryRepository.save(existing);

        log.info(
                "Category with id {} was updated successfully",
                id
        );

        return savedCategory;
    }

    public void delete(Long id) {

        log.debug("Deleting category with id={}", id);

        Category category = getById(id);

        if (!category.getBooks().isEmpty()) {

            log.error(
                    "Cannot delete category with id {} because books are associated with it",
                    id
            );

            throw new InvalidOperationException(
                    "Category cannot be deleted because books are associated with it"
            );
        }

        categoryRepository.delete(category);

        log.info(
                "Category with id {} was deleted successfully",
                id
        );
    }
}