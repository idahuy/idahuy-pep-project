package Service;

import Model.Account;
import DAO.AccountDAO;
import java.util.List;
import java.util.ArrayList;

public class AccountService {
    
    private AccountDAO accountDAO;
    private List<Account> accounts = new ArrayList<>();

    public AccountService() {
        accountDAO = new AccountDAO();
    }

    public Account createAccount(Account account) {
        return accountDAO.createAccount(account);
    }

    public Account getAccountById(int id) {
        return accountDAO.getAccountById(id);
    }

    public Account getAccountByUsername(String username) {
        return accountDAO.getAccountByUsername(username);
    }

    public List<Account> getAllAccounts() {
        return accountDAO.getAllAccounts();
    }

    public boolean usernameExists(String username) {
        return getAllAccounts().stream().anyMatch(account -> account.getUsername().equals(username));
    }

    public Account getAccountByUsernameAndPassword(String username, String password) {
        Account account = getAccountByUsername(username);
        if (account != null && account.getPassword().equals(password)) {
            return account;
        } else {
            return null;
        }
    }
    
}