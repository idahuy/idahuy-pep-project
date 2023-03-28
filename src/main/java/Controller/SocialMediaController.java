package Controller;

import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.SQLException;
import java.io.IOException;
import java.util.Map;


public class SocialMediaController {

    private final AccountService accountService;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;
    
    public SocialMediaController() {
        this.accountService = new AccountService();
        this.messageService = new MessageService();
        this.objectMapper = new ObjectMapper();
    }

    public SocialMediaController(AccountService accountService, MessageService messageService) {
        this.accountService = accountService;
        this.messageService = messageService;
        this.objectMapper = new ObjectMapper();
    }

    public Javalin startAPI() {
        Javalin app = Javalin.create();

        // creating new user
        app.post("/register", this::registerHandler);

        // user login
        app.post("/login", this::loginHandler);

        // creating new message
        app.post("/messages", this::postMessageHandler);

        // get all messages
        app.get("/messages", this::getAllMessagesHandler);

        // getting a message by id
        app.get("/messages/{id}", this::getMessageHandler);

        app.get("/accounts/{id}/messages", this::getMessagesByAccountIdHandler);

        // getting all posts by user
        //app.get("/users/{username}/messages", this::getUserMessageHandler);

        // update a message by id
        app.patch("/messages/{id}", this::updateMessageHandler);

        // delete a message by id
        app.delete("/messages/{id}", this::deleteMessageHandler);


        return app;
    }
    
    public void registerHandler(Context context) {
        ObjectMapper objectMapper = new ObjectMapper();
    
        String requestBody = context.body();
        Account inputAccount;
        try {
            inputAccount = objectMapper.readValue(requestBody, Account.class);
        } catch (IOException e) {
            context.status(400); // Bad request
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "Invalid request body. Please provide a valid JSON object.");
            context.json(errorNode);
            return;
        }
    
