package com.petpal.model;

import javax.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * Entity class representing a user's favorite animal.
 * Links a user to an animal and stores the date it was marked as favorite.
 * Enforces uniqueness (a user cannot favorite the same animal twice).
 */
//======================================= Favorites Table =======================================================================//




@Entity
@Table(name = "favorites",uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "animal_id"}))
public class Favorite implements Serializable{

	private static final long serialVersionUID = 1L;


	/******************************************** Entity Fields *********************************************************/    

	//Primary Key
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "animal_id", nullable = false)
	private Animal animal;

	private LocalDateTime timestamp;

	/*******************************************************************************************************************/    

	/*************************************** Constructors / Getters & Setters ******************************************/    


	public Favorite() {this.timestamp = LocalDateTime.now();}

	public Favorite(User user, Animal animal) {
		this.user = user;
		this.animal = animal;
		this.timestamp = LocalDateTime.now();
	}

	public Long getId() {return id;}

	public User getUser() {return user;}

	public Animal getAnimal() {return animal;}

	public LocalDateTime getTimestamp() {return timestamp;}
	
	
	
	/*******************************************************************************************************************/    

}


//================================================================================================================================//

