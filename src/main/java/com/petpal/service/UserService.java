package com.petpal.service;

import javax.persistence.*;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Random;
import com.petpal.model.User;





/**
 * Service layer for managing User entities.
 *
 * Responsibilities:
 * - Look up users by username/email.
 * - Check existence of usernames/emails.
 * - Create new users and update their passwords.
 * - Issue temporary passwords (for password reset flow).
 * - Authenticate users (basic check — plain text for now).
 *
 * Notes:
 *  - In production, all password operations must use hashing (e.g. BCrypt).
 *  - This service uses JPA (EntityManager) with "PetPalPU" persistence unit.
 */
//=================================================== User Service ==============================================================//

public class UserService {

	private static final EntityManagerFactory emf =
			Persistence.createEntityManagerFactory("PetPalPU");

	// Work factor (cost)
	private static final int BCRYPT_COST = 12;


	// Password requirement: Minimum 7 characters, at least a lowercase letter, an uppercase letter, a number and a special character
	private static final String STRONG_PWD_REGEX =
	        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{7,}$";



	//***************************************** Lookup Operations *********************************************//



	/**
	 * Find a user by username.
	 *
	 * @param username the username to search for
	 * @return User entity or null if not found
	 */
	public User findByUsername(String username) {
		EntityManager em = emf.createEntityManager();
		try {
			return em.createQuery(
					"SELECT u FROM User u WHERE u.username = :u", User.class)
					.setParameter("u", username)
					.getResultStream().findFirst().orElse(null);
		} finally { em.close(); }
	}




	/**
	 * Find a user by email.
	 *
	 * @param email the email to search for
	 * @return User entity or null if not found
	 */
	public User findByEmail(String email) {
		EntityManager em = emf.createEntityManager();
		try {
			return em.createQuery(
					"SELECT u FROM User u WHERE u.email = :e", User.class)
					.setParameter("e", email)
					.getResultStream().findFirst().orElse(null);
		} finally { em.close(); }
	}


	/**
	 * Check if a username already exists.
	 *
	 * @param username the username to check
	 * @return true if username exists, false otherwise
	 */
	public boolean usernameExists(String username) {
		EntityManager em = emf.createEntityManager();
		try {
			Long c = em.createQuery(
					"SELECT COUNT(u) FROM User u WHERE u.username = :u", Long.class)
					.setParameter("u", username)
					.getSingleResult();
			return c != null && c > 0;
		} finally { em.close(); }
	}


	/**
	 * Check if an email already exists.
	 *
	 * @param email the email to check
	 * @return true if email exists, false otherwise
	 */
	public boolean emailExists(String email) {
		EntityManager em = emf.createEntityManager();
		try {
			Long c = em.createQuery(
					"SELECT COUNT(u) FROM User u WHERE u.email = :e", Long.class)
					.setParameter("e", email)
					.getSingleResult();
			return c != null && c > 0;
		} finally { em.close(); }
	}



	//***************************************** Write Operations *********************************************//
	/**
	 * Create a new user and persist it in the database.
	 * The raw password is hashed with BCrypt before being saved.
	 *
	 * @param u User entity to persist
	 */
	public void create(User u) {
	    if (!isStrongPassword(u.getPassword())) {
	        throw new IllegalArgumentException(
	            "Password must be at least 7 chars and include lowercase, uppercase, digit, and special.");
	    }
	    EntityManager em = emf.createEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        u.setPassword(hash(u.getPassword()));   
	        tx.begin();
	        em.persist(u);
	        tx.commit();
	    } catch (Exception ex) {
	        if (tx.isActive()) tx.rollback();
	        throw ex;
	    } finally { em.close(); }
	}

	
	
	/**
	 * Update an existing user's password.
	 * The new password is securely hashed with BCrypt before being stored.
	 *
	 * @param userId      ID of the user whose password should be updated
	 * @param newPassword the new plain-text password (hashed before save)
	 */
	public void updatePassword(Long userId, String newPassword) {
	    if (!isStrongPassword(newPassword)) {
	        throw new IllegalArgumentException(
	            "Password must be at least 7 chars and include lowercase, uppercase, digit, and special.");
	    }
	    EntityManager em = emf.createEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        User managed = em.find(User.class, userId);
	        if (managed != null) {
	            managed.setPassword(hash(newPassword)); 
	        }
	        tx.commit();
	    } catch (Exception ex) {
	        if (tx.isActive()) tx.rollback();
	        throw ex;
	    } finally { em.close(); }
	}






	//***************************************** Password Reset *********************************************//

	/**
	 * Generate and assign a temporary password for a user identified by email.
	 * - Creates a random code like "temp1234".
	 * - Stores it in the DB as a BCrypt hash (never plain text).
	 * - Returns the temporary raw code so it can be shown once to the user.
	 *
	 * @param email email address used to find the user
	 * @return the temporary password in plain text, or null if no user was found
	 **/
	  
	public String issueTempPassword(String email) {
	    EntityManager em = emf.createEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        User u = em.createQuery(
	                "SELECT u FROM User u WHERE u.email = :e", User.class)
	                .setParameter("e", email)
	                .getResultStream().findFirst().orElse(null);
	        if (u == null) return null;

	        String digits = String.valueOf(100000 + new java.util.Random().nextInt(900000));
	        String temp = "temp" + digits + "A!";   

	        tx.begin();
	        u = em.find(User.class, u.getId()); 
	        u.setPassword(hash(temp));         
	        tx.commit();

	        return temp; 
	    } catch (Exception ex) {
	        if (tx.isActive()) tx.rollback();
	        throw ex;
	    } finally { em.close(); }
	}




	//***************************************** Authentication *********************************************//

	/**
	 * Authenticate a user by verifying username and password.
	 * - Loads the user by username.
	 * - Compares the provided raw password against the stored BCrypt hash.
	 *
	 * @param username    the username
	 * @param rawPassword the plain-text password entered by user
	 * @return the User entity if authentication succeeds, null otherwise
	 */
	public User authenticate(String username, String rawPassword) {
		User u = findByUsername(username);
		if (u != null && matches(rawPassword, u.getPassword())) {
			return u;
		}
		return null;
	}





	//***************************************** Password Hashing Utilities *********************************************//


	/**
	 * Hash a raw (plain-text) password using BCrypt.
	 * Adds a unique salt and applies the configured work factor (cost).
	 *
	 * @param raw the plain-text password
	 * @return a secure hashed representation of the password
	 */
	private String hash(String raw) {
		return BCrypt.hashpw(raw, BCrypt.gensalt(BCRYPT_COST));
	}

	/**
	 * Verify that a raw (plain-text) password matches a stored BCrypt hash.
	 *
	 * @param raw    the plain-text password entered by user
	 * @param hashed the stored BCrypt hash from DB
	 * @return true if password matches, false otherwise
	 */
	private boolean matches(String raw, String hashed) {
		return hashed != null && raw != null && BCrypt.checkpw(raw, hashed);
	}


	private static boolean isStrongPassword(String raw) {
	    return raw != null && raw.matches(STRONG_PWD_REGEX);
	}



}
//===============================================================================================================================//
