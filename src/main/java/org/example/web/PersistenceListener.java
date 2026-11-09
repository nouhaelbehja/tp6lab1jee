package org.example.web;
import org.example.util.HibernateUtil;
import javax.servlet.*;
public class PersistenceListener implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent event) { HibernateUtil.getSessionFactory(); }
    @Override public void contextDestroyed(ServletContextEvent event) { HibernateUtil.shutdown(); }
}

