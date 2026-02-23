package edu.pucmm.icc352;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity // 1. Se convierte en tabla
public class Etiqueta {

    @Id // 2. Llave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 3. La base de datos generará el ID automáticamente (Auto-incremento)
    private long id;

    private String etiqueta;

    // Constructor vacío obligatorio para Hibernate
    public Etiqueta() {
    }

    public Etiqueta(long id, String etiqueta) {
        this.id = id;
        this.etiqueta = etiqueta;
    }

    // --- Getters y Setters ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
}