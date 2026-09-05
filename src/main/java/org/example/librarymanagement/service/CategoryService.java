package org.example.librarymanagement.service;

import lombok.RequiredArgsConstructor;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category create(Category category) {

        categoryRepository.findByName(category.getName())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Category with name '" +
                                    category.getName() +
                                    "' already exists"
                    );
                });

        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category with id " + id + " was not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category update(Long id, Category updatedCategory) {

        Category existing = getById(id);

        categoryRepository
                .findByName(updatedCategory.getName())
                .filter(category -> !category.getId().equals(id))
                .ifPresent(category -> {
                    throw new DuplicateResourceException(
                            "Another category with this name already exists"
                    );
                });

        existing.setName(updatedCategory.getName());
        existing.setDescription(updatedCategory.getDescription());

        return categoryRepository.save(existing);
    }

    public void delete(Long id) {

        Category category = getById(id);

        if (!category.getBooks().isEmpty()) {
            throw new InvalidOperationException(
                    "Category cannot be deleted because books are associated with it"
            );
        }

        categoryRepository.delete(category);
    }
}