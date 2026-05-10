package com.example.tarea.controller;

import com.example.tarea.entity.Customer;
import com.example.tarea.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/clientes")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    // Listar clientes
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        return "clientes";
    }

    // Mostrar formulario para nuevo cliente
    @GetMapping("/nuevo")
    public String nuevoCliente(Model model) {
        model.addAttribute("customer", new Customer());
        return "cliente_form";
    }

    // Mostrar formulario para editar cliente
    @GetMapping("/editar/{id}")
    public String editarCliente(@PathVariable Integer id, Model model) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) {
            return "redirect:/clientes";
        }
        model.addAttribute("customer", customer);
        return "cliente_form";
    }

    // Guardar cliente (crear o editar)
    @PostMapping("/guardar")
    public String guardarCliente(@Valid @ModelAttribute("customer") Customer customer,
                                 BindingResult result,
                                 Model model) {

        // Validación personalizada: formato según tipo de documento
        String docType = customer.getDocumentType();
        String docNum = customer.getDocument();

        if (docType != null && docNum != null) {
            if ("DNI".equals(docType) && !docNum.matches("\\d{8}")) {
                result.rejectValue("document", "error.customer", "El DNI debe tener exactamente 8 dígitos numéricos");
            } else if ("RUC".equals(docType) && !docNum.matches("\\d{11}")) {
                result.rejectValue("document", "error.customer", "El RUC debe tener exactamente 11 dígitos numéricos");
            }
        }

        // Validación de unicidad del documento
        if (customer.getId() == null) {
            // Creando nuevo
            if (customerRepository.existsByDocument(customer.getDocument())) {
                result.rejectValue("document", "error.customer", "Ya existe un cliente con ese número de documento");
            }
        } else {
            // Editando: excluir el propio
            if (customerRepository.existsByDocumentAndIdNot(customer.getDocument(), customer.getId())) {
                result.rejectValue("document", "error.customer", "Ya existe otro cliente con ese número de documento");
            }
        }

        if (result.hasErrors()) {
            return "cliente_form";
        }

        customerRepository.save(customer);
        return "redirect:/clientes";
    }

    // Eliminar cliente
    @GetMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Integer id) {
        customerRepository.deleteById(id);
        return "redirect:/clientes";
    }
}