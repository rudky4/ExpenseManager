/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paymentmanager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
        SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd"); 
        String date = "2015-03-09";
        
        Date newDate = null;
        try{
            newDate = dateFormat.parse(date);
        }catch (ParseException e) { 
            System.out.println("Unparseable using " + dateFormat); 
        }
        
        Payment payment = newPayment("Running shoes Nike",newDate,new BigDecimal(130));
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

        
        SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd"); 
        String date_1 = "2015-03-05";
        String date_2 = "2015-03-06";
        
        Date newDate_1 = null;
        Date newDate_2 = null;
        try{
            newDate_1 = dateFormat.parse(date_1);
            newDate_2 = dateFormat.parse(date_2);
        }catch (ParseException e) { 
            System.out.println("Unparseable using " + dateFormat); 
        }
        
        
        Payment g1 = newPayment("Payment 1",newDate_1,new BigDecimal(55.24));
        Payment g2 = newPayment("Payment 2",newDate_2,new BigDecimal(61.15));

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

        SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd"); 
        String date = "2015-03-09";
        
        Date newDate = null;
        try{
            newDate = dateFormat.parse(date);
        }catch (ParseException e) { 
            System.out.println("Unparseable using " + dateFormat); 
        }
        
        Payment payment = newPayment("Running shoes Nike",newDate,new BigDecimal(130));
        payment.setId(1l);
        try {
            manager.createPayment(payment);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        payment = newPayment("Electricity bill",newDate,null); 
        try {
            manager.createPayment(payment);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        payment = newPayment("Denim Jacket",newDate,new BigDecimal(-20.35)); 
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

        payment = newPayment(null,newDate,new BigDecimal(10.00));
        try {
            manager.createPayment(payment);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        
        try{
            newDate = dateFormat.parse("2016-01-01");
        }catch (ParseException e) { 
            System.out.println("Unparseable using " + dateFormat); 
        }
        
        payment = newPayment("Waste fee",newDate,new BigDecimal(40.25));
        try {
            manager.createPayment(payment);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

    }
    
    @Test
    public void updatePayment() {
        SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd"); 
        String date = "2015-03-10";
        Date newDate = null;
        try{
            newDate = dateFormat.parse(date);
        }catch (ParseException e) { 
            System.out.println("Unparseable using " + dateFormat); 
        }
        
        Payment p1 = newPayment("Flowers",newDate,new BigDecimal(5.6));
        Payment p2 = newPayment("Chocolate",newDate,new BigDecimal(4.7));
        manager.createPayment(p1);
        manager.createPayment(p2);
        Long paymentId = p1.getId();

        p1 = manager.findPaymentById(paymentId);
        p1.setAmount(new BigDecimal(6.5));
        manager.updatePayment(p1);        
        assertEquals(new BigDecimal(6.5), p1.getAmount());
        assertEquals("Flowers", p1.getDescription());
        assertEquals(newDate, p1.getDate());

        p1 = manager.findPaymentById(paymentId);
        try{
            newDate = dateFormat.parse("2015-02-15");
        }catch (ParseException e) { 
            System.out.println("Unparseable using " + dateFormat); 
        }
        p1.setDate(newDate);
        manager.updatePayment(p1);        
        assertEquals(new BigDecimal(6.5), p1.getAmount());
        assertEquals("Flowers", p1.getDescription());
        assertEquals(newDate, p1.getDate());

        p1 = manager.findPaymentById(paymentId);
        p1.setDescription("Magazines");
        manager.updatePayment(p1);        
        assertEquals(new BigDecimal(6.5), p1.getAmount());
        assertEquals("Magazines", p1.getDescription());
        assertEquals(newDate, p1.getDate());


        // Check if updates didn't affected other records
        assertDeepEquals(p2, manager.findPaymentById(p2.getId()));
    }
    
    
    private static Payment newPayment(String description, Date date, BigDecimal amount) {
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
