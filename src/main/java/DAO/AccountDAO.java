package DAO;

import Model.Account;
import Util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class AccountDAO {
    private final Connection connection;

    public AccountDAO() {
        this.connection = ConnectionUtil.getConnection();
    }

    public Account createAccount(Account account) {
        String insertQuery = "INSERT INTO account (username, password) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, account.getUsername());
            statement.setString(2, account.getPassword());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                return null;
            }
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int accountId = generatedKeys.getInt(1);
                    account.setAccount_id(accountId);
                    return account;
                } else {
                    throw new SQLException("Creating account failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    
    public Account getAccountById(int id) {
        String query = "SELECT * FROM account WHERE account_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    String password = rs.getString("password");
                    Account account = new Account(username, password);
                    account.setAccount_id(id);
                    return account;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    
    public Account getAccountByUsername(String username) {
        String query = "SELECT * FROM account WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int accountId = rs.getInt("account_id");
                    String password = rs.getString("password");
                    Account account = new Account(username, password);
                    account.setAccount_id(accountId);
                    return account;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }    

    // Add this method to your AccountDAO class
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String query = "SELECT * FROM account";
    
        try (PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                int accountId = rs.getInt("account_id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                Account account = new Account(username, password);
                account.setAccount_id(accountId);
                accounts.add(account);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    
        return accounts;
    }
}