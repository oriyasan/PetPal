package com.petpal.beans;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ApplicationScoped;
import javax.persistence.*;
import com.petpal.model.Category;


/**
 * Application-scoped bean that seeds default data into the database.
 * Runs once at application startup and ensures the "categories" table
 * is populated with initial values if empty.
 */

//================================================== Data Initializer ======================================================================//

@ManagedBean(name = "dataInitializer")
@ApplicationScoped
public class DataInitializer implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final EntityManagerFactory emf =
        Persistence.createEntityManagerFactory("PetPalPU");
    
    
    
    

    //***************************************** Initialization Logic *********************************************//

    private volatile boolean seeded = false;

    
    
    /**
     * Insert default categories if the table is empty.
     * Called automatically at startup from layout.xhtml:
     * <f:event type="preRenderView" listener="#{dataInitializer.seed}" />
     */
    public synchronized void seed() {
        if (seeded) {
            return; 
        }

        EntityManager em = emf.createEntityManager();
        try {
            Long count = em.createQuery("SELECT COUNT(c) FROM Category c", Long.class)
                           .getSingleResult();
            if (count == 0L) {
                em.getTransaction().begin();
                em.persist(new Category("כלבים"));
                em.persist(new Category("חתולים"));
                em.persist(new Category("ציפורים"));
                em.persist(new Category("מכרסמים"));
                em.persist(new Category("זוחלים"));
                em.getTransaction().commit();
                System.out.println("[Seed] Default categories were inserted.");
            } else {
                System.out.println("[Seed] Categories already exist (" + count + "). Skipping.");
            }
            seeded = true; 
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
//===============================================================================================================================//
