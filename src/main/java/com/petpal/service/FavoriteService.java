package com.petpal.service;

import com.petpal.model.Animal;
import com.petpal.model.Favorite;
import com.petpal.model.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.util.ArrayList;
import java.util.List;



/**
 * Service layer for managing Favorites.
 * Handles adding, removing, and retrieving user's favorite animals.
 */

//============================================== Favorite Service ===============================================================//

public class FavoriteService {

	private static final EntityManagerFactory emf =
			Persistence.createEntityManagerFactory("PetPalPU");

	private EntityManager getEntityManager() {
		return emf.createEntityManager();
	}


	/**
	 * Adds a new favorite relation between a user and an animal.
	 * Ensures no duplicate favorites are created.
	 *
	 * @param userId   the user ID
	 * @param animalId the animal ID
	 */
	public void addFavorite(Long userId, Long animalId) {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();

			User user = em.find(User.class, userId);
			Animal animal = em.find(Animal.class, animalId);

			// Validate existence
			if (user == null || animal == null) {
				System.err.println("⚠ addFavorite: User or Animal not found");
				em.getTransaction().rollback();
				return;
			}

			// Check if already exists
			boolean exists = !em.createQuery(
					  "SELECT 1 FROM Favorite f WHERE f.user=:user AND f.animal=:animal")
					  .setParameter("user", user)
					  .setParameter("animal", animal)
					  .setMaxResults(1)
					  .getResultList().isEmpty();


			if (!exists) {
				em.persist(new Favorite(user, animal));
			}

			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			e.printStackTrace();
		} finally {
			em.close();
		}
	}

	/**
	 * Removes the favorite relation between a user and an animal.
	 * If multiple entries exist, removes all of them.
	 *
	 * @param userId   the user ID
	 * @param animalId the animal ID
	 */
	public void removeFavorite(Long userId, Long animalId) {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();

			User user = em.find(User.class, userId);
			Animal animal = em.find(Animal.class, animalId);

			// Validate existence
			if (user == null || animal == null) {
				System.err.println("⚠ removeFavorite: User or Animal not found");
				em.getTransaction().rollback();
				return;
			}

            // Find all favorites for this user-animal pair
			List<Favorite> favorites = em.createQuery(
					"SELECT f FROM Favorite f WHERE f.user = :user AND f.animal = :animal", Favorite.class)
					.setParameter("user", user)
					.setParameter("animal", animal)
					.getResultList();

			for (Favorite f : favorites) {
				em.remove(em.contains(f) ? f : em.merge(f));
			}

			em.getTransaction().commit();
		} catch (Exception e) {
			em.getTransaction().rollback();
			e.printStackTrace();
		} finally {
			em.close();
		}
	}

	/**
     * Retrieves all favorite entries (Favorite objects) for a given user.
     *
     * @param userId the user ID
     * @return list of Favorite entities
     */	public List<Favorite> getFavoritesByUser(Long userId) {
		EntityManager em = getEntityManager();
		try {
			User user = em.find(User.class, userId);
			if (user == null) {
				return new ArrayList<>();
			}
			return em.createQuery("SELECT f FROM Favorite f WHERE f.user = :user", Favorite.class)
					.setParameter("user", user)
					.getResultList();
		} finally {
			em.close();
		}
	}


     /**
      * Retrieves only the animals that the given user has marked as favorites.
      *
      * @param userId the user ID
      * @return list of Animal entities
      */
     public List<Animal> getFavoriteAnimalsForUser(Long userId) {
		EntityManager em = getEntityManager();
		try {
			return em.createQuery(
					"SELECT f.animal FROM Favorite f WHERE f.user.id = :userId", Animal.class)
					.setParameter("userId", userId)
					.getResultList();
		} finally {
			em.close();
		}
	}
}
//===============================================================================================================================//
