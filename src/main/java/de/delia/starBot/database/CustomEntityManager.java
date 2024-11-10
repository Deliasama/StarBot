package de.delia.starBot.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.function.Function;

public class CustomEntityManager {
    private final EntityManagerFactory factory;

    public CustomEntityManager(EntityManagerFactory factory) {
        this.factory = factory;
    }

    public <T> T getEntityManager(Function<EntityManager, T> function) {
        EntityManager entityManager = factory.createEntityManager();
        try {
            return function.apply(entityManager);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
        return null;
    }
}
