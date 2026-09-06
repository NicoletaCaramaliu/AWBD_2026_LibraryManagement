package org.example.librarymanagement.service;

import org.example.librarymanagement.entity.Book;
import org.example.librarymanagement.entity.Publisher;
import org.example.librarymanagement.exception.DuplicateResourceException;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.PublisherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

    @Mock
    private PublisherRepository publisherRepository;

    @InjectMocks
    private PublisherService publisherService;

    private Publisher publisher;

    @BeforeEach
    void setUp() {
        publisher = Publisher.builder()
                .id(1L)
                .name("Penguin Books")
                .country("United Kingdom")
                .books(new ArrayList<>())
                .build();
    }

    @Test
    void create_shouldSavePublisher_whenPublisherDoesNotExist() {

        when(publisherRepository.findByName("Penguin Books"))
                .thenReturn(Optional.empty());

        when(publisherRepository.save(publisher))
                .thenReturn(publisher);

        Publisher result = publisherService.create(publisher);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Penguin Books", result.getName());
        assertEquals("United Kingdom", result.getCountry());

        verify(publisherRepository).findByName("Penguin Books");
        verify(publisherRepository).save(publisher);
    }

    @Test
    void create_shouldThrowException_whenPublisherAlreadyExists() {

        when(publisherRepository.findByName("Penguin Books"))
                .thenReturn(Optional.of(publisher));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> publisherService.create(publisher)
        );

        assertEquals(
                "Publisher with name 'Penguin Books' already exists",
                exception.getMessage()
        );

        verify(publisherRepository, never())
                .save(any(Publisher.class));
    }

    @Test
    void getById_shouldReturnPublisher_whenPublisherExists() {

        when(publisherRepository.findById(1L))
                .thenReturn(Optional.of(publisher));

        Publisher result = publisherService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Penguin Books", result.getName());
        assertEquals("United Kingdom", result.getCountry());

        verify(publisherRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowException_whenPublisherDoesNotExist() {

        when(publisherRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> publisherService.getById(99L)
        );

        assertEquals(
                "Publisher with id 99 was not found",
                exception.getMessage()
        );

        verify(publisherRepository).findById(99L);
    }

    @Test
    void getAll_shouldReturnAllPublishers() {

        Publisher secondPublisher = Publisher.builder()
                .id(2L)
                .name("HarperCollins")
                .country("United States")
                .books(new ArrayList<>())
                .build();

        when(publisherRepository.findAll())
                .thenReturn(List.of(publisher, secondPublisher));

        List<Publisher> result = publisherService.getAll();

        assertEquals(2, result.size());
        assertEquals("Penguin Books", result.get(0).getName());
        assertEquals("HarperCollins", result.get(1).getName());

        verify(publisherRepository).findAll();
    }

    @Test
    void update_shouldUpdatePublisher_whenDataIsValid() {

        Publisher updatedPublisher = Publisher.builder()
                .name("Penguin Random House")
                .country("United Kingdom")
                .build();

        when(publisherRepository.findById(1L))
                .thenReturn(Optional.of(publisher));

        when(publisherRepository.findByName("Penguin Random House"))
                .thenReturn(Optional.empty());

        when(publisherRepository.save(publisher))
                .thenReturn(publisher);

        Publisher result = publisherService.update(
                1L,
                updatedPublisher
        );

        assertEquals(
                "Penguin Random House",
                result.getName()
        );

        assertEquals(
                "United Kingdom",
                result.getCountry()
        );

        verify(publisherRepository).save(publisher);
    }

    @Test
    void update_shouldThrowException_whenAnotherPublisherWithSameNameExists() {

        Publisher updatedPublisher = Publisher.builder()
                .name("HarperCollins")
                .country("United States")
                .build();

        Publisher duplicatePublisher = Publisher.builder()
                .id(2L)
                .name("HarperCollins")
                .country("United States")
                .build();

        when(publisherRepository.findById(1L))
                .thenReturn(Optional.of(publisher));

        when(publisherRepository.findByName("HarperCollins"))
                .thenReturn(Optional.of(duplicatePublisher));

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> publisherService.update(
                        1L,
                        updatedPublisher
                )
        );

        assertEquals(
                "Another publisher with this name already exists",
                exception.getMessage()
        );

        verify(publisherRepository, never())
                .save(any(Publisher.class));
    }

    @Test
    void delete_shouldDeletePublisher_whenPublisherHasNoBooks() {

        when(publisherRepository.findById(1L))
                .thenReturn(Optional.of(publisher));

        publisherService.delete(1L);

        verify(publisherRepository).delete(publisher);
    }

    @Test
    void delete_shouldThrowException_whenPublisherHasBooks() {

        Book book = new Book();
        publisher.getBooks().add(book);

        when(publisherRepository.findById(1L))
                .thenReturn(Optional.of(publisher));

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> publisherService.delete(1L)
        );

        assertEquals(
                "Publisher cannot be deleted because books are associated with it",
                exception.getMessage()
        );

        verify(publisherRepository, never())
                .delete(any(Publisher.class));
    }
}