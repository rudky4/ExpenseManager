/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pb168.expensemanager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rk
 */
public class AccountManagerImpl implements AccountManager{
    
    final static Logger log = LoggerFactory.getLogger(AccountManagerImpl.class);

    private final DataSource dataSource;

    public AccountManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void createAccount(Account account) throws ServiceFailureException{
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        if (account.getId() != null) {
            throw new IllegalArgumentException("account id is already set");
        }
        if (account.getName() == null) {
            throw new IllegalArgumentException("account name is null");
        }
        if (account.getCreationDate() == null) {
            throw new IllegalArgumentException("account creation date is null");
        }
        
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO ACCOUNT (name,description,creationdate) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, account.getName() );
                st.setString(2, account.getDescription());
                if(account.getCreationDate() != null){
                st.setDate(3, java.sql.Date.valueOf(account.getCreationDate()));
                } else{ 
                st.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                }
                
                st.executeUpdate();
                ResultSet keys= st.getGeneratedKeys();
                if (keys.next()) {
                    account.setId(keys.getLong(1));
                }        
            }            
        } catch (SQLException ex) {
            log.error("cannot insert account", ex);
            throw new ServiceFailureException("database insert failed", ex);
        }
    }
    
        private Long getKey(ResultSet keyRS, Account account) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert account " + account
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert account " + account
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert account " + account
                    + " - no key found");
        }
    }
    
    @Override
    public void updateAccount(Account account) throws ServiceFailureException{
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        if (account.getId() == null) {
            throw new IllegalArgumentException("account with null id cannot be updated");
        }
        if (account.getName() == null) {
            throw new IllegalArgumentException("account name is null");
        }
        if (account.getCreationDate() == null) {
            throw new IllegalArgumentException("account creation date is null");
        }

        try (Connection conn = dataSource.getConnection()) {
            try(PreparedStatement st = conn.prepareStatement("UPDATE account SET name=?,description=?,creationdate=?,cancellationdate=? WHERE id=?")) {
                st.setString(1, account.getName() );
                st.setString(2, account.getDescription());
                st.setDate(3, java.sql.Date.valueOf(account.getCreationDate()));
                if(account.getCancellationDate() != null)
                    st.setDate(4, java.sql.Date.valueOf(account.getCancellationDate()));
                else
                    st.setDate(4, null);
                st.setLong(5,account.getId());
                if(st.executeUpdate()!=1) {
                    throw new IllegalArgumentException("cannot update account "+account);
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when updating account", ex);
        }
    }
    
    @Override
    public void deleteAccount(Long accountId) throws ServiceFailureException{
        try (Connection conn = dataSource.getConnection()) {
            try(PreparedStatement st = conn.prepareStatement("DELETE FROM account WHERE id=?")) {
                st.setLong(1,accountId);
                if(st.executeUpdate()!=1) {
                    throw new ServiceFailureException("did not delete account with id ="+accountId);
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when deleting account", ex);
        }
    }  
    
    @Override
    public Account getAccount(Long id) throws ServiceFailureException{
    try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id, name, description, creationdate, cancellationdate FROM account WHERE id = ?")) {
                st.setLong(1, id);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    Account account = resultSetToAccount(rs);
                    if (rs.next()) {
                        throw new ServiceFailureException(
                                "Internal error: More entities with the same id found "
                                        + "(source id: " + id + ", found " + account + " and " + resultSetToAccount(rs));
                    }
                    return account;
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving account", ex);
        }
    }
    
    private Account resultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getLong("id"));
        account.setName(rs.getString("name"));
        if(rs.getString("description") != null)
        account.setDescription(rs.getString("description"));        
        if(rs.getDate("creationdate") != null)
        account.setCreationDate(rs.getDate("creationdate").toLocalDate());
        if(rs.getDate("cancellationdate") != null)
            account.setCancellationDate(rs.getDate("cancellationdate").toLocalDate());
        return account;
    }
    
    @Override
    public List<Account> findAllAccounts() throws ServiceFailureException{
        log.debug("finding all accounts");
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,name,description,creationdate,cancellationdate FROM account")) {
                ResultSet rs = st.executeQuery();
                List<Account> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToAccount(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all account", ex);
        }
    }
    
    public void createAccountTable() throws SQLException {                
        BasicDataSource bds = new BasicDataSource();
        bds.setUrl("jdbc:derby://localhost:1527/ExpenseDB");
        bds.setPassword("manager");
        bds.setUsername("manager");
        
        //create new empty table
        try (Connection conn = bds.getConnection()) {                 
            conn.prepareStatement("CREATE TABLE ACCOUNT ("
                    + "id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "name VARCHAR(30) NOT NULL,"
                    + "description VARCHAR(255),"
                    + "creationdate DATE NOT NULL,"
                    + "cancellationdate DATE)").executeUpdate();
        }         
        bds.close();
    }
}

