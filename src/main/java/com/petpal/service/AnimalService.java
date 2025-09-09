package com.petpal.service;

import com.petpal.model.Animal;
import com.petpal.model.Category;
import com.petpal.model.User;

import javax.persistence.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


/**
 * Service class for managing animals and categories.
 * Encapsulates all database operations (read/write) related to animals,
 * separating persistence logic from JSF beans.
 */
//============================================= Animal Service ==================================================================//



public class AnimalService {

	/**
	 * Single EntityManagerFactory for the entire application.
	 * Created from the persistence unit "PetPalPU" (see persistence.xml).
	 * Used to create EntityManager instances for DB operations.
	 */
	private static final EntityManagerFactory emf =
			Persistence.createEntityManagerFactory("PetPalPU");



	/*************************************** read operations ********************************************************************/


	/**
	 * Retrieve all categories sorted by name.
	 * @return list of categories
	 */
	public List<Category> listCategories() {
		
		EntityManager em = emf.createEntityManager();
		try {
			return em.createQuery("SELECT c FROM Category c ORDER BY c.name", Category.class)
					.getResultList();
		} finally { em.close(); }
	}



	/**
	 * Search animals by filters and sort order.
	 * Optionally converts image blobs to Base64 for UI display.
	 *
	 * @param categoryId filter by category (nullable)
	 * @param gender filter by gender (nullable/empty)
	 * @param minAge minimum age (nullable)
	 * @param maxAge maximum age (nullable)
	 * @param sortBy column to sort by (name/age/category/timestamp)
	 * @param sortDir sort direction (ASC/DESC)
	 * @param withBase64 whether to convert images to Base64 for display
	 * @return list of matching animals
	 */
	public List<Animal> search(Long categoryId, String gender,Integer minAge, Integer maxAge,String sortBy, String sortDir,boolean withBase64) {

		EntityManager em = emf.createEntityManager();

		try {
			StringBuilder jpql = new StringBuilder(
					"SELECT a FROM Animal a " +
							"JOIN FETCH a.category " +
							"JOIN FETCH a.owner " +
							"WHERE 1=1"
					);
			if (categoryId != null)               jpql.append(" AND a.category.id = :catId");
			if (gender != null && !gender.isEmpty()) jpql.append(" AND a.gender = :gender");
			if (minAge != null)                   jpql.append(" AND a.age >= :minAge");
			if (maxAge != null)                   jpql.append(" AND a.age <= :maxAge");

			String sortColumn;
			switch (sortBy != null ? sortBy : "") {
			case "name":     sortColumn = "a.name"; break;
			case "age":      sortColumn = "a.age"; break;
			case "category": sortColumn = "a.category.name"; break;
			default:         sortColumn = "a.timestamp";
			}
			String dir = "DESC".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
			jpql.append(" ORDER BY ").append(sortColumn).append(" ").append(dir);

			TypedQuery<Animal> q = em.createQuery(jpql.toString(), Animal.class);
			if (categoryId != null)                 q.setParameter("catId", categoryId);
			if (gender != null && !gender.isEmpty()) q.setParameter("gender", gender);
			if (minAge != null)                     q.setParameter("minAge", minAge);
			if (maxAge != null)                     q.setParameter("maxAge", maxAge);

			List<Animal> list = q.getResultList();
			if (!withBase64) return list;

			for (Animal a : list) {
				if (a.getImageBlob() != null && a.getImageBlob().length > 0) {
					a.setImageBase64(Base64.getEncoder().encodeToString(a.getImageBlob()));
				} else {
					a.setImageBase64(null);
				}
			}
			return list;
		} finally { em.close(); }
	}





	/**
	 * Find a category by its id.
	 * @param id category id
	 * @return Category or null if not found
	 */
	public Category findCategory(Long id) {
		if (id == null) return null;
		EntityManager em = emf.createEntityManager();
		try { return em.find(Category.class, id); }
		finally { em.close(); }
	}

	/*****************************************************************************************************************************/


	/*************************************** write operations *********************************************************************/



