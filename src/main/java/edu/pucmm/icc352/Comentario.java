package edu.pucmm.icc352;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID automático
    private long id;

    private String comentario;

    @ManyToOne // Relación: Muchos comentarios pueden pertenecer a UN solo Usuario
    private Usuario autor;

    @ManyToOne // Relación: Muchos comentarios pueden pertenecer a UN solo Artículo
    private Articulo articulo;

    // Constructor vacío obligatorio
    public Comentario() {
    }

    public Comentario(long id, String comentario, Usuario autor, Articulo articulo) {
        this.id = id;
        this.comentario = comentario;
        this.autor = autor;
        this.articulo = articulo;
    }

    // --- Getters y Setters ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }

    public Articulo getArticulo() { return articulo; }
    public void setArticulo(Articulo articulo) { this.articulo = articulo; }
}