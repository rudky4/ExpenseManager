/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pb168.expensemanager;

import java.util.List;

/**
 *
 * @author Andrea Turiaková
 */
public interface PaymentManager {
 

    /**
     * 
     * @param payment
     * @throws ServiceFailureException 
     */
    public void createPayment(Payment payment) throws ServiceFailureException;
    
    /**
     * 
     * @param payment
     * @throws ServiceFailureException 
     */
    public void updatePayment(Payment payment) throws ServiceFailureException;
    
    /**
     * 
     * @param payment
     * @throws ServiceFailureException 
     */
    public void deletePayment(Payment payment) throws ServiceFailureException;
    
    /**
     * 
     * @param id
     * @return
     * @throws ServiceFailureException 
     */
    public Payment findPaymentById(Long id)  throws ServiceFailureException;
    
    /**
     * 
     * @return
     * @throws ServiceFailureException 
     */
    public List<Payment> findAllPayments()  throws ServiceFailureException;
    
    
}
