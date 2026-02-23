package servicios;

import org.h2.tools.Server;

import java.sql.SQLException;

public class DbBootstrap implements AutoCloseable {
    private Server h2Server;

    public void start() {
        try {
            h2Server = Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers").start();
            System.out.println("Servidor H2 TCP iniciado en el puerto 9092.");
        } catch (SQLException e) {
            System.err.println("No se pudo iniciar servidor H2 TCP. Se continuará sin modo servidor: " + e.getMessage());
        }

        JpaProvider.getEntityManagerFactory();
    }

    @Override
    public void close() {
        JpaProvider.shutdown();

        if (h2Server != null && h2Server.isRunning(false)) {
            h2Server.stop();
            System.out.println("Servidor H2 TCP detenido.");
        }
    }
}
