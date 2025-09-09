package com.petpal.model;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


/**
 * Entity class representing application users.
 * Each user has a username, password and email, and may own animals
 * or send/receive messages in the system.
 */
//======================================= Users Table ============================================================================//



@Entity
@Table(name = "users")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	/******************************************** Entity Fields *********************************************************/

	//Primary Key
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank                    
    @Column(nullable = false, length = 100)
	private String username;
	
	@NotBlank                    
    @Column(nullable = false, length = 60)  
	private String password;
	
	@NotBlank                     
    @Email                        
    @Column(nullable = false, length = 255)
	private String email;

	/********************************************************************************************************************/

	/*************************************** Constructors / Getters & Setters *******************************************/    

	public User() {}

	public User( String username, String password, String email) {
		this.username = username;
		this.password = password;
		this.email = email;
	}

	public Long getId() { return id;}

	public String getUsername() {return username;}

	public void setUsername(String username) {this.username = username;}

	public String getPassword() {return password;}

	public void setPassword(String password) { this.password = password;}

	public String getEmail() {return email;}

	public void setEmail(String email) {this.email = email;}

	/*******************************************************************************************************************/

}
//===============================================================================================================================//
