package edu.pucmm.icc352;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity // 1. Se convierte en la tabla principal
public class Articulo {

    @Id // 2. Llave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID automático
    private long id;

    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String cuerpo;

    @ManyToOne // 4. Muchos artículos pertenecen a UN autor
    private Usuario autor;

    private Date fecha;

    // 5. Relación: Un artículo tiene MUCHOS comentarios.
    // cascade = CascadeType.ALL significa que si borras el artículo, se borran sus comentarios (¡super útil!)
    // fetch = FetchType.EAGER carga los comentarios automáticamente cuando buscas el artículo.
    @OneToMany(mappedBy = "articulo", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Comentario> listaComentarios = new ArrayList<>();


    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Etiqueta> listaEtiquetas = new ArrayList<>();

    // --- Constructores ---

    // Constructor vacío (Obligatorio para JPA/Hibernate)
    public Articulo() {
    }

    // Constructor normal (Para compatibilidad con tu código anterior)
    public Articulo(long id, String titulo, String cuerpo, Usuario autor, Date fecha) {
        this.id = id;
        this.titulo = titulo;
        this.cuerpo = cuerpo;
        this.autor = autor;
        this.fecha = fecha;
    }

    // --- Getters y Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getCuerpo() { return cuerpo; }
    public void setCuerpo(String cuerpo) { this.cuerpo = cuerpo; }

    public Usuario getAutor() { return autor; }
    public void setAutor(Usuario autor) { this.autor = autor; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public List<Comentario> getListaComentarios() { return listaComentarios; }
    public void setListaComentarios(List<Comentario> listaComentarios) { this.listaComentarios = listaComentarios; }

    public List<Etiqueta> getListaEtiquetas() { return listaEtiquetas; }
    public void setListaEtiquetas(List<Etiqueta> listaEtiquetas) { this.listaEtiquetas = listaEtiquetas; }

    // Método extra para cumplir con el Requisito #3 (Resumen de 70 caracteres en el index)
    public String getResumen() {
        if (this.cuerpo == null) return "";
        if (this.cuerpo.length() > 70) {
            return this.cuerpo.substring(0, 70) + "...";
        }
        return this.cuerpo;
    }
}