	/**
	 * Save a new animal to the DB.
	 * Sets owner, category, timestamp and optional image.
	 *
	 * @param animal the Animal entity (without owner/category yet)
	 * @param owner the currently logged in user
	 * @param categoryId the selected category id
	 * @param uploadedImageStream optional image input stream
	 * @throws Exception if validation fails or database error occurs
	 */
	public void saveAnimal(Animal animal, User owner, Long categoryId, InputStream uploadedImageStream) throws Exception {

		if (owner == null) throw new IllegalStateException("משתמש לא מחובר");
		if (categoryId == null) throw new IllegalStateException("לא נבחרה קטגוריה");

		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			Category cat = em.find(Category.class, categoryId);
			if (cat == null) throw new IllegalStateException("קטגוריה לא נמצאה");



			/*This section takes the image that the user uploaded (InputStream), 
			  reads it into memory, and converts it to a byte array (byte[]) that can be stored in a table
			 */
			byte[] blob = null;
			if (uploadedImageStream != null) {
				try (InputStream in = uploadedImageStream;
						ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
					byte[] data = new byte[2048];
					int n;
					while ((n = in.read(data)) != -1) buf.write(data, 0, n);
					blob = buf.toByteArray();
				}
			}

			tx.begin();
			animal.setOwner(owner);
			animal.setCategory(cat);
			animal.setTimestamp(LocalDateTime.now());
			if (blob != null && blob.length > 0) animal.setImageBlob(blob);
			em.persist(animal);
			tx.commit();
			
		} catch (Exception e) {
			
			if (tx.isActive()) tx.rollback();
			throw e;
			
		} finally { em.close(); }
	}

	
	
	
	
	/**
	 * Delete an animal only if it belongs to the given owner.
	 * Also deletes related Favorites and Messages before removing the animal.
	 *
	 * @param animalId the ID of the animal to delete
	 * @param ownerId the ID of the user attempting the deletion
	 * @return true if the animal was deleted successfully, false if not found or not owned by user
	 * @throws Exception if a database error occurs during deletion
	 */
	public boolean deleteIfOwner(Long animalId, Long ownerId) {
	    if (animalId == null || ownerId == null) return false;

	    EntityManager em = emf.createEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();

	        Animal a = em.find(Animal.class, animalId);
	        if (a == null) {
	            tx.commit();
	            return false;
	        }

	        if (a.getOwner() == null || !ownerId.equals(a.getOwner().getId())) {
	            tx.commit();
	            return false;
	        }

	        em.createQuery("DELETE FROM Favorite f WHERE f.animal.id = :aid")
	          .setParameter("aid", animalId)
	          .executeUpdate();

	        em.createQuery("DELETE FROM Message m WHERE m.animal.id = :aid")
	          .setParameter("aid", animalId)
	          .executeUpdate();

	        em.remove(em.contains(a) ? a : em.merge(a));

	        tx.commit();
	        return true;
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        throw e;
	    } finally {
	        em.close();
	    }
	}
	
	

	
	/**
	 * Retrieve all animals owned by the given user, ordered by timestamp (newest first).
	 * Optionally converts image blobs to Base64 for display in the UI.
	 *
	 * @param ownerId the ID of the user whose animals to retrieve
	 * @param withBase64 whether to convert image data to Base64 for rendering
	 * @return list of animals owned by the user (empty list if ownerId is null)
	 */
	public List<Animal> listByOwner(Long ownerId, boolean withBase64) {
	    if (ownerId == null) return java.util.Collections.emptyList();
	    EntityManager em = emf.createEntityManager();
	    try {
	        List<Animal> list = em.createQuery(
	            "SELECT a FROM Animal a " +
	            "JOIN FETCH a.category " +
	            "JOIN FETCH a.owner " +
	            "WHERE a.owner.id = :ownerId " +
	            "ORDER BY a.timestamp DESC", Animal.class)
	            .setParameter("ownerId", ownerId)
	            .getResultList();

	        if (!withBase64) return list;

	        for (Animal a : list) {
	            if (a.getImageBlob() != null && a.getImageBlob().length > 0) {
	                a.setImageBase64(java.util.Base64.getEncoder().encodeToString(a.getImageBlob()));
	            } else {
	                a.setImageBase64(null);
	            }
	        }
	        return list;
	    } finally {
	        em.close();
	    }
	}


	
	
	

	/*****************************************************************************************************************************/

}
//===============================================================================================================================//
