package com.petpal.model;

import javax.persistence.*;


import java.io.Serializable;
import java.time.LocalDateTime;



/**
 * Entity class representing messages exchanged between users.
 * Stores sender, recipient, optional related animal, subject, content,
 * timestamp and read/unread status.
 */
//======================================= Messages Table =======================================================================//





@Entity
@Table(name = "messages")
public class Message implements Serializable {

	private static final long serialVersionUID = 1L;





	/******************************************** Entity Fields *********************************************************/    

	//Primary Key
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;

	@ManyToOne
	@JoinColumn(name = "recipient_id", nullable = false)
	private User recipient;

	@ManyToOne
	@JoinColumn(name = "animal_id", nullable = false)
	private Animal animal;

	private String subject;

	@Lob
	private String content;

	private LocalDateTime timestamp;

	private boolean isRead;



	/*******************************************************************************************************************/    



	/*************************************** Constructors / Getters & Setters ******************************************/    

	public Message() {
		this.timestamp = LocalDateTime.now();
		this.isRead = false;
	}

	public Message(User sender, User recipient, Animal animal, String subject, String content) {
		this.sender = sender;
		this.recipient = recipient;
		this.animal = animal;
		this.subject = subject;
		this.content = content;
		this.timestamp = LocalDateTime.now();
		this.isRead = false;
	}

	public Long getId() { return id; }

	public User getSender() { return sender; }

	public void setSender(User sender) { this.sender = sender; }

	public User getRecipient() { return recipient; }

	public void setRecipient(User recipient) { this.recipient = recipient; }

	public Animal getAnimal() { return animal; }

	public void setAnimal(Animal animal) { this.animal = animal; }

	public String getSubject() { return subject; }

	public void setSubject(String subject) { this.subject = subject; }

	public String getContent() { return content; }

	public void setContent(String content) { this.content = content; }

	public LocalDateTime getTimestamp() { return timestamp; }

	public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

	public boolean isRead() { return isRead; }

	public void setRead(boolean read) { isRead = read; }

	
	
	
	/**
	 * Returns a human-readable timestamp for UI display.
	 * Not persisted in the database (marked as JPA @Transient).
	 *
	 * Behavior:
	 * - If timestamp is null, returns an empty string.
	 * - Otherwise formats using pattern "dd/MM/yyyy HH:mm" (24-hour clock).
	 */
	@Transient
	public String getTimestampFormatted() {
		return (timestamp == null)
				? ""
						: timestamp.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
	}

	/*******************************************************************************************************************/    




}
//===============================================================================================================================//

