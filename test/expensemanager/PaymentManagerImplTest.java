/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expensemanager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author charlliz
 */
public class PaymentManagerImplTest {
    
    private PaymentManagerImpl manager;
    
    @Before
    public void setUp() throws SQLException{
        manager = new PaymentManagerImpl();
    }

    
    @Test
    public void testCreatePayment() {
        System.out.println("createPayment");
        
        Payment payment = newPayment("Running shoes Nike",LocalDate.of(2015,Month.MARCH,6),new BigDecimal(130));
        manager.createPayment(payment);
        
        Long paymentId = payment.getId();
        assertNotNull(paymentId);
        Payment temp = manager.findPaymentById(paymentId);
        assertEquals(payment,temp);
        assertNotSame(payment,temp);
        assertDeepEquals(payment,temp);
       
    }

    @Test
    public void getAllContacts() {

        assertTrue(manager.findAllPayments().isEmpty());
        
        Payment g1 = newPayment("Payment 1",LocalDate.of(2015,Month.JANUARY,12),new BigDecimal(55.24));
        Payment g2 = newPayment("Payment 2",LocalDate.of(2015,Month.FEBRUARY,10),new BigDecimal(61.15));

        manager.createPayment(g1);
        manager.createPayment(g2);

        List<Payment> expected = Arrays.asList(g1,g2);
        List<Payment> actual = manager.findAllPayments();

        Collections.sort(actual,idComparator);
        Collections.sort(expected,idComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }
    
    @Test
    public void addContactWithWrongAttributes() {

        try {
            manager.createPayment(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        LocalDate date = LocalDate.of(2015,Month.MARCH,10);
        
        Payment payment = newPayment("Running shoes Nike",date,new BigDecimal(130));
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
        
        payment = newPayment("Denim Jacket",date,new BigDecimal(-20.35)); 
        try {
            manager.createPayment(payment);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        payment = newPayment("Waste fee",null,new BigDecimal(40.25));
        try {
            manager.createPayment(payment);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        payment = newPayment(null,date,new BigDecimal(10.00));
        try {
            manager.createPayment(payment);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        date = LocalDate.of(2016,Month.MARCH,10);
        
        payment = newPayment("Waste fee",date,new BigDecimal(40.25));
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
        
        Payment p1 = newPayment("Flowers",date,new BigDecimal(5.6));
        Payment p2 = newPayment("Chocolate",date,new BigDecimal(4.7));
        manager.createPayment(p1);
        manager.createPayment(p2);
        Long paymentId = p1.getId();

        p1 = manager.findPaymentById(paymentId);
        p1.setAmount(new BigDecimal(6.5));
        manager.updatePayment(p1);        
        assertEquals(new BigDecimal(6.5), p1.getAmount());
        assertEquals("Flowers", p1.getDescription());
        assertEquals(date, p1.getDate());

        p1 = manager.findPaymentById(paymentId);
        
        date = LocalDate.of(2015,Month.JANUARY,10);
        p1.setDate(date);
        manager.updatePayment(p1);        
        assertEquals(new BigDecimal(6.5), p1.getAmount());
        assertEquals("Flowers", p1.getDescription());
        assertEquals(date, p1.getDate());

        p1 = manager.findPaymentById(paymentId);
        p1.setDescription("Magazines");
        manager.updatePayment(p1);        
        assertEquals(new BigDecimal(6.5), p1.getAmount());
        assertEquals("Magazines", p1.getDescription());
        assertEquals(date, p1.getDate());


        // Check if updates didn't affected other records
        assertDeepEquals(p2, manager.findPaymentById(p2.getId()));
    }
    
    
    private static Payment newPayment(String description, LocalDate date, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setDescription(description);
        payment.setDate(date);
        payment.setAmount(amount);
        return payment;
    }
    
    private void assertDeepEquals(List<Payment> expectedList, List<Payment> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Payment expected = expectedList.get(i);
            Payment actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }
    
    private void assertDeepEquals(Payment expected, Payment actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getDate(), actual.getDate());
        assertEquals(expected.getAmount(), actual.getAmount());
    }
    
    private static final Comparator<Payment> idComparator = new Comparator<Payment>() {

        @Override
        public int compare(Payment o1, Payment o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
    
}
