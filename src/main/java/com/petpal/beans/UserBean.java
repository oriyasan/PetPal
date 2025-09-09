package com.petpal.beans;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.petpal.model.User;

import java.io.Serializable;

/**
 * JSF ManagedBean (SessionScoped) for handling user authentication and profile.
 *
 * Responsibilities:
 *  - User login and logout.
 *  - Registration of new users.
 *  - Password reset (temporary password) and password change.
 *  - Store and expose the currently logged-in user.
 *
 * Delegates database logic to {@link com.petpal.service.UserService}.
 */
//=================================================== User Bean =================================================================//

@ManagedBean(name = "userBean")
@SessionScoped
public class UserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private final com.petpal.service.UserService userService =
			new com.petpal.service.UserService();

	/** Current user state */
	private Long id;
	private boolean loggedIn = false;
	private User loggedInUser;

	/** Fields for forms */
	private String resetEmail;
	private String confirmPassword;

	@NotBlank(message = "שדה חובה")
	private String username;

	@NotBlank(message = "שדה חובה")
	private String password;

	@NotBlank(message = "יש להזין אימייל")
	@Email(message = "אנא הזינ/י כתובת אימייל תקינה (לדוגמה: name@example.com)")
	private String email;

	//***************************************** Authentication *********************************************//

	public boolean isLoggedIn() { return loggedIn; }

	/**
	 * Authenticate the user with username/password.
	 * On success → invalidate old session, create new session, mark as logged in and store user.
	 */
	public String login() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		try {
			final String userTrim = username != null ? username.trim() : null;
			User u = userService.authenticate(userTrim, password);
			this.password = null; // לניקוי

			if (u != null) {
				this.loggedIn = true;
				this.loggedInUser = u;
				this.username = u.getUsername();
				this.email = u.getEmail();
				ctx.getExternalContext().getSessionMap().put("user", u);
				return "index?faces-redirect=true";
			}
			else {
				return "login_failed?faces-redirect=true";
			}
		} catch (Exception e) {
			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"שגיאה בהתחברות", null));
			return null;
		}
	}


	/**
	 * Register a new user in the system.
	 * Validates duplicates by username/email, then persists (service hashes the password).
	 */
	public String register() {
	    try {
	        if (userService.usernameExists(username) || userService.emailExists(email)) {
	            FacesContext.getCurrentInstance().addMessage(null,
	                new FacesMessage(FacesMessage.SEVERITY_ERROR,
	                    "שם המשתמש או כתובת האימייל כבר קיימים", null));
	            return null;
	        }
	        User newUser = new User();
	        newUser.setUsername(username);
	        newUser.setPassword(password); 
	        newUser.setEmail(email);

	        userService.create(newUser);

	        this.username = null; this.password = null; this.email = null;
	        return "welcome.xhtml";
	    } catch (IllegalArgumentException iae) {
	        FacesContext.getCurrentInstance().addMessage("password",
	            new FacesMessage(FacesMessage.SEVERITY_ERROR, iae.getMessage(), null));
	        return null;
	    } catch (Exception e) {
	        FacesContext.getCurrentInstance().addMessage(null,
	            new FacesMessage(FacesMessage.SEVERITY_ERROR, "אירעה שגיאה במהלך הרישום", null));
	        return null;
	    }
	}

	/** Log out the current user: invalidate session and redirect to index. */
	public String logout() {
		FacesContext fc = FacesContext.getCurrentInstance();
		try {
			fc.getExternalContext().invalidateSession();
		} catch (Exception ignored) {}
		this.loggedIn = false;
		this.loggedInUser = null;
		this.username = null;
		this.email = null;
		this.password = null;
		this.confirmPassword = null;

		return "index?faces-redirect=true";
	}

	
	
	/**
	 * Issues a temporary password for the given email.
	 * If email exists → service stores a BCrypt hash and returns the raw temp code (once).
	 * Otherwise → shows a generic info message without exposing if email exists.
	 */
	public void issueTempPassword() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		if (resetEmail == null || resetEmail.trim().isEmpty()) {
			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "יש להזין אימייל", null));
			return;
		}
		try {
			String temp = userService.issueTempPassword(resetEmail.trim());
			ctx.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_INFO,
					(temp == null)
					? "אם האימייל קיים במערכת – הוגדרה סיסמה זמנית."
							: "הוגדרה לך סיסמה זמנית: " + temp + " . לאחר ההתחברות, יש לשנות סיסמה.",
							null));
			this.resetEmail = null;
		} catch (Exception e) {
			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "שגיאה באיפוס הסיסמה", null));
			e.printStackTrace();
		}
	}

	/**
	 * Change the current user's password.
	 * Validates length and confirmation before calling the service (which hashes).
	 */
	public void changePassword() {
		FacesContext ctx = FacesContext.getCurrentInstance();

		User u = getLoggedInUser();
		if (u == null) {
			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "יש להתחבר למערכת", null));
			return;
		}
		if (password == null || password.length() < 4) {
			ctx.addMessage("newPass", new FacesMessage(FacesMessage.SEVERITY_ERROR, "סיסמה חייבת 4 תווים לפחות", null));
			return;
		}
		if (confirmPassword == null || !confirmPassword.equals(password)) {
			ctx.addMessage("confirmPass", new FacesMessage(FacesMessage.SEVERITY_ERROR, "האישור אינו תואם לסיסמה", null));
			return;
		}

		try {
		    userService.updatePassword(u.getId(), password); 
		    ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "הסיסמה עודכנה בהצלחה", null));
		    this.password = null;
		    this.confirmPassword = null;
		} catch (IllegalArgumentException iae) {
		    ctx.addMessage("newPass", new FacesMessage(FacesMessage.SEVERITY_ERROR, iae.getMessage(), null));
		} catch (Exception e) {
		    ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "שגיאה בעדכון סיסמה", null));
		}

	}

	//***************************************** Getters & Setters *********************************************//

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public User getLoggedInUser() { return loggedInUser; }
	public void setLoggedInUser(User loggedInUser) { this.loggedInUser = loggedInUser; }

	public String getResetEmail() { return resetEmail; }
	public void setResetEmail(String resetEmail) { this.resetEmail = resetEmail; }

	public String getConfirmPassword() { return confirmPassword; }
	public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
//===============================================================================================================================//
