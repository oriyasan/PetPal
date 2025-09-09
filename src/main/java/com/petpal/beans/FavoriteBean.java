package com.petpal.beans;

import com.petpal.model.Animal;
import com.petpal.model.Favorite;
import com.petpal.model.User;
import com.petpal.service.FavoriteService;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;


/**
 * JSF ManagedBean (SessionScoped) for managing a user's favorites.
 *
 * Responsibilities:
 * - Load user's favorites on init.
 * - Add or remove animals from favorites.
 * - Provide data to UI (list of favorites, list of animals).
 *
 * Delegates DB logic to {@link FavoriteService}.
 */
//=================================================== Favorite Bean =============================================================//

@ManagedBean(name = "favoriteBean")
@SessionScoped
public class FavoriteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final FavoriteService favoriteService = new FavoriteService();
    private List<Favorite> favorites;
    
    
    
    //***************************************** initialization *************************************//

    /** Default constructor */
    public FavoriteBean() {}

    /**
     * Called once after bean construction (per session).
     * Loads the favorites list for the current user (if logged in).
     */
    @PostConstruct
    public void init() {
        loadFavorites(); 
    }

    
    //***************************************** Core Actions *********************************************//

    /**
     * Loads/refreshes the favorites list for the current user.
     * Also converts animal image blobs to Base64 so they can be rendered in XHTML.
     * If no user is logged in, initializes with an empty list.
     */
    public void loadFavorites() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            this.favorites = favoriteService.getFavoritesByUser(currentUser.getId());

            for (Favorite fav : favorites) {
                Animal a = fav.getAnimal();
                if (a != null && a.getImageBlob() != null && a.getImageBlob().length > 0) {
                    a.setImageBase64(java.util.Base64.getEncoder().encodeToString(a.getImageBlob()));
                }
            }
        } else {
            this.favorites = java.util.Collections.emptyList();
        }
    }
    

    /**
     * Adds the given animal to the current user's favorites.
     * After adding, reloads the favorites list so the UI updates immediately.
     *
     * @param animalId the ID of the animal to add
     */
    public void addToFavorites(Long animalId) {
        User currentUser = getCurrentUser();
        if (currentUser != null && animalId != null) {
            favoriteService.addFavorite(currentUser.getId(), animalId);
            loadFavorites();
        }
    }

    
    
    /**
     * Removes the given animal from the current user's favorites.
     * After removal, reloads the favorites list so the UI updates immediately.
     *
     * @param animalId the ID of the animal to remove
     */
    public void removeFromFavorites(Long animalId) {
        User currentUser = getCurrentUser();
        if (currentUser != null && animalId != null) {
            favoriteService.removeFavorite(currentUser.getId(), animalId);
            loadFavorites();
        }
    }

    
    
    /**
     * Returns only the Animal entities marked as favorites by the current user.
     * Useful for UIs that need just animals (without Favorite metadata).
     *
     * @return list of Animal entities or an empty list if no user is logged in
     */
    public List<Animal> getUserFavoriteAnimals() {
        User currentUser = getCurrentUser();
        if (currentUser == null) return Collections.emptyList();
        return favoriteService.getFavoriteAnimalsForUser(currentUser.getId());
    }
    
    
    //***************************************** Getters & Helpers *********************************************//


    public List<Favorite> getFavorites() {
        return favorites == null ? Collections.emptyList() : favorites;
    }
    
    

    /**
     * Helper: retrieves the currently logged-in user from the HTTP session.
     * The user is stored under the "user" key by UserBean.login().
     *
     * @return User if logged in; otherwise null
     */
    private User getCurrentUser() {
        return (User) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("user"); 
    }
}
//===============================================================================================================================//
