package Service;

import Model.Message;
import DAO.MessageDAO;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


public class MessageService {

    private MessageDAO messageDAO;
    private List<Message> messagesCache;

    public MessageService() {
        messageDAO = new MessageDAO();
        messagesCache = new ArrayList<>();
    }

    public Message createMessage(Message message) {
        return messageDAO.createMessage(message);
    }  

    public static class MessageNotFoundException extends RuntimeException {
        public MessageNotFoundException(String message) {
            super(message);
        }
    }
    
    public Message getMessageById(int id) {
        Message message = messageDAO.getMessageById(id);
        if (message == null) {
            throw new MessageNotFoundException("Message not found");
        }
        return message;
    }    
    
    public List<Message> getMessagesByUserId(int posted_by) {
        return messageDAO.getMessagesByUserId(posted_by);
    }

    public List<Message> getAllMessages() {
        List<Message> messages = messageDAO.getAllMessages();
        if (messages == null) {
            return Collections.emptyList();
        }
        return messages;
    }   

    public boolean updateMessage(Message message) throws MessageNotFoundException {
        int messageId = message.getMessage_id();
        Message existingMessage = messageDAO.getMessageById(messageId);
        if (existingMessage == null) {
            throw new MessageNotFoundException("Message not found with ID: " + messageId);
        } else {
            existingMessage.setMessage_text(message.getMessage_text());
            existingMessage.setTime_posted_epoch(message.getTime_posted_epoch());
            return messageDAO.updateMessage(existingMessage);
        }
    }
    
    public Message deleteMessage(int messageId) {
        Message message = messageDAO.getMessageById(messageId);
        if (message != null) {
            boolean deleted = messageDAO.deleteMessage(messageId);
            if (deleted) {
                return message;
            }
        }
        return null;
    }  
    
    public Message getMessageByDetails(int postedBy, String messageText, long timePostedEpoch) {
        return messageDAO.getMessageByDetails(postedBy, messageText, timePostedEpoch);
    }
    
    public void removeMessageFromCache(int messageId) {
        for (int i = 0; i < messagesCache.size(); i++) {
            if (messagesCache.get(i).getMessage_id() == messageId) {
                messagesCache.remove(i);
                return;
            }
        }
    }
    
}