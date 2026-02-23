package edu.pucmm.icc352;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;


@Entity // 1. Esto le dice a Hibernate: "Crea una tabla en la base de datos para esta clase"
public class Usuario {

    @Id // 2. Esto indica que 'username' es la Llave Primaria (Primary Key)
    private String username;

    private String nombre;
    private String password;
    private boolean administrator;
    private boolean autor;

    @Column(columnDefinition = "TEXT")
    private String fotoBase64;

    // 4. un constructor vacío para poder funcionar por debajo
    public Usuario() {
    }


    public Usuario(String username, String nombre, String password, boolean administrator, boolean autor, String fotoBase64) {
        this.username = username;
        this.nombre = nombre;
        this.password = password;
        this.administrator = administrator;
        this.autor = autor;
        this.fotoBase64 = fotoBase64;
    }

    // Getters y Setters

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isAdministrator() { return administrator; }
    public void setAdministrator(boolean administrator) { this.administrator = administrator; }

    public boolean isAutor() { return autor; }
    public void setAutor(boolean autor) { this.autor = autor; }

    // 6. Agregamos el Getter y Setter para la foto
    public String getFotoBase64() { return fotoBase64; }
    public void setFotoBase64(String fotoBase64) { this.fotoBase64 = fotoBase64; }
}