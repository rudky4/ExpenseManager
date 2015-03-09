package expensemanager;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rk
 */

import java.util.List;


public interface AccountManager {
    
    void createAccount(Account account) throws ServiceFailureException;
    
    void updateAccount(Account account) throws ServiceFailureException;
    
    void deleteAccount(Account account) throws ServiceFailureException;  
    
    
    
}
