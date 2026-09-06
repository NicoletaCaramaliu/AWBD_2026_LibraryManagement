package org.example.librarymanagement.service;

import org.example.librarymanagement.entity.Book;
import org.example.librarymanagement.entity.Category;
import org.example.librarymanagement.exception.DuplicateResourceException;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Fiction")
                .description("Fiction books")
                .books(new HashSet<>())
                .build();
    }

    @Test
    void create_shouldSaveCategory_whenCategoryDoesNotExist() {

        when(categoryRepository.findByName("Fiction"))
                .thenReturn(Optional.empty());

        when(categoryRepository.save(category))
                .thenReturn(category);

        Category result = categoryService.create(category);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Fiction", result.getName());
        assertEquals("Fiction books", result.getDescription());

        verify(categoryRepository).findByName("Fiction");
        verify(categoryRepository).save(category);
    }

    @Test
    void create_shouldThrowException_whenCategoryAlreadyExists() {

        when(categoryRepository.findByName("Fiction"))
                .thenReturn(Optional.of(category));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> categoryService.create(category)
        );

        assertEquals(
                "Category with name 'Fiction' already exists",
                exception.getMessage()
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void getById_shouldReturnCategory_whenCategoryExists() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        Category result = categoryService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Fiction", result.getName());

        verify(categoryRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowException_whenCategoryDoesNotExist() {

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.getById(99L)
        );

        assertEquals(
                "Category with id 99 was not found",
                exception.getMessage()
        );

        verify(categoryRepository).findById(99L);
    }

    @Test
    void getAll_shouldReturnAllCategories() {

        Category secondCategory = Category.builder()
                .id(2L)
                .name("History")
                .description("History books")
                .books(new HashSet<>())
                .build();

        when(categoryRepository.findAll())
                .thenReturn(List.of(category, secondCategory));

        List<Category> result = categoryService.getAll();

        assertEquals(2, result.size());
        assertEquals("Fiction", result.get(0).getName());
        assertEquals("History", result.get(1).getName());

        verify(categoryRepository).findAll();
    }

    @Test
    void update_shouldUpdateCategory_whenDataIsValid() {

        Category updatedCategory = Category.builder()
                .name("Science Fiction")
                .description("Science fiction books")
                .build();

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.findByName("Science Fiction"))
                .thenReturn(Optional.empty());

        when(categoryRepository.save(category))
                .thenReturn(category);

        Category result = categoryService.update(
                1L,
                updatedCategory
        );

        assertEquals(
                "Science Fiction",
                result.getName()
        );

        assertEquals(
                "Science fiction books",
                result.getDescription()
        );

        verify(categoryRepository).save(category);
    }

    @Test
    void update_shouldThrowException_whenAnotherCategoryWithSameNameExists() {

        Category updatedCategory = Category.builder()
                .name("History")
                .description("Updated description")
                .build();

        Category duplicateCategory = Category.builder()
                .id(2L)
                .name("History")
                .description("Another category")
                .build();

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(categoryRepository.findByName("History"))
                .thenReturn(Optional.of(duplicateCategory));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> categoryService.update(
                        1L,
                        updatedCategory
                )
        );

        assertEquals(
                "Another category with this name already exists",
                exception.getMessage()
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void delete_shouldDeleteCategory_whenCategoryHasNoBooks() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void delete_shouldThrowException_whenCategoryHasBooks() {

        Book book = new Book();
        category.getBooks().add(book);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> categoryService.delete(1L)
        );

        assertEquals(
                "Category cannot be deleted because books are associated with it",
                exception.getMessage()
        );

        verify(categoryRepository, never())
                .delete(any(Category.class));
    }
}