package org.example.librarymanagement.service;

import lombok.RequiredArgsConstructor;
import org.example.librarymanagement.entity.Publisher;
import org.example.librarymanagement.exception.DuplicateResourceException;
import org.example.librarymanagement.exception.InvalidOperationException;
import org.example.librarymanagement.exception.ResourceNotFoundException;
import org.example.librarymanagement.repository.PublisherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PublisherService {

    private final PublisherRepository publisherRepository;

    public Publisher create(Publisher publisher) {

        publisherRepository.findByName(publisher.getName())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Publisher with name '" +
                                    publisher.getName() +
                                    "' already exists"
                    );
                });

        return publisherRepository.save(publisher);
    }

    @Transactional(readOnly = true)
    public Publisher getById(Long id) {
        return publisherRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Publisher with id " + id + " was not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Publisher> getAll() {
        return publisherRepository.findAll();
    }

    public Publisher update(Long id, Publisher updatedPublisher) {

        Publisher existing = getById(id);

        publisherRepository.findByName(updatedPublisher.getName())
                .filter(publisher -> !publisher.getId().equals(id))
                .ifPresent(publisher -> {
                    throw new DuplicateResourceException(
                            "Another publisher with this name already exists"
                    );
                });

        existing.setName(updatedPublisher.getName());
        existing.setCountry(updatedPublisher.getCountry());

        return publisherRepository.save(existing);
    }

    public void delete(Long id) {

        Publisher publisher = getById(id);

        if (!publisher.getBooks().isEmpty()) {
            throw new InvalidOperationException(
                    "Publisher cannot be deleted because books are associated with it"
            );
        }

        publisherRepository.delete(publisher);
    }
}