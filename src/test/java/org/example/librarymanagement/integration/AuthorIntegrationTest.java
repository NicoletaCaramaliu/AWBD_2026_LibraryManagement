package org.example.librarymanagement.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.librarymanagement.entity.Author;
import org.example.librarymanagement.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    void setUp() {
        authorRepository.deleteAll();
    }

    @Test
    void authorCrud_shouldWorkEndToEnd() throws Exception {

        // 1. CREATE
        Author author = Author.builder()
                .firstName("George")
                .lastName("Orwell")
                .build();

        String createResponse = mockMvc.perform(
                        post("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(author))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("George"))
                .andExpect(jsonPath("$.lastName").value("Orwell"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Author createdAuthor = objectMapper.readValue(
                createResponse,
                Author.class
        );

        Long authorId = createdAuthor.getId();

        // 2. GET BY ID
        mockMvc.perform(
                        get("/api/authors/{id}", authorId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(authorId))
                .andExpect(jsonPath("$.firstName").value("George"))
                .andExpect(jsonPath("$.lastName").value("Orwell"));

        // 3. UPDATE
        Author updatedAuthor = Author.builder()
                .firstName("Eric")
                .lastName("Blair")
                .build();

        mockMvc.perform(
                        put("/api/authors/{id}", authorId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                updatedAuthor
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Eric"))
                .andExpect(jsonPath("$.lastName").value("Blair"));

        // 4. GET ALL
        mockMvc.perform(
                        get("/api/authors")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("Eric"))
                .andExpect(jsonPath("$[0].lastName").value("Blair"));

        // 5. DELETE
        mockMvc.perform(
                        delete("/api/authors/{id}", authorId)
                )
                .andExpect(status().isNoContent());

        // 6. VERIFY DELETE
        mockMvc.perform(
                        get("/api/authors/{id}", authorId)
                )
                .andExpect(status().isNotFound());
    }
}