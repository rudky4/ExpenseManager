/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pb168.expensemanager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author rk
 */
public interface ExpenseManager {    
    
    void addPaymentToAccount(Payment payment, Account account) throws ServiceFailureException;
    
    List<Payment> getAllPaymentsInAccount(Account account) throws ServiceFailureException;
    
    List<Payment> getAllPaymentsForPeriod(Account account, LocalDate from, LocalDate to) throws ServiceFailureException;
    
    void removePaymentFromAccount(Payment payment, Account account) throws ServiceFailureException;
    
    BigDecimal getAccountBalance(Account account) throws ServiceFailureException, SQLException;
    
    BigDecimal getAccountBalanceForPeriod(Account account, LocalDate from, LocalDate to) throws ServiceFailureException, SQLException;
}
