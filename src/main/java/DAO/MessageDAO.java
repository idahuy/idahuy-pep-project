package DAO;

import Model.Message;
import Util.ConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    private Connection connection;

    public MessageDAO() {
        connection = ConnectionUtil.getConnection();
    }

    public Message createMessage(Message message) {
        String insertQuery = "INSERT INTO message (posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, message.getPosted_by());
            statement.setString(2, message.getMessage_text());
            statement.setLong(3, message.getTime_posted_epoch());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                return null;
            }
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int messageId = generatedKeys.getInt(1);
                    message.setMessage_id(messageId);
                    return message;
                } else {
                    throw new SQLException("Creating message failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public Message getMessageById(int id) {
        String query = "SELECT * FROM message WHERE message_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int userId = resultSet.getInt("posted_by");
                String messageText = resultSet.getString("message_text");
                long timePostedEpoch = resultSet.getLong("time_posted_epoch");
                return new Message(id, userId, messageText, timePostedEpoch);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }    

    public List<Message> getMessagesByUserId(int userId) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM message WHERE posted_by = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("message_id");
                String messageText = resultSet.getString("message_text");
                long timePostedEpoch = resultSet.getLong("time_posted_epoch");
                messages.add(new Message(id, userId, messageText, timePostedEpoch));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        String selectQuery = "SELECT * FROM message";
        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            ResultSet resultSet = selectStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("message_id");
                int userId = resultSet.getInt("posted_by");
                String messageText = resultSet.getString("message_text");
                long timePostedEpoch = resultSet.getLong("time_posted_epoch");
                messages.add(new Message(id, userId, messageText, timePostedEpoch));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // If an exception occurs, return an empty list instead of null
            return new ArrayList<>();
        }
        // If there are no messages, return an empty list instead of null
        if (messages.isEmpty()) {
            return new ArrayList<>();
        }
        return messages;
    }      

    public boolean updateMessage(Message message) {
        String query = "UPDATE message SET message_text = ?, time_posted_epoch = ? WHERE message_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, message.getMessage_text());
            preparedStatement.setLong(2, message.getTime_posted_epoch());
            preparedStatement.setInt(3, message.getMessage_id());
            int result = preparedStatement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }    

    public boolean deleteMessage(int messageId) {
        String query = "DELETE FROM message WHERE message_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, messageId);
            int result = preparedStatement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Message getMessageByDetails(int postedBy, String messageText, long timePostedEpoch) {
        String query = "SELECT * FROM message WHERE posted_by = ? AND message_text = ? AND time_posted_epoch = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, postedBy);
            preparedStatement.setString(2, messageText);
            preparedStatement.setLong(3, timePostedEpoch);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("message_id");
                return new Message(id, postedBy, messageText, timePostedEpoch);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}