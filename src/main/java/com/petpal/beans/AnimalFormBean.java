package com.petpal.beans;

import com.petpal.model.Animal;
import com.petpal.model.Category;
import com.petpal.model.User;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.http.Part;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JSF ManagedBean (RequestScoped) responsible for handling
 * the animal creation form (add new animal).
 *
 * Loads categories for selection, receives form input,
 * processes uploaded image, assigns the current logged-in user
 * as the owner, and persists the animal entity into the DB.
 */
//================================================== Animal Form Bean ===========================================================//

@ManagedBean(name = "animalFormBean")
@RequestScoped
public class AnimalFormBean implements Serializable {
	
	
    private static final long serialVersionUID = 1L;
    private final com.petpal.service.AnimalService animalService = new com.petpal.service.AnimalService();

    @ManagedProperty("#{userBean}")
    private UserBean userBean;             
    
    
    
    //***************************************** Form Data *************************************//

   
    private Animal animal = new Animal();
    private List<Category> categories;
    private Long selectedCategoryId;
    private Part uploadedImage;
    
    
    

    //***************************************** initialization *************************************//

    /**
     * Load categories once per request to populate the dropdown.
     */
    @PostConstruct
    public void init() {
        categories = animalService.listCategories();
    }
    
    
    
    
    //***************************************** Actions *******************************************//


    
    /**
     * Handles the form submit: validates inputs, resolves the owner,
     * passes data to the service, and navigates back to the animals page.
     *
     * @return faces-redirect to animals on success, or null to stay on page if error
     */
    public String saveAnimal() {
        try {
            User owner = userBean != null ? userBean.getLoggedInUser() : null;
            InputStream imageStream = null;
            if (uploadedImage != null && uploadedImage.getSize() > 0) {
                imageStream = uploadedImage.getInputStream();
            }

            animalService.saveAnimal(animal, owner, selectedCategoryId, imageStream);
            return "animals?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //****************************************** Getters & Setters *************************************//
    

    public Animal getAnimal() {return animal;}

    public void setAnimal(Animal animal) {this.animal = animal; }

    public List<Category> getCategories() {return categories;}

    public Long getSelectedCategoryId() { return selectedCategoryId; }

    public void setSelectedCategoryId(Long selectedCategoryId) { this.selectedCategoryId = selectedCategoryId; }

    public Part getUploadedImage() {return uploadedImage; }

    public void setUploadedImage(Part uploadedImage) {this.uploadedImage = uploadedImage;}
    
    public void setUserBean(UserBean userBean) { this.userBean = userBean; }


}
//===============================================================================================================================//
