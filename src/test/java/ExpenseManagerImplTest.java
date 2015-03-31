/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.pb168.expensemanager.Account;
import com.pb168.expensemanager.AccountManager;
import com.pb168.expensemanager.AccountManagerImpl;
import com.pb168.expensemanager.DBUtils;
import com.pb168.expensemanager.ExpenseManagerImpl;
import com.pb168.expensemanager.Payment;
import com.pb168.expensemanager.PaymentManagerImpl;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rk
 */
public class ExpenseManagerImplTest {
    
    private AccountManagerImpl accountManager;
    private PaymentManagerImpl paymentManager;
    private ExpenseManagerImpl expenseManager;
    
    private DataSource dataSource;
    
    
    private static DataSource prepareDataSource() throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        //we will use in memory database
        ds.setUrl("jdbc:derby:memory:ExpenseManagerTest;create=true");
        return ds;
    }
    
    public ExpenseManagerImplTest() {
    }
    
    @Before
    public void setUp() throws SQLException {            
       
        dataSource = prepareDataSource();
        
        try (Connection conn = dataSource.getConnection()) {                 
            conn.prepareStatement("CREATE TABLE ACCOUNT ("
                    + "id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "name VARCHAR(30) NOT NULL,"
                    + "description VARCHAR(255),"
                    + "creationdate DATE NOT NULL,"
                    + "cancellationdate DATE)").executeUpdate();
        } 
        
        try (Connection conn = dataSource.getConnection()) {                 
            conn.prepareStatement("CREATE TABLE PAYMENT ("
                    + "id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "ACCOUNTID BIGINT REFERENCES ACCOUNT (ID),                 "
                    + "description VARCHAR(255) NOT NULL,"
                    + "date DATE NOT NULL,"
                    + "amount DECIMAL(12,2) NOT NULL)").executeUpdate();
        }
                              
        expenseManager = new ExpenseManagerImpl();
        expenseManager.setDataSource(dataSource);        
        accountManager = new AccountManagerImpl(dataSource);
        paymentManager = new PaymentManagerImpl(dataSource);
    }
    
    
    @After
    public void tearDown() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.prepareStatement("DROP TABLE PAYMENT").executeUpdate();
            conn.prepareStatement("DROP TABLE ACCOUNT").executeUpdate();            
        }
    }

    @Test
    public void testAddPaymentToAccount() throws SQLException {
        Account account = newAccount("MyAccount", "", LocalDate.of(2015, Month.JANUARY, 1));
        accountManager.createAccount(account);
        
        Payment payment = newPayment("Running shoes Nike",LocalDate.of(2015,Month.MARCH,6),new BigDecimal("-139.99"));
        paymentManager.createPayment(payment);
        
        expenseManager.addPaymentToAccount(payment, account);
        
        List<Payment> result = new ArrayList<Payment>();
        result = expenseManager.getAllPaymentsInAccount(account);

        Payment payment2 = result.get(0);
        
        assertEquals(payment, payment2);
    }
    
    @Test
    public void testRemovePaymentToAccount() throws SQLException {
        Account account = newAccount("MyAccount", "", LocalDate.of(2015, Month.JANUARY, 1));
        accountManager.createAccount(account);
        
        Payment payment = newPayment("Running shoes Nike",LocalDate.of(2015,Month.MARCH,8),new BigDecimal("-139.99"));
        paymentManager.createPayment(payment);
        
        Payment payment2 = newPayment("Sold Running shoes Adidas",null,new BigDecimal("29.99"));
        paymentManager.createPayment(payment2);
        
        expenseManager.addPaymentToAccount(payment, account);
        expenseManager.addPaymentToAccount(payment2, account);
        
        List<Payment> result = new ArrayList<Payment>();
        result = expenseManager.getAllPaymentsInAccount(account);
        assertEquals(result.size(), 2);
        
        expenseManager.removePaymentFromAccount(payment, account);
        result = expenseManager.getAllPaymentsInAccount(account);
        assertEquals(result.size(), 1);
        
        Payment payment3 = result.get(0);
        
        assertEquals(payment2, payment3);
        paymentManager.deletePayment(payment);
    }
    
    @Test
    public void testGetAccountBalance() throws SQLException {
        Account account = newAccount("MyAccount", "", LocalDate.of(2015, Month.JANUARY, 1));
        accountManager.createAccount(account);
        
        Payment payment = newPayment("Running shoes Nike",LocalDate.of(2015,Month.MARCH,8),new BigDecimal("-139.99"));
        paymentManager.createPayment(payment);
        
        Payment payment2 = newPayment("Sold Running shoes Adidas",null,new BigDecimal("29.99"));
        paymentManager.createPayment(payment2);
        
        Payment payment3 = newPayment("Found",LocalDate.of(2015,Month.MAY,8),new BigDecimal("0.01"));
        paymentManager.createPayment(payment3);
        
        expenseManager.addPaymentToAccount(payment, account);
        expenseManager.addPaymentToAccount(payment2, account);
        expenseManager.addPaymentToAccount(payment3, account);
         
        BigDecimal count = payment.getAmount();
        count = count.add(payment2.getAmount());
        count = count.add(payment3.getAmount());
        
        
        //System.out.println('');
        assertEquals(expenseManager.getAccountBalance(account), count);
    }
    
        @Test
    public void testGetAccountBalanceForPeriod() throws SQLException {
        Account account = newAccount("MyAccount", "", LocalDate.of(2015, Month.JANUARY, 1));
        accountManager.createAccount(account);
        
        Payment payment = newPayment("Running shoes Nike",LocalDate.of(2015,Month.MARCH,8),new BigDecimal("-139.99"));
        paymentManager.createPayment(payment);
        
        Payment payment2 = newPayment("Sold Running shoes Adidas",LocalDate.of(2015,Month.MARCH,5),new BigDecimal("29.99"));
        paymentManager.createPayment(payment2);
        
        Payment payment3 = newPayment("Found",LocalDate.of(2015,Month.MAY,10),new BigDecimal("0.02"));
        paymentManager.createPayment(payment3);
        
        expenseManager.addPaymentToAccount(payment, account);
        expenseManager.addPaymentToAccount(payment2, account);
        expenseManager.addPaymentToAccount(payment3, account);
         
        BigDecimal count = payment.getAmount();
        count = count.add(payment3.getAmount());
        
        assertEquals(expenseManager.getAccountBalanceForPeriod(account,LocalDate.of(2015,Month.MARCH,7),LocalDate.of(2015,Month.MAY,10)), count);
    }
    
    

    //help method to create object with pre-setted atributes
    private static Account newAccount(String name, String description, LocalDate creationDate) {
        Account account = new Account();
        account.setName(name);
        account.setDescription(description);
        account.setCreationDate(creationDate);
        return account;
    }   
    
        private static Payment newPayment(String description, LocalDate date, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setDescription(description);
        payment.setDate(date);
        payment.setAmount(amount);
        return payment;
    }

}
