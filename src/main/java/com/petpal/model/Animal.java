package com.petpal.model;

import java.time.LocalDateTime;

import javax.persistence.*;
import java.io.Serializable;


/**
 * Entity class representing an animal available for adoption.
 * Includes category, name, owner, descriptive fields, image, timestamp and age/gender.
 */
//======================================= Animals Table =========================================================================//






@Entity
@Table(name = "animals")
public class Animal implements Serializable {

	private static final long serialVersionUID = 1L;

	/******************************************** Entity Fields *********************************************************/    

	//Primary Key
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) //This value is automatically created by the DB
	private Long id;  

	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;
	
	@Column(nullable = false, length = 100)  
	private String name;
	
	
	private int age;

	@Column(length = 20)    
	private String gender;
	
	@Column(length = 255) 
	private String shortDescription;
	
	@Lob
	private String fullDescription;

	//animal image
	@Lob
	@Column(name = "image_blob")
	private byte[] imageBlob;

	//Date added/updated 
	@Column(name = "timestamp")
	private LocalDateTime timestamp;

	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;


	/*******************************************************************************************************************/    




	//An auxiliary field not saved in the database that is intended to hold the image after it has been converted from bytes (byte[] imageBlob) to Base64.
	@Transient
	private String imageBase64;



	/*************************************** Constructors / Getters & Setters *******************************************/    

	public Animal() {}

	public Animal(Category category, String name, int age, 
			String gender, String shortDescription, String fullDescription,
			byte[] imageBlob, User owner) {
		this.category = category;
		this.name = name;
		this.age = age;
		this.gender = gender;
		this.shortDescription = shortDescription;
		this.fullDescription = fullDescription;
		this.imageBlob = imageBlob;
		this.owner = owner;
	}




	public Long getId() {return id;}

	public Category getCategory() {return category;}

	public void setCategory(Category category) {this.category = category;}

	public String getName() {return name;}

	public void setName(String name) {this.name = name;}

	public int getAge() {return age;}

	public void setAge(int age) {this.age = age;}

	public String getGender() {return gender;}

	public void setGender(String gender) {this.gender = gender;}

	public String getShortDescription() {return shortDescription;}

	public void setShortDescription(String shortDescription) {this.shortDescription = shortDescription;}

	public String getFullDescription() {return fullDescription;}

	public void setFullDescription(String fullDescription) {this.fullDescription = fullDescription;}

	public byte[] getImageBlob() {return imageBlob;}

	public void setImageBlob(byte[] imageBlob) {this.imageBlob = imageBlob;}

	public User getOwner() {return owner;}

	public void setOwner(User owner) {this.owner = owner;}

	public LocalDateTime getTimestamp() {return timestamp;}

	public void setTimestamp(LocalDateTime timestamp) {this.timestamp = timestamp; }

	public String getImageBase64() {return imageBase64;}

	public void setImageBase64(String imageBase64) {this.imageBase64 = imageBase64;}



	/********************************************************************************************************************/    

}

//===============================================================================================================================//
