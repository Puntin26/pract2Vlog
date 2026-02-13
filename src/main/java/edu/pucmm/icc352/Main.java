package edu.pucmm.icc352;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class Main {

    public static void main(String[] args) {

        // Creando la instancia del servidor y configurando.
        Javalin app = Javalin.create(config -> {

            // configurando los documentos estáticos.
            config.staticFiles.add(staticFileConfig -> {
                staticFileConfig.hostedPath = "/";
                staticFileConfig.directory = "/publico";
                staticFileConfig.location = Location.CLASSPATH;
                staticFileConfig.precompress = false;
                staticFileConfig.aliasCheck = null;
            });

        }).start(7000);

        app.get("/", ctx -> {
            ctx.contentType("text/plain; charset=UTF-8");
            ctx.result("Blog funcionando 🚀");
        });

    }
}
