package com.example.tarea.repository;

import com.example.tarea.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    boolean existsByDocument(String document);
    boolean existsByDocumentAndIdNot(String document, Integer id);
}