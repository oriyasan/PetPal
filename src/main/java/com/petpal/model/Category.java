package com.petpal.model;

import javax.persistence.*;
import java.io.Serializable;



/**
 * Entity class representing categories of animals (e.g. dogs, cats).
 * Used for classifying animals in the adoption system.
 */
//======================================= Categories Table =======================================================================//



@Entity
@Table(name = "categories",uniqueConstraints = @UniqueConstraint(name = "uk_category_name", columnNames = "name"))
public class Category implements Serializable {

	private static final long serialVersionUID = 1L;



	/******************************************** Entity Fields *********************************************************/    
	//Primary Key
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String name;

	/*******************************************************************************************************************/    


	/*************************************** Constructors / Getters & Setters *******************************************/    

	public Category() {}
	public Category(String name) { this.name = name; }

	public Long getId() { return id; }

	public String getName() { return name; }

	public void setName(String name) { this.name = name; }


	//Determines when categories are considered identical
	@Override public boolean equals(Object o) {

		if (this == o) return true;
		if (!(o instanceof Category)) return false;
		Category other = (Category) o;
		return id != null && id.equals(other.id);
	}

	@Override public int hashCode() { return 31; }
	@Override public String toString() { return name; }
}


//================================================================================================================================//

