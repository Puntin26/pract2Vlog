package servicios;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Map;

public final class JpaProvider {
    private static final String UNIT_NAME = "MiBlogPU";
    private static volatile EntityManagerFactory entityManagerFactory;

    private JpaProvider() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            synchronized (JpaProvider.class) {
                if (entityManagerFactory == null) {
                    Map<String, String> overrides = DatabaseConfig.getJpaOverrides();
                    entityManagerFactory = Persistence.createEntityManagerFactory(UNIT_NAME, overrides);
                }
            }
        }

        return entityManagerFactory;
    }

    public static void shutdown() {
        EntityManagerFactory localFactory = entityManagerFactory;
        if (localFactory != null && localFactory.isOpen()) {
            localFactory.close();
        }
    }
}
