/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.sql.Connection;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import com.pb168.expensemanager.*;

/**
 *
 * @author rk
 */
public class AccountManagerImplTest {

    private AccountManagerImpl manager;
    private DataSource dataSource;
    
    @Before
    public void setUp() throws SQLException {
                
        BasicDataSource bds = new BasicDataSource();
        bds.setUrl("jdbc:derby:memory:AccountManagerTest;create=true");
        this.dataSource = bds;
        
        try (Connection conn = bds.getConnection()) {                 
            conn.prepareStatement("CREATE TABLE ACCOUNT ("
                    + "id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "name VARCHAR(30) NOT NULL,"
                    + "description VARCHAR(255),"
                    + "creationdate DATE NOT NULL,"
                    + "cancellationdate DATE)").executeUpdate();
        } 
        manager = new AccountManagerImpl(bds);
    }

   @After
    public void tearDown() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.prepareStatement("DROP TABLE ACCOUNT").executeUpdate();
        }
    }

    @Test
    public void testCreateAccount() throws SQLException {
        Account account = newAccount("MyAccount", "", LocalDate.of(2014, Month.JANUARY, 1));
        manager.createAccount(account);

        Long accountId = account.getId();
        assertNotNull(accountId);
        Account result = manager.getAccount(accountId);
        assertEquals(account, result);
        assertNotSame(account, result);
    }

    @Test
    public void testGetAccount() {
        assertNull(manager.getAccount(1L));

        Account account = newAccount("MyAccount", "", LocalDate.of(2015, Month.JANUARY, 9));
        manager.createAccount(account);
        Long accountId = account.getId();

        Account result = manager.getAccount(accountId);
        assertEquals(account, result);
    }

    @Test
    public void testUpdateAccount() {
        Account account = newAccount("MyAccount", "", LocalDate.of(2015, Month.JANUARY, 9));
        Account account2 = newAccount("MySecondAccount", "Note", LocalDate.of(2013, Month.JANUARY, 1));
        manager.createAccount(account);
        manager.createAccount(account2);
        
        Long accountId = account.getId();
        Long account2Id = account2.getId();
        
        account = manager.getAccount(accountId);
        account.setDescription("newDesc");
        manager.updateAccount(account);
        
        assertEquals("MyAccount", manager.getAccount(accountId).getName());
        assertEquals("newDesc", manager.getAccount(accountId).getDescription());
        assertEquals(accountId, manager.getAccount(accountId).getId());
        assertEquals(LocalDate.of(2015, Month.JANUARY, 9), manager.getAccount(accountId).getCreationDate());
        
        assertEquals("MySecondAccount", manager.getAccount(account2Id).getName());
        assertEquals("Note", manager.getAccount(account2Id).getDescription());
        assertEquals(account2Id, manager.getAccount(account2Id).getId());
        assertEquals(LocalDate.of(2013, Month.JANUARY, 1), manager.getAccount(account2Id).getCreationDate());
                
        assertNotSame(account, account2);
    }

    @Test
    public void testDeleteAccount(){
        Account account = newAccount("Acc1", "", LocalDate.of(2014, Month.JUNE, 9));
        Account account2 = newAccount("Acc2", "Note", LocalDate.of(2013, Month.DECEMBER, 1));
        manager.createAccount(account);
        manager.createAccount(account2);
    
        Long accountId = account.getId();
        Long account2Id = account2.getId();
        
        manager.deleteAccount(account.getId());
        
        try {
            manager.getAccount(accountId);
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        assertEquals(account2Id, manager.getAccount(account2Id).getId());               
    }

    
    //help method to create object with pre-setted atributes
    private static Account newAccount(String name, String description, LocalDate creationDate) {
        Account account = new Account();
        account.setName(name);
        account.setDescription(description);
        account.setCreationDate(creationDate);
        return account;
    }   
}
