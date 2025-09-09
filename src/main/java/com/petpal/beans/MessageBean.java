package com.petpal.beans;

import com.petpal.model.Animal;
import com.petpal.model.Message;
import com.petpal.model.User;
import com.petpal.service.MessageService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;



/**
 * JSF ManagedBean (ViewScoped) for handling user messages.
 *
 * Responsibilities:
 * - Load inbox and sent messages for the logged-in user.
 * - Send new messages (direct or replies).
 * - Delete messages (from inbox or sent).
 * - Prepare reply forms and handle selected animal references.
 *
 * Works together with {@link MessageService}.
 */
//===============================================================================================================================//

@ManagedBean(name = "messageBean")
@ViewScoped
public class MessageBean implements Serializable {

	private static final long serialVersionUID = 1L;

    /** Service layer that performs DB operations for messages */
	private final MessageService messageService = new MessageService();

    /** Injected UserBean to retrieve the logged-in user */
	@ManagedProperty("#{userBean}")
	private UserBean userBean;             

    /** New message being composed (direct or reply) */
	private Message newMessage = new Message();
	
    /** For sending message about a specific animal */
	private Animal selectedAnimal;
	private Long animalId;

    /** Cached inbox & sent lists */
	private List<Message> inbox;
	private List<Message> sent;

    /** If replying, holds ID of the original message */
	private Long replyingToId;


    //***************************************** Navigation / Loading *********************************************//

	
	
    /** Redirects to send_message.xhtml with an animalId parameter */
	public String redirectToSendMessage(Long animalId) {
		this.animalId = animalId;
		return "send_message.xhtml?faces-redirect=true&animalId=" + animalId;
	}

	
    /** Loads sent messages for current user  */
	public void loadInbox() {
		if (FacesContext.getCurrentInstance().isPostback()) return;
		User current = userBean.getLoggedInUser();
		if (current != null) {
			inbox = messageService.loadInbox(current);
		}
	}

    /** Loads sent messages for current user (skips reload on postback) */
	public void loadSent() {
		if (FacesContext.getCurrentInstance().isPostback()) return;
		User current = userBean.getLoggedInUser();
		if (current != null) {
			sent = messageService.loadSent(current);
		}
	}

	
	
    /** Loads the selected animal if animalId exists (skips on postback) */
	public void loadSelectedAnimal() {
		if (animalId != null && !FacesContext.getCurrentInstance().isPostback()) {
			selectedAnimal = messageService.findAnimal(animalId);
		}
	}

    //***************************************** Reply *********************************************//

	
	   /**
     * Prepares a reply message to an existing one.
     * Sets recipient to the original sender, copies subject and animal.
     */
	public void startReply(Long messageId) {
		Message original = messageService.findMessage(messageId);
		if (original != null) {
			replyingToId = messageId;
			newMessage = new Message();
			newMessage.setRecipient(original.getSender());
			newMessage.setSubject("Re: " + (original.getSubject() != null ? original.getSubject() : ""));
			newMessage.setAnimal(original.getAnimal());
		}
	}

	
	
    //***************************************** Sending *********************************************//

	
	  /**
     * Sends a new message (direct or reply).
     * Validates recipient, subject, and content.
     * After success, resets form and reloads inbox/sent.
     */
	public String sendMessage() {
		FacesContext fc = FacesContext.getCurrentInstance();
		User currentUser = userBean.getLoggedInUser();
		if (currentUser == null) {
			fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "יש להתחבר למערכת", null));
			return null;
		}

        // If no recipient but a selected animal exists → send to owner
		if (newMessage.getRecipient() == null && selectedAnimal != null) {
			newMessage.setRecipient(selectedAnimal.getOwner());
			newMessage.setAnimal(selectedAnimal);
		}
		

        // Validation
		if (newMessage.getRecipient() == null) {
			fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "נמען חסר", null));
			return null;
		}
		if (newMessage.getSubject() == null || newMessage.getSubject().trim().isEmpty()) {
			fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "נושא הינו שדה חובה", null));
			return null;
		}
		if (newMessage.getContent() == null || newMessage.getContent().trim().isEmpty()) {
			fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "תוכן ההודעה הינו שדה חובה", null));
			return null;
		}

		try {
			messageService.sendMessage(
					currentUser,
					newMessage.getRecipient(),
					newMessage.getAnimal(),
					newMessage.getSubject(),
					newMessage.getContent(),
					LocalDateTime.now());

            // Reset UI state
			newMessage = new Message();
			replyingToId = null;

            // Refresh both inbox and sent
			inbox = messageService.loadInbox(currentUser);
			sent = messageService.loadSent(currentUser);

			return "inbox?faces-redirect=true";
		} catch (Exception e) {
			fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "שגיאה בשליחה", null));
			return null;
		}
	}

    //***************************************** Deletion *********************************************//

	
    /** Deletes a message from inbox (if belongs to current user) */
	public void deleteFromInbox(Long messageId) {
		FacesContext ctx = FacesContext.getCurrentInstance();
		User me = userBean.getLoggedInUser();
		try {
			boolean ok = messageService.deleteFromInbox(me, messageId);
			if (ok) {
				ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "נמחק", "ההודעה נמחקה"));
				inbox = messageService.loadInbox(me);
			} else {
				ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "שגיאה", "אין הרשאה למחוק"));
			}
		} catch (Exception e) {
			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "שגיאה במחיקה", null));
		}
	}

	
    /** Deletes a message from sent (if belongs to current user) */
	public void deleteFromSent(Long messageId) {
		FacesContext ctx = FacesContext.getCurrentInstance();
		User me = userBean.getLoggedInUser();
		try {
			boolean ok = messageService.deleteFromSent(me, messageId);
			if (ok) {
				ctx.addMessage(null, new FacesMessage("ההודעה נמחקה."));
				sent = messageService.loadSent(me);
			} else {
				ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "שגיאה", "אין הרשאה למחוק"));
			}
		} catch (Exception e) {
			ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "שגיאה במחיקה", null));
		}
	}

    //***************************************** Getters & Setters *********************************************//

	public Message getNewMessage() { return newMessage; }
	public void setNewMessage(Message newMessage) { this.newMessage = newMessage; }

	public Animal getSelectedAnimal() { return selectedAnimal; }
	public void setSelectedAnimal(Animal selectedAnimal) { this.selectedAnimal = selectedAnimal; }

	public Long getAnimalId() { return animalId; }
	public void setAnimalId(Long animalId) { this.animalId = animalId; }

	public List<Message> getInbox() { return inbox; }
	public void setInbox(List<Message> inbox) { this.inbox = inbox; }

	public List<Message> getSent() { return sent; }
	public void setSent(List<Message> sent) { this.sent = sent; }

	public Long getReplyingToId() { return replyingToId; }
	public void setReplyingToId(Long replyingToId) { this.replyingToId = replyingToId; }
	
	public void setUserBean(UserBean userBean) { this.userBean = userBean; }



}
//===============================================================================================================================//
