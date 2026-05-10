package com.example.tarea.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class InvoiceForm {

    @NotNull(message = "El tipo de comprobante es obligatorio")
    private String type; // para la "FACTURA" o "BOLETA" gaa

    @NotNull(message = "Oye! La fecha es obligatoria")
    @PastOrPresent(message = "Viajaste al futuro :o! La fecha no puede ser futura")
    private LocalDate date;

    @NotNull(message = "Hey! Debes seleccionar un cliente no crees?")
    private Integer customerId;

    // Mapa: productId -> cantidad
    private Map<Integer, Integer> quantities = new HashMap<>();

    // Getters y setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public Map<Integer, Integer> getQuantities() { return quantities; }
    public void setQuantities(Map<Integer, Integer> quantities) { this.quantities = quantities; }
}