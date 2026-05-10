package com.example.tarea.controller;

import com.example.tarea.entity.Product;
import com.example.tarea.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/productos")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // Listar productos
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "productos";
    }

    // Mostrar formulario para nuevo producto
    @GetMapping("/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("product", new Product());
        return "producto_form";
    }

    // Mostrar formulario para editar producto
    @GetMapping("/editar/{id}")
    public String editarProducto(@PathVariable Integer id, Model model) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/productos";
        }
        model.addAttribute("product", product);
        return "producto_form";
    }

    // Guardar producto (crear o editar)
    @PostMapping("/guardar")
    public String guardarProducto(@Valid @ModelAttribute("product") Product product,
                                  BindingResult result,
                                  Model model) {

        // Validación manual de unicidad de nombre
        if (product.getId() == null) {
            if (productRepository.existsByName(product.getName())) {
                result.rejectValue("name", "error.product", "Ya existe un producto con ese nombre");
            }
        } else {
            if (productRepository.existsByNameAndIdNot(product.getName(), product.getId())) {
                result.rejectValue("name", "error.product", "Ya existe otro producto con ese nombre");
            }
        }

        // Manejo de error de tipo numérico (precio o stock no convertibles)

        if (result.hasErrors()) {
            return "producto_form";
        }

        productRepository.save(product);
        return "redirect:/productos";
    }

    // Eliminar producto
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Integer id) {
        productRepository.deleteById(id);
        return "redirect:/productos";
    }
}