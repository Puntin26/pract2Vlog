package edu.pucmm.icc352;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import java.util.List;
import org.jasypt.util.text.AES256TextEncryptor;

import java.util.Base64;
import io.javalin.http.UploadedFile;

// 1. NUEVOS IMPORTS PARA LA BASE DE DATOS Y HIBERNATE
import org.h2.tools.Server;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.sql.SQLException;

// IMPORTS DE TUS CLASES
import edu.pucmm.icc352.Articulo;
import edu.pucmm.icc352.Usuario;
import edu.pucmm.icc352.Comentario;
import edu.pucmm.icc352.Etiqueta;
import servicios.GestionDb;

import java.util.HashMap;
import java.util.Map;

public class Main {
    // 2. AGREGAMOS 'throws SQLException' PORQUE EL SERVIDOR H2 PUEDE DAR ERROR AL INICIAR
    public static void main(String[] args) throws SQLException {

        System.out.println("=============================================");
        System.out.println("1. Iniciando servidor de Base de Datos H2...");
        System.out.println("=============================================");
        // 3. INICIAMOS H2 EN MODO SERVIDOR (Puerto 9092) - Requisito #1
       // Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers").start();

        System.out.println("=============================================");
        System.out.println("2. Iniciando Hibernate (ORM) y creando tablas...");
        System.out.println("=============================================");
        // 4. CONECTAMOS HIBERNATE USANDO EL NOMBRE QUE PUSIMOS EN persistence.xml
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("MiBlogPU");

        System.out.println("=============================================");
        System.out.println("3. Tablas creadas con éxito. Iniciando Javalin...");
        System.out.println("=============================================");


        // --- A PARTIR DE AQUÍ ESTÁ TU CÓDIGO ORIGINAL INTACTO ---

        // 1. INICIAR SERVIDOR
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.fileRenderer(new JavalinThymeleaf());
        }).start(7000);

        // --- CONFIGURACIÓN DE JASYPT (Requisito #5) ---
        AES256TextEncryptor encriptador = new AES256TextEncryptor();
        encriptador.setPassword("mi_clave_secreta_super_segura_352"); // Contraseña maestra para encriptar/desencriptar

        // --- FILTRO: REVISAR COOKIE EN CADA PETICIÓN (Requisito #5) ---
        app.before(ctx -> {
            Usuario usuarioSession = ctx.sessionAttribute("usuario");
            String cookieRecordarme = ctx.cookie("usuario_cookie");

            // Si NO hay sesión iniciada, pero SÍ hay una cookie guardada en el navegador
            if (usuarioSession == null && cookieRecordarme != null) {
                try {
                    // Desencriptamos la cookie para sacar el nombre de usuario
                    String usernameDescifrado = encriptador.decrypt(cookieRecordarme);
                    Usuario u = GestionDb.getInstancia().getUsuarioPorUsername(usernameDescifrado);

                    if (u != null) {
                        ctx.sessionAttribute("usuario", u); // Le iniciamos la sesión mágicamente
                    }
                } catch (Exception e) {
                    // Si alguien intentó hackear o modificar la cookie, dará error al desencriptar. La borramos.
                    ctx.removeCookie("usuario_cookie");
                }
            }
        });

        // 2. RUTA INICIO Muestra lista de artículos
        // 2. RUTA INICIO (Con Paginación - Requisito #3)
        app.get("/", ctx -> {
            // Obtener el número de página de la URL (Ej: /?pagina=2). Si no hay nada, asumimos que es la página 1.
            int paginaActual = ctx.queryParamAsClass("pagina", Integer.class).getOrDefault(1);
            int articulosPorPagina = 5;

            // Buscar SOLO los 5 artículos de esta página

            List<Articulo> articulos = GestionDb.getInstancia().getArticulosPaginados(paginaActual, articulosPorPagina);

            // Calcular cuántas páginas hay en total
            long totalArticulos = GestionDb.getInstancia().getTotalArticulos();
            // Math.ceil redondea hacia arriba (Ej: si hay 11 artículos, 11/5 = 2.2 -> Sube a 3 páginas)
            int totalPaginas = (int) Math.ceil((double) totalArticulos / articulosPorPagina);

            Map<String, Object> modelo = new HashMap<>();
            modelo.put("listaArticulos", articulos);
            modelo.put("paginaActual", paginaActual);
            modelo.put("totalPaginas", totalPaginas); // Mandamos este dato al HTML para dibujar los botones
            modelo.put("session", ctx.sessionAttributeMap());

            ctx.render("templates/index.html", modelo);
        });

        // 3. RUTAS DE LOGIN
        app.get("/login", ctx -> ctx.render("templates/login.html"));

        // --- RUTAS DE LOGIN ACTUALIZADAS ---
        app.post("/login", ctx -> {
            String usuario = ctx.formParam("username");
            String pass = ctx.formParam("password");
            boolean recordar = ctx.formParam("recordarme") != null; // Verificamos si marcó la casilla

            Usuario u = GestionDb.getInstancia().autenticarUsuario(usuario, pass);

            if (u != null) {
                ctx.sessionAttribute("usuario", u);

                // Si marcó la casilla, creamos la cookie encriptada
                if (recordar) {
                    String textoEncriptado = encriptador.encrypt(u.getUsername());
                    // Guardamos la cookie por 7 días (604800 segundos)
                    ctx.cookie("usuario_cookie", textoEncriptado, 604800);
                }

                ctx.redirect("/");
            } else {
                Map<String, Object> modelo = new HashMap<>();
                modelo.put("error", true);
                ctx.render("templates/login.html", modelo);
            }
        });

        app.get("/logout", ctx -> {
            ctx.req().getSession().invalidate(); // Borramos la sesión
            ctx.removeCookie("usuario_cookie"); // MUY IMPORTANTE: Borramos la cookie al salir
            ctx.redirect("/");
        });

        // 4. CREAR ARTÍCULO
        app.get("/crear-articulo", ctx -> {
            if (ctx.sessionAttribute("usuario") == null) {
                ctx.redirect("/login");
                return;
            }
            ctx.render("templates/crear-articulo.html");
        });

        app.post("/procesar-articulo", ctx -> {
            Usuario u = ctx.sessionAttribute("usuario");
            if (u == null) { ctx.redirect("/login"); return; }

            String titulo = ctx.formParam("titulo");
            String cuerpo = ctx.formParam("cuerpo");
            String tags = ctx.formParam("etiquetas");

            Articulo art = new Articulo(0, titulo, cuerpo, u, new java.util.Date());

            if (tags != null && !tags.isEmpty()) {
                for (String tag : tags.split(",")) {
                    art.getListaEtiquetas().add(new Etiqueta(0, tag.trim()));
                }
            }
            GestionDb.getInstancia().crearArticulo(art);
            ctx.redirect("/");
        });

        // 5. VER DETALLE DE ARTÍCULO
        app.get("/articulo/{id}", ctx -> {
            // Convertimos el ID de la URL a numero
            long id = Long.parseLong(ctx.pathParam("id"));

            Articulo art = GestionDb.getInstancia().getArticuloPorId(id);

            if (art != null) {
                Map<String, Object> modelo = new HashMap<>();
                modelo.put("articulo", art);
                modelo.put("session", ctx.sessionAttributeMap());
                ctx.render("templates/detalle-articulo.html", modelo);
            } else {
                ctx.status(404).result("Error 404: Artículo no encontrado");
            }
        });

        // 6. COMENTAR EN UN ARTÍCULO
        app.post("/articulo/{id}/comentario", ctx -> {
            Usuario u = ctx.sessionAttribute("usuario");
            if (u == null) { ctx.redirect("/login"); return; }

            long id = Long.parseLong(ctx.pathParam("id"));
            String comentario = ctx.formParam("comentario");

            Articulo art = GestionDb.getInstancia().getArticuloPorId(id);
            if (art != null) {
                Comentario c = new Comentario(0, comentario, u, art);
                art.getListaComentarios().add(c);
            }
            ctx.redirect("/articulo/" + id);
        });

        // 7. BORRAR ARTÍCULO
        app.get("/borrar-articulo/{id}", ctx -> {
            Usuario u = ctx.sessionAttribute("usuario");
            if (u != null && (u.isAdministrator() || u.isAutor())) {
                long id = Long.parseLong(ctx.pathParam("id"));
                GestionDb.getInstancia().borrarArticulo(id);
            }
            ctx.redirect("/");
        });

        // 8. BUSCAR POR ETIQUETA (Requisito #4)
        app.get("/etiquetas/{etiqueta}", ctx -> {
            String etiqueta = ctx.pathParam("etiqueta"); // Sacamos la etiqueta de la URL

            // Buscamos los artículos usando el método nuevo
            List<Articulo> articulosFiltrados = GestionDb.getInstancia().getArticulosPorEtiqueta(etiqueta);

            Map<String, Object> modelo = new HashMap<>();
            modelo.put("listaArticulos", articulosFiltrados);
            modelo.put("etiquetaFiltro", etiqueta); // Para decirle al HTML que estamos filtrando
            modelo.put("totalPaginas", 1); // Apagamos la paginación temporalmente
            modelo.put("paginaActual", 1);
            modelo.put("session", ctx.sessionAttributeMap());

            ctx.render("templates/index.html", modelo);
        });

        // --- REQUISITO #6: PERFIL Y FOTO EN BASE64 ---

        // 1. Mostrar la página del perfil
        app.get("/perfil", ctx -> {
            Usuario u = ctx.sessionAttribute("usuario");
            if (u == null) {
                ctx.redirect("/login");
                return;
            }
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("session", ctx.sessionAttributeMap());
            ctx.render("templates/perfil.html", modelo);
        });

        // 2. Procesar la foto subida
        app.post("/perfil/foto", ctx -> {
            Usuario u = ctx.sessionAttribute("usuario");
            if (u == null) {
                ctx.redirect("/login");
                return;
            }

            // Atrapamos el archivo que el usuario subió en el formulario
            UploadedFile fotoObtenida = ctx.uploadedFile("foto");

            if (fotoObtenida != null && fotoObtenida.size() > 0) {
                try {
                    // Magia: Convertimos el archivo a una lista de bytes
                    byte[] bytesImagen = fotoObtenida.content().readAllBytes();

                    // Magia 2: Convertimos los bytes a texto Base64
                    String base64String = Base64.getEncoder().encodeToString(bytesImagen);

                    // Le agregamos la "cabecera" para que el HTML sepa que es una imagen
                    String fotoFinal = "data:" + fotoObtenida.contentType() + ";base64," + base64String;

                    // Se la asignamos al usuario y actualizamos la Base de Datos
                    u.setFotoBase64(fotoFinal);
                    GestionDb.getInstancia().actualizarUsuario(u);

                    // Actualizamos la sesión para que el cambio se vea de inmediato
                    ctx.sessionAttribute("usuario", u);

                } catch (Exception e) {
                    System.out.println("Error al procesar la imagen: " + e.getMessage());
                }
            }
            ctx.redirect("/perfil");
        });
    }
}