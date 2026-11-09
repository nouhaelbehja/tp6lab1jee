package org.example.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/** Une seule factory, utilisée comme EntityManagerFactory par les DAO JPA. */
public final class HibernateUtil {
    private HibernateUtil() {}
    private static class Holder {
        private static final SessionFactory FACTORY = build();
    }
    private static SessionFactory build() {
        Configuration config = new Configuration().configure();
        config.setProperty("hibernate.connection.url", env("DB_URL",
                "jdbc:mysql://localhost:3306/gestion_commerciale?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8"));
        config.setProperty("hibernate.connection.username", env("DB_USER", "root"));
        config.setProperty("hibernate.connection.password", env("DB_PASSWORD", ""));
        return config.buildSessionFactory();
    }
    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null ? fallback : value;
    }
    public static SessionFactory getSessionFactory() { return Holder.FACTORY; }
    public static void shutdown() { if (!Holder.FACTORY.isClosed()) Holder.FACTORY.close(); }
}

