package com.pb168.expensemanager;

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
    
    /**
     * Stores new account into database. Id for the new account is automatically
     * generated and stored into id attribute.
     * 
     * @param account account to be created.
     * @throws IllegalArgumentException when account is null, or account has already 
     * assigned id.
     * @throws  ServiceFailureException when db operation fails.
     */
    void createAccount(Account account) throws ServiceFailureException;
    
    /**
     * Updates account in database.
     * 
     * @param account updated account to be stored into database.
     * @throws IllegalArgumentException when account is null, or account has null id.
     * @throws  ServiceFailureException when db operation fails.
     */
    void updateAccount(Account account) throws ServiceFailureException;
    
    /**
     * Deletes account from database. 
     * 
     * @param accountId account to be deleted from db.
     * @throws IllegalArgumentException when account is null, or account has null id.
     * @throws  ServiceFailureException when db operation fails.
     */
    void deleteAccount(Long accountId) throws ServiceFailureException;  
    
    /**
     * Returns account with given id.
     * 
     * @param id primary key of requested account.
     * @return account with given id or null if such account does not exist.
     * @throws IllegalArgumentException when given id is null.
     * @throws  ServiceFailureException when db operation fails.
     */
    Account getAccount(Long id) throws ServiceFailureException;
    
    /**
     * Returns list of all accounts in the database.
     * 
     * @return list of all accounts in database.
     * @throws  ServiceFailureException when db operation fails.
     */
    List<Account> findAllAccounts() throws ServiceFailureException;

}
