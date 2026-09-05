package org.example.librarymanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publishers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Publisher name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Country is required")
    @Column(nullable = false)
    private String country;

    @OneToMany(mappedBy = "publisher")
    @Builder.Default
    private List<Book> books = new ArrayList<>();
}