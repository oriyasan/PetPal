package com.petpal.beans;

import com.petpal.model.Animal;
import com.petpal.model.Category;
import com.petpal.model.User;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


/**
 * JSF ManagedBean (ViewScoped) responsible for managing
 * the list of animals displayed in the UI.
 * 
 * Provides filtering (by category, gender, age),
 * sorting, and initial loading of categories and animals.
 */

//==================================================== Animal Bean ==============================================================//



@ManagedBean(name = "animalBean")
@ViewScoped
public class AnimalBean implements Serializable {
	
	// serialVersionUID is required because this class implements Serializable (JSF ViewScoped/SessionScoped).
	// It identifies the version of the class for saving and restoring (Serialization/Deserialization).
    private static final long serialVersionUID = 1L;

    /** Service layer for DB operations related to animals and categories */
    private final com.petpal.service.AnimalService animalService = new com.petpal.service.AnimalService();
    
    
    

    //************************************************* View Data **********************************************//
   
    // Animals to be displayed in the UI (after search/filter/sort) 
    private List<Animal> animals = new ArrayList<>();
    private List<Animal> myAnimals = new ArrayList<>();

    
    // All categories (loaded once for filtering options) 
    private List<Category> categories = new ArrayList<>();
    
    
    @ManagedProperty("#{userBean}")
    private UserBean userBean;          
    
    //************************************************ Filters ************************************************//

    private Long categoryId;
    private String gender = "";
    private Integer minAge;
    private Integer maxAge;

    
    
    //************************************************ Sorting ***********************************************//
    private String sortBy = "timestamp";
    private String sortDir = "DESC";

    
    
    //************************************************ Initialization ****************************************//

    
    /**
     * Called once when the bean is constructed for the view.
     * Loads categories and performs an initial search.
     */
    @PostConstruct
    public void init() {
        categories = animalService.listCategories();
        search(); 
    }

   
    //************************************************ Actions ***********************************************//

    
    /**
     * Performs search with the current filters and sorting.
     * Populates the animals list for display.
     */
    public void search() {
        animals = animalService.search(
                categoryId, gender, minAge, maxAge, sortBy, sortDir, true);
    }
    
    

    /**
     * Deletes an animal by its ID, but only if the logged-in user
     * is the owner of that animal. Shows FacesMessages for:
     * - Not logged in
     * - Success
     * - No permission
     * - Error during deletion
     */
    public void deleteAnimal(Long animalId) {
        User me = (userBean != null) ? userBean.getLoggedInUser() : null;
        if (me == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "יש להתחבר למערכת", null));
            return;
        }
        try {
            boolean ok = animalService.deleteIfOwner(animalId, me.getId());
            if (ok) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "החיה נמחקה בהצלחה", null));
                search();  
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "אין הרשאה למחוק חיה זו", null));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "תקלה במחיקה", null));
        }
    }
    
    
    
    /**
     * Loads the list of animals owned by the currently logged-in user.
     * If no user is logged in, returns an empty list.
     * Otherwise, fetches all animals of that user (with Base64 images).
     */
    public void loadMyAnimals() {
    	
        User me = (User) FacesContext.getCurrentInstance()
            .getExternalContext().getSessionMap().get("user");

        if (me == null) {
            myAnimals = java.util.Collections.emptyList();
            return;
        }
        myAnimals = animalService.listByOwner(me.getId(), true);
    }

    
    //*********************************************** Getters & Setters *************************************//
    
    public List<Animal> getAnimals() { return animals; }
    public List<Category> getCategories() { return categories; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getMinAge() { return minAge; }
    public void setMinAge(Integer minAge) { this.minAge = minAge; }

    public Integer getMaxAge() { return maxAge; }
    public void setMaxAge(Integer maxAge) { this.maxAge = maxAge; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortDir() { return sortDir; }
    public void setSortDir(String sortDir) { this.sortDir = sortDir; }
    public void setUserBean(UserBean userBean){ this.userBean = userBean; }
    public List<Animal> getMyAnimals() { return myAnimals; }


}

//===============================================================================================================================//
