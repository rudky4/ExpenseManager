/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expensemanager;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author rk
 */
public class AccountManagerImplTest {
    
    private AccountManagerImpl manager;

    @Before
    public void setUp() throws SQLException {
        manager = new AccountManagerImpl();
    }

    @Test
    public void testCreateAccount() {
        Account account = newAccount("MyAccount","",new Date(2015,2,25));
        manager.createAccount(account);
        
        Long accountId = account.getId();        
        assertNotNull(accountId);        
        Account result = manager.getAccount(accountId);
        assertEquals(account, result);
        assertNotSame(account, result);        
    }
    
    @Test
    public void getContact() {
        
        assertNull(manager.getAccount(1l));
        
        Account account = newAccount("MyAccount","",new Date(2015,5,9));
        manager.createAccount(account);
        Long accountId = account.getId();

        Account result = manager.getAccount(accountId);
        assertEquals(account, result);
    }
    
    @Test
    public void updateGrave() {
        Account account = newAccount("MyAccount","",new Date(2015,5,9));
        Account account2 = newAccount("MySecondAccount","Notew",new Date(2013,5,9));
        manager.createAccount(account);
        manager.createAccount(account2);
        Long accountId = account.getId();

        account = manager.getAccount(accountId);
        account.setDescription("newDesc");
        manager.updateAccount(account);        
        assertEquals("newDesc", account.getDescription());
        assertEquals(accountId, account.getId());
        assertNotSame(account, account2);
    }
       
    private static Account newAccount(String name, String description, Date creationDate) {
        Account account = new Account();
        account.setName(name);
        account.setDescription(description);
        account.setCreationDate(creationDate);
        return account;
    }
    
    private static Comparator<Account> idComparator = new Comparator<Account>() {

        @Override
        public int compare(Account o1, Account o2) {
            return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
        }
    };
    
}
