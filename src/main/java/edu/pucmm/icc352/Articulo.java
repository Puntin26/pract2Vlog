package edu.pucmm.icc352;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Articulo {
    private long id;
    private String titulo;
    private String cuerpo;
    private Usuario autor;
    private Date fecha;
    private List<Comentario> listaComentarios;
    private List<Etiqueta> listaEtiquetas;


    public Articulo(long id, String titulo, String cuerpo, Usuario autor, Date fecha) {
        this.id = id;
        this.titulo = titulo;
        this.cuerpo = cuerpo;
        this.autor = autor;
        this.fecha = fecha;
        // Inicializamos las listas vacías
        this.listaComentarios = new ArrayList<>();
        this.listaEtiquetas = new ArrayList<>();
    }

    //para recortar el texto a 70 caracteres
    public String getResumen() {
        if (cuerpo != null && cuerpo.length() > 70) {
            return cuerpo.substring(0, 70) + "...";
        }
        return cuerpo;
    }

    // Getters y Setters
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
}