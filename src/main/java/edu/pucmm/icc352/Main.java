package edu.pucmm.icc352;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;

// IMPORTS DE TUS CLASES (Asegúrate que coincidan con tus paquetes)
import edu.pucmm.icc352.Articulo;
import edu.pucmm.icc352.Usuario;
import edu.pucmm.icc352.Comentario;
import edu.pucmm.icc352.Etiqueta;
import servicios.GestionDb; // Si GestionDb está en otro paquete, ajústalo aquí

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        // 1. INICIAR SERVIDOR
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.fileRenderer(new JavalinThymeleaf());
        }).start(7000);

        // 2. RUTA INICIO Muestra lista de artículos
        app.get("/", ctx -> {
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("listaArticulos", GestionDb.getInstancia().getListaArticulos());
            modelo.put("session", ctx.sessionAttributeMap()); // Pasamos la sesión al HTML
            ctx.render("templates/index.html", modelo);
        });

        // 3. RUTAS DE LOGIN
        app.get("/login", ctx -> ctx.render("templates/login.html"));

        app.post("/login", ctx -> {
            String usuario = ctx.formParam("username");
            String pass = ctx.formParam("password");
            Usuario u = GestionDb.getInstancia().autenticarUsuario(usuario, pass);

            if (u != null) {
                ctx.sessionAttribute("usuario", u);
                ctx.redirect("/");
            } else {
                Map<String, Object> modelo = new HashMap<>();
                modelo.put("error", true);
                ctx.render("templates/login.html", modelo);
            }
        });

        app.get("/logout", ctx -> {
            ctx.req().getSession().invalidate();
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
    }
}