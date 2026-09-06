package org.example.librarymanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "book_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "ISBN is required")
    @Column(nullable = false, unique = true)
    private String isbn;

    @NotNull(message = "Number of pages is required")
    @Min(value = 1, message = "Number of pages must be greater than 0")
    @Column(nullable = false)
    private Integer numberOfPages;

    @NotBlank(message = "Language is required")
    @Column(nullable = false)
    private String language;

    @NotNull(message = "Publication year is required")
    @Column(nullable = false)
    private Integer publicationYear;

    @Column(length = 3000)
    private String description;
}