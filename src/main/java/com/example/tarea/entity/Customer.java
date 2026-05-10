package com.example.tarea.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "El tipo de documento es obligatorio")
    @Column(name = "document_type", nullable = false)
    private String documentType; // "DNI" o "RUC"

    @NotBlank(message = "El número de documento es obligatorio")
    @Column(name = "document", nullable = false, unique = true)
    private String document;

    // Constructores, getters y setters
    public Customer() {}

    public Customer(String name, String documentType, String document) {
        this.name = name;
        this.documentType = documentType;
        this.document = document;
    }

    // Getters y Setters (todos)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }
}