        String username = inputAccount.getUsername();
        String password = inputAccount.getPassword();
    
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            context.status(400); // Bad request
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "Both 'username' and 'password' fields are required.");
            context.json(errorNode);
            return;
        }
    
        if (password.length() < 8) {
            context.status(400); // Bad request
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "Password must be at least 8 characters long.");
            errorNode.put("errorCode", 1);
            context.json(errorNode);
            return;
        }
    
        if (accountService.usernameExists(username)) {
            context.status(400); // Bad request
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "Username already exists.");
            context.json(errorNode);
            return;
        }
    
        Account account = new Account(username, password);
        Account createdAccount = accountService.createAccount(account);
    
        if (createdAccount != null) {
            context.status(200); // OK
            context.json(createdAccount);
        } else {
            context.status(500); // Internal Server Error
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "Failed to create user account.");
            context.json(errorNode);
        }
    }    
    
    public void loginHandler(Context context) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode requestBody = objectMapper.readTree(context.body());
    
            String username = requestBody.get("username").asText();
            String password = requestBody.get("password").asText();
    
            if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                context.status(400); // Bad request
                ObjectNode errorNode = objectMapper.createObjectNode();
                errorNode.put("error", "Both 'username' and 'password' fields are required.");
                context.json(errorNode);
                return;
            }
    
            Account account = accountService.getAccountByUsernameAndPassword(username, password);
    
            if (account != null) {
                context.status(200); // OK
                context.header("Content-Type", "application/json");
                context.json(account);
            } else {
                context.status(401); // Unauthorized
                context.result("{\"error\":\"Invalid username or password.\"}");
            }
        } catch (JsonProcessingException e) {
            context.status(400); // Bad request
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "Invalid request body. Please provide a valid JSON object.");
            context.json(errorNode);
        } catch (Exception e) {
            context.status(500); // Internal Server Error
            context.result("{\"error\":\"Failed to process request.\"}");
        }
    }

    public void getAllMessagesHandler(Context context) {
        List<Message> messages = messageService.getAllMessages();
        context.status(200); // OK
        context.header("Content-Type", "application/json");
        context.json(messages);
    }

    public void postMessageHandler(Context context) {
        try {
            Message message = context.bodyAsClass(Message.class);
    
            int postedBy = message.getPosted_by();
            Account account = accountService.getAccountById(postedBy);
    
            if (account == null) {
                context.status(400); // Bad request
                context.header("Content-Type", "application/json");
                context.result("{\"error\":\"Account does not exist.\"}");
                return;
            }
    
            // Check if message text is too long
            if (message.getMessage_text().length() > 255) {
                context.status(400); // Bad request
                context.header("Content-Type", "application/json");
                context.result("{\"error\":\"Message text is too long.\"}");
                return;
            }
    
            // Check if message text is blank
            if (message.getMessage_text().isEmpty()) {
                context.status(400); // Bad request
                context.header("Content-Type", "application/json");
                context.result("{\"error\":\"Message text is blank.\"}");
                return;
            }
    
            // Create and save the message using the MessageService
            Message createdMessage = messageService.createMessage(message);
    
            if (createdMessage != null) {
                context.status(200); // OK
                context.header("Content-Type", "application/json");
                context.json(createdMessage);
            } else {
                context.status(500); // Internal Server Error
                context.result("{\"error\":\"Failed to create message.\"}");
            }
        } catch (Exception e) {
            context.status(400); // Bad request
            context.header("Content-Type", "application/json");
            context.result("{\"error\":\"Invalid request data.\"}");
        }
    }

    public void getMessageHandler(Context context) {
        int messageId = Integer.parseInt(context.pathParam("id"));
        Message message;
    
        try {
            message = messageService.getMessageById(messageId);
            context.header("Content-Type", "application/json");
            context.json(message);
        } catch (MessageService.MessageNotFoundException e) {
            context.status(200); // OK
            context.result(""); // Empty body
        }
    }    

    public void getMessagesByAccountIdHandler(Context context) {
        int accountId = Integer.parseInt(context.pathParam("id"));
    
        List<Message> messages = messageService.getMessagesByUserId(accountId);
    
        if (messages != null) {
            context.status(200); // OK
            context.header("Content-Type", "application/json");
            context.json(messages);
        } else {
            context.status(404); // Not Found
            context.result("{\"error\":\"No messages found for this account.\"}");
        }
    }
    
    public void deleteMessageHandler(Context context) {
        int messageId = Integer.parseInt(context.pathParam("id"));
        Message message = null;
    
        try {
            message = messageService.getMessageById(messageId);
        } catch (MessageService.MessageNotFoundException e) {
            context.status(200); // OK
            context.result(""); // empty body
            return;
        }
    
        Message deletedMessage = messageService.deleteMessage(messageId);
    
        if (deletedMessage != null) {
            context.status(200); // OK
            context.header("Content-Type", "application/json");
            context.json(deletedMessage);
        } else {
            context.status(500); // Internal Server Error
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "Failed to delete message.");
            context.json(errorNode);
        }
    }    
    
    public void updateMessageHandler(Context context) throws JsonProcessingException {
        int messageId = Integer.parseInt(context.pathParam("id"));
        Message existingMessage;
    
        try {
            existingMessage = messageService.getMessageById(messageId);
        } catch (MessageService.MessageNotFoundException e) {
            context.status(400); // Bad Request
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "Message with id " + messageId + " does not exist.");
            context.json(errorNode);
            return;
        }
    
        JsonNode requestBody = context.bodyAsClass(JsonNode.class);
        if (requestBody.get("message_text") == null || requestBody.get("message_text").asText().isEmpty()) {
            context.status(400); // Bad Request
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "Message text is required.");
            context.json(errorNode);
        } else if (requestBody.get("message_text").asText().length() > 255) {
            context.status(400); // Bad Request
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("error", "Message text cannot be longer than 255 characters.");
            context.json(errorNode);
        } else {
            existingMessage.setMessage_text(requestBody.get("message_text").asText());
            boolean updated = messageService.updateMessage(existingMessage);
            if (updated) {
                context.status(200); // OK
                context.header("Content-Type", "application/json");
                context.json(existingMessage);
            } else {
                context.status(500); // Internal Server Error
                ObjectNode errorNode = objectMapper.createObjectNode();
                errorNode.put("error", "Failed to update message.");
                context.json(errorNode);
            }
        }
    }    
    
}