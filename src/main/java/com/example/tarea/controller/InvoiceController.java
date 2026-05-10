package com.example.tarea.controller;

import com.example.tarea.entity.*;
import com.example.tarea.form.InvoiceForm;
import com.example.tarea.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/comprobantes")
public class InvoiceController {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceDetailRepository detailRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    // Listar comprobantesss
    @GetMapping
    public String listar(Model model) {
        List<Invoice> invoices = invoiceRepository.findAll();
        // Calcular total para cada invoice (suma de subtotales)
        model.addAttribute("invoices", invoices);
        return "comprobantes";
    }

    // Mostrar formulario de nuevo comprobanteee
    @GetMapping("/nuevo")
    public String nuevoComprobante(Model model) {
        model.addAttribute("invoiceForm", new InvoiceForm());
        model.addAttribute("clientes", customerRepository.findAll());
        model.addAttribute("productos", productRepository.findAll());
        return "comprobante_form";
    }

    // Guardar comprobantee
    @PostMapping("/guardar")
    @Transactional
    public String guardarComprobante(@Valid @ModelAttribute("invoiceForm") InvoiceForm form,
                                     BindingResult result,
                                     Model model) {

        // Validaciones adicionales que no pueden hacerse con anotaciones simpless
        Customer customer = customerRepository.findById(form.getCustomerId()).orElse(null);
        if (customer == null) {
            result.rejectValue("customerId", "error.invoiceForm", "Cliente no existe");
        } else {
            // Validar tipo de comprobante y tipo de documento del cliente
            if ("FACTURA".equals(form.getType()) && !"RUC".equals(customer.getDocumentType())) {
                result.rejectValue("type", "error.invoiceForm", "La factura solo puede emitirse a clientes con RUC");
            } else if ("BOLETA".equals(form.getType()) && !"DNI".equals(customer.getDocumentType())) {
                result.rejectValue("type", "error.invoiceForm", "La boleta solo puede emitirse a clientes con DNI");
            }
        }

        // Validar productos y cantidades
        Map<Integer, Integer> quantities = form.getQuantities();
        List<Integer> productIdsWithQuantity = new ArrayList<>();
        boolean hasAtLeastOneProduct = false;

        for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
            Integer productId = entry.getKey();
            Integer quantity = entry.getValue();
            if (quantity != null && quantity > 0) {
                hasAtLeastOneProduct = true;
                // Validar que no se repita producto (el mapa ya evita repetición por clave)
                // Validar stock
                Product product = productRepository.findById(productId).orElse(null);
                if (product == null) {
                    result.rejectValue("quantities[" + productId + "]", "error.invoiceForm", "Producto no existe");
                } else if (quantity > product.getStock()) {
                    result.rejectValue("quantities[" + productId + "]", "error.invoiceForm",
                            "Stock insuficiente para " + product.getName() + ". Stock disponible: " + product.getStock());
                } else if (quantity < 0) {
                    result.rejectValue("quantities[" + productId + "]", "error.invoiceForm",
                            "La cantidad no puede ser negativa");
                }
            }
        }

        if (!hasAtLeastOneProduct) {
            result.rejectValue("quantities", "error.invoiceForm", "Debe seleccionar al menos un producto y cantidad mayor a cero");
        }

        // en caso de q hay errores, volver al formulario con los mismos datos
        if (result.hasErrors()) {
            model.addAttribute("clientes", customerRepository.findAll());
            model.addAttribute("productos", productRepository.findAll());
            return "comprobante_form";
        }

        // Crear invoice
        Invoice invoice = new Invoice();
        invoice.setType(form.getType());
        invoice.setDate(form.getDate());
        invoice.setCustomer(customer);

        // Guardar invoice primero para obtener ID
        invoice = invoiceRepository.save(invoice);

        double total = 0.0;

        // Crear algunos detalles y actualizar stock
        for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
            Integer productId = entry.getKey();
            Integer quantity = entry.getValue();
            if (quantity != null && quantity > 0) {
                Product product = productRepository.findById(productId).get(); // ya validado
                double price = product.getPrice();
                double subtotal = price * quantity;
                total += subtotal;

                InvoiceDetail detail = new InvoiceDetail();
                detail.setInvoice(invoice);
                detail.setProduct(product);
                detail.setQuantity(quantity);
                detail.setPrice(price);
                detail.setSubtotal(subtotal);
                detailRepository.save(detail);

                // Actualizar el stock
                product.setStock(product.getStock() - quantity);
                productRepository.save(product);
            }
        }

        // No olvidar q no guardamos total en la tabla invoice (ya q no tiene campo), pero lo podemos mostrar en vista

        return "redirect:/comprobantes";
    }
}