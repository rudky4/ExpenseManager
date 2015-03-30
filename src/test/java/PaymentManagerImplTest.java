/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import com.pb168.expensemanager.*;
import java.sql.Connection;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;

/**
 *
 * @author charlliz
 */
public class PaymentManagerImplTest {
    
    private PaymentManagerImpl manager;
    private DataSource dataSource;
    
    @Before
    public void setUp() throws SQLException{
        
        BasicDataSource bds = new BasicDataSource();
        bds.setUrl("jdbc:derby:memory:PaymentManagerTest;create=true");
        this.dataSource = bds;
        
        try (Connection conn = bds.getConnection()) {                 
            conn.prepareStatement("CREATE TABLE PAYMENT ("
                    + "id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "description VARCHAR(255) NOT NULL,"
                    + "date DATE NOT NULL,"
                    + "amount DECIMAL(12,2) NOT NULL)").executeUpdate();
        } 
        manager = new PaymentManagerImpl(bds);
    }
    
    @After
    public void tearDown() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.prepareStatement("DROP TABLE PAYMENT").executeUpdate();
        }
    }

    
    @Test
    public void testCreatePayment() {
        
        Payment payment = newPayment("Running shoes Nike",LocalDate.of(2015,Month.MARCH,6),new BigDecimal("130.00"));
        manager.createPayment(payment);
        
        Long paymentId = payment.getId();
        System.out.println(paymentId);
        assertNotNull(paymentId);
        Payment temp = manager.getPayment(paymentId);
        assertEquals(payment,temp);
        assertNotSame(payment,temp);
        assertDeepEquals(payment,temp);
       
    }

    @Test
    public void testGetPayment(){
    
        assertNull(manager.getPayment(1L));
        
        Payment payment = newPayment("Running shirt Adidas",LocalDate.of(2015,Month.MARCH,6),new BigDecimal("25.00"));
        manager.createPayment(payment);
        Long paymentId = payment.getId();
        
        Payment result = manager.getPayment(paymentId);
        assertEquals(payment,result);
        assertDeepEquals(payment,result);
    
    }
    
    @Test
    public void createPaymentWithWrongAttributes() {

        try {
            manager.createPayment(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        LocalDate date = LocalDate.of(2015,Month.MARCH,10);
        
        Payment payment = newPayment("Running shoes Nike",date,new BigDecimal("130.00"));
        payment.setId(1l);
        try {
            manager.createPayment(payment);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        payment = newPayment("Electricity bill",date,null); 
        try {
            manager.createPayment(payment);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        payment = newPayment(null,date,new BigDecimal("10.00"));
        try {
            manager.createPayment(payment);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

    }
    
    @Test
    public void updatePayment() {
        
        LocalDate date = LocalDate.of(2015,Month.MARCH,10);
        
        Payment p1 = newPayment("Flowers",date,new BigDecimal("5.60"));
        Payment p2 = newPayment("Chocolate",date,new BigDecimal("4.70"));
        manager.createPayment(p1);
        manager.createPayment(p2);
        
        Long paymentId = p1.getId();

        p1 = manager.getPayment(paymentId);
        p1.setAmount(new BigDecimal("6.50"));
        manager.updatePayment(p1);        
        assertEquals(new BigDecimal("6.50"), p1.getAmount());
        assertEquals("Flowers", p1.getDescription());
        assertEquals(date, p1.getDate());

        p1 = manager.getPayment(paymentId);
        date = LocalDate.of(2015,Month.JANUARY,10);
        p1.setDate(date);
        manager.updatePayment(p1);        
        assertEquals(new BigDecimal("6.50"), p1.getAmount());
        assertEquals("Flowers", p1.getDescription());
        assertEquals(date, p1.getDate());

        p1 = manager.getPayment(paymentId);
        p1.setDescription("Magazines");
        manager.updatePayment(p1);        
        assertEquals(new BigDecimal("6.50"), p1.getAmount());
        assertEquals("Magazines", p1.getDescription());
        assertEquals(date, p1.getDate());


        // Check if updates didn't affected other records
        assertDeepEquals(p2, manager.getPayment(p2.getId()));
    }
    
    @Test
    public void updatePaymentWithWrongArguments(){
        LocalDate date = LocalDate.of(2015,Month.MARCH,10);
        Payment p1 = newPayment("Books",date,new BigDecimal("7.80"));
        manager.createPayment(p1);
        Long paymentId = p1.getId();

        
        try {
            manager.updatePayment(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        try {
            p1 = manager.getPayment(paymentId);
            p1.setId(null);
            manager.updatePayment(p1);        
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        
        try {
            p1 = manager.getPayment(paymentId);
            p1.setAmount(null);
            manager.updatePayment(p1);        
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        try {
            p1 = manager.getPayment(paymentId);
            p1.setDescription(null);
            manager.updatePayment(p1);        
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
            
              
    }
    
    @Test
    public void testDeleteBody(){
        
        Payment p1 = newPayment("Wine",LocalDate.of(2015,Month.MARCH,10),new BigDecimal("3.20"));
        Payment p2 = newPayment("Milk",LocalDate.of(2015,Month.JULY,02),new BigDecimal("0.80"));
        manager.createPayment(p1);
        manager.createPayment(p2);
        
        assertNotNull(manager.getPayment(p1.getId()));
        assertNotNull(manager.getPayment(p2.getId()));
        
        manager.deletePayment(p1);
        
        assertNull(manager.getPayment(p1.getId()));
        assertNotNull(manager.getPayment(p2.getId()));  
        
        try {
            manager.deletePayment(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
    }
    
    private static Payment newPayment(String description, LocalDate date, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setDescription(description);
        payment.setDate(date);
        payment.setAmount(amount);
        return payment;
    }
    
    
    private void assertDeepEquals(Payment expected, Payment actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getDate(), actual.getDate());
        assertEquals(expected.getAmount(), actual.getAmount());
    }
    
}
