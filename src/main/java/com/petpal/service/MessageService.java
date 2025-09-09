package com.petpal.service;

import com.petpal.model.Animal;
import com.petpal.model.Message;
import com.petpal.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;



/**
 * Service layer for message operations.
 *
 * Responsibilities:
 *  - Load inbox/sent lists with the necessary relationships (sender/recipient/animal).
 *  - Find single Message/Animal by id.
 *  - Send a new message (persist).
 *  - Delete messages from inbox/sent with authorization checks.
 *
 * Notes:
 *  - Uses a single EntityManagerFactory created from persistence unit "PetPalPU".
 *  - Each method opens its own EntityManager and closes it in a finally block.
 */

//============================================== Message Service ===============================================================//

public class MessageService {

	private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("PetPalPU");


	
	
	
	//***************************************** Read Operations *************************************************//



	/**
	 * Load inbox messages for the given user.
	 * Eagerly fetches the message sender and (optionally) the related animal to avoid N+1 queries in the view.
	 *
	 * @param user recipient of the messages
	 * @return list of messages ordered by timestamp DESC
	 */
	public List<Message> loadInbox(User user) {
		EntityManager em = emf.createEntityManager();
		try {
			return em.createQuery(
					"SELECT m FROM Message m " +
							"JOIN FETCH m.sender " +
							"LEFT JOIN FETCH m.animal " +
							"WHERE m.recipient = :user " +
							"ORDER BY m.timestamp DESC",
							Message.class)
					.setParameter("user", user)
					.getResultList();
		} finally { em.close(); }
	}








	/**
	 * Load sent messages for the given user.
	 * Eagerly fetches the message recipient and (optionally) the related animal to avoid N+1 queries in the view.
	 *
	 * @param user sender of the messages
	 * @return list of messages ordered by timestamp DESC
	 */
	public List<Message> loadSent(User user) {
		EntityManager em = emf.createEntityManager();
		try {
			return em.createQuery(
					"SELECT m FROM Message m " +
							"JOIN FETCH m.recipient " +
							"LEFT JOIN FETCH m.animal " +
							"WHERE m.sender = :user " +
							"ORDER BY m.timestamp DESC",
							Message.class)
					.setParameter("user", user)
					.getResultList();
		} finally { em.close(); }
	}




	/**
	 * Find a single message by its id.
	 *
	 * @param id message id
	 * @return Message or null if not found
	 */
	public Message findMessage(Long id) {
		EntityManager em = emf.createEntityManager();
		try { return em.find(Message.class, id); }
		finally { em.close(); }
	}


	/**
	 * Find an animal by id (used when composing messages about an animal).
	 *
	 * @param id animal id
	 * @return Animal or null if not found
	 */
	public Animal findAnimal(Long id) {
		EntityManager em = emf.createEntityManager();
		try { return em.find(Animal.class, id); }
		finally { em.close(); }
	}




	//***************************************** Write Operations *********************************************//




	/**
	 * Send (persist) a new message.
	 * Assumes validation (non-null sender/recipient/subject/content) is done by the caller (bean).
	 *
	 * @param sender    the user who sends
	 * @param recipient the user who receives
	 * @param animal    optional related animal (nullable)
	 * @param subject   message subject (required)
	 * @param content   message content (required)
	 * @param when      timestamp to set; if null, uses now()
	 */
	public void sendMessage(User sender, User recipient, Animal animal,
			String subject, String content, LocalDateTime when) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			Message m = new Message();
			m.setSender(sender);
			m.setRecipient(recipient);
			m.setAnimal(animal);
			m.setSubject(subject);
			m.setContent(content);
			m.setTimestamp(when != null ? when : LocalDateTime.now());
			em.persist(m);
			tx.commit();
		} catch (Exception e) {
			if (tx.isActive()) tx.rollback();
			throw e;
		} finally { em.close(); }
	}






	/**
	 * Delete a message from the inbox of currentUser.
	 * Performs an authorization check: the current user must be the recipient.
	 *
	 * @param currentUser the user attempting the deletion
	 * @param messageId   id of the message to delete
	 * @return true if deletion occurred; false if not allowed or not found
	 */   
	public boolean deleteFromInbox(User currentUser, Long messageId) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			Message m = em.find(Message.class, messageId);
			if (m == null || m.getRecipient() == null ||
					!m.getRecipient().getId().equals(currentUser.getId())) {
				tx.commit();
				return false;
			}
			em.remove(m);
			tx.commit();
			return true;
		} catch (Exception e) {
			if (tx.isActive()) tx.rollback();
			throw e;
		} finally { em.close(); }
	}
	
	
	
	
	
	

	/**
	 * Delete a message from the sent box of currentUser.
	 * Performs an authorization check: the current user must be the sender.
	 *
	 * @param currentUser the user attempting the deletion
	 * @param messageId   id of the message to delete
	 * @return true if deletion occurred; false if not allowed or not found
	 */
	public boolean deleteFromSent(User currentUser, Long messageId) {
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			Message m = em.find(Message.class, messageId);
			if (m == null || m.getSender() == null ||
					!m.getSender().getId().equals(currentUser.getId())) {
				tx.commit();
				return false;
			}
			em.remove(m);
			tx.commit();
			return true;
		} catch (Exception e) {
			if (tx.isActive()) tx.rollback();
			throw e;
		} finally { em.close(); }
	}
}
//===============================================================================================================================//
