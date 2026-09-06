package org.example.librarymanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class PublisherService {

    private final PublisherRepository publisherRepository;

    public Publisher create(Publisher publisher) {

        log.debug(
                "Creating publisher with name={}",
                publisher.getName()
        );

        publisherRepository.findByName(publisher.getName())
                .ifPresent(existing -> {

                    log.error(
                            "Cannot create publisher. Publisher with name '{}' already exists",
                            publisher.getName()
                    );

                    throw new DuplicateResourceException(
                            "Publisher with name '" +
                                    publisher.getName() +
                                    "' already exists"
                    );
                });

        Publisher savedPublisher = publisherRepository.save(publisher);

        log.info(
                "Publisher created successfully. id={}",
                savedPublisher.getId()
        );

        return savedPublisher;
    }

    @Transactional(readOnly = true)
    public Publisher getById(Long id) {

        log.debug("Searching for publisher with id={}", id);

        return publisherRepository.findById(id)
                .orElseThrow(() -> {

                    log.error(
                            "Publisher with id {} was not found",
                            id
                    );

                    return new ResourceNotFoundException(
                            "Publisher with id " + id + " was not found"
                    );
                });
    }

    @Transactional(readOnly = true)
    public List<Publisher> getAll() {

        log.debug("Retrieving all publishers");

        return publisherRepository.findAll();
    }

    public Publisher update(Long id, Publisher updatedPublisher) {

        log.debug("Updating publisher with id={}", id);

        Publisher existing = getById(id);

        publisherRepository.findByName(updatedPublisher.getName())
                .filter(publisher -> !publisher.getId().equals(id))
                .ifPresent(publisher -> {

                    log.error(
                            "Cannot update publisher with id {}. Another publisher with name '{}' already exists",
                            id,
                            updatedPublisher.getName()
                    );

                    throw new DuplicateResourceException(
                            "Another publisher with this name already exists"
                    );
                });

        existing.setName(updatedPublisher.getName());
        existing.setCountry(updatedPublisher.getCountry());

        Publisher savedPublisher = publisherRepository.save(existing);

        log.info(
                "Publisher with id {} was updated successfully",
                id
        );

        return savedPublisher;
    }

    public void delete(Long id) {

        log.debug("Deleting publisher with id={}", id);

        Publisher publisher = getById(id);

        if (!publisher.getBooks().isEmpty()) {

            log.error(
                    "Cannot delete publisher with id {} because books are associated with it",
                    id
            );

            throw new InvalidOperationException(
                    "Publisher cannot be deleted because books are associated with it"
            );
        }

        publisherRepository.delete(publisher);

        log.info(
                "Publisher with id {} was deleted successfully",
                id
        );
    }
}