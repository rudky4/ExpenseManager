/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pb168.expensemanager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import java.sql.ResultSet;

/**
 *
 * @author rk
 */
public class ExpenseManagerImpl implements ExpenseManager {
    
    private static final Logger logger = Logger.getLogger(AccountManagerImpl.class.getName());

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }    

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }
    
    @Override
    public void addPaymentToAccount(Payment payment, Account account) throws ServiceFailureException{
                    
        checkDataSource();
                               
        if (payment == null) {
            throw new IllegalArgumentException("payment is null");
        }   
        
        if (payment.getId() == null) {
            throw new IllegalEntityException("payment id is null");
        }       
        
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }   
        
        if (account.getId() == null) {
            throw new IllegalEntityException("account id is null");
        }
        
        Connection conn = null;
        PreparedStatement updateSt = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
                        
            updateSt = conn.prepareStatement(
                    "UPDATE PAYMENT SET accountId = ? WHERE id = ? AND accountId IS NULL");
            updateSt.setLong(1, account.getId());
            updateSt.setLong(2, payment.getId());
            int count = updateSt.executeUpdate();
            if (count == 0) {
                throw new IllegalEntityException("Payment " + payment + " not found or is already in other account");
            }
            DBUtils.checkUpdatesCount(count, payment, false);            
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when putting payment to account";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, updateSt);
        }
    }
    
    @Override
    public List<Payment> getAllPaymentsInAccount(Account account) throws ServiceFailureException{
        checkDataSource();                                   
        
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }        
        if (account.getId() == null) {
            throw new IllegalEntityException("account id is null");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT PAYMENT.ID, PAYMENT.DESCRIPTION, PAYMENT.DATE, PAYMENT.AMOUNT " +
                    "FROM PAYMENT JOIN ACCOUNT ON ACCOUNT.ID = PAYMENT.ACCOUNTID " +
                    "WHERE ACCOUNT.ID = ?");
            st.setLong(1, account.getId());
            return PaymentManagerImpl.executeQueryForMultiplePayments(st);
        } catch (SQLException ex) {
            String msg = "Error when trying to get payments from " + account;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }
    
    @Override
    public List<Payment> getAllPaymentsForPeriod(Account account, LocalDate from, LocalDate to) throws ServiceFailureException{
         checkDataSource();                                   
        
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }        
        if (account.getId() == null) {
            throw new IllegalEntityException("account id is null");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT PAYMENT.ID, DESCRIPTION, DATE, AMOUNT " +
                    "FROM PAYMENT JOIN ACCOUNT ON ACCOUNT.ID = PAYMENT.ACCOUNTID " +
                    "WHERE ACCOUNT.ID = ? AND PAYMENT.DATE >= ? AND PAYMENT.DATE <= ?");
            st.setLong(1, account.getId());
            st.setDate(2, java.sql.Date.valueOf(from));
            st.setDate(3, java.sql.Date.valueOf(to));
            return PaymentManagerImpl.executeQueryForMultiplePayments(st);
        } catch (SQLException ex) {
            String msg = "Error when trying to get payments from " + account;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }
    
    @Override
    public void removePaymentFromAccount(Payment payment, Account account) throws ServiceFailureException{
         checkDataSource();
         
        if (payment == null) {
            throw new IllegalArgumentException("payment is null");
        }   
        
        if (payment.getId() == null) {
            throw new IllegalEntityException("payment id is null");
        }       
        
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }   
        
        if (account.getId() == null) {
            throw new IllegalEntityException("account id is null");
        }                            
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "UPDATE PAYMENT SET ACCOUNTID = NULL WHERE ID = ? AND ACCOUNTID = ?");
            st.setLong(1, payment.getId());
            st.setLong(2, account.getId());
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, payment, false);            
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when putting payment to account";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }    
    
    @Override
    public BigDecimal getAccountBalance(Account account) throws ServiceFailureException, SQLException{
       
        PreparedStatement checkSt = null;
        BigDecimal result = new BigDecimal("0.0");
         
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }   
        
        if (account.getId() == null) {
            throw new IllegalEntityException("account id is null");
        }                            
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            checkSt = conn.prepareStatement(
                    "SELECT AMOUNT FROM PAYMENT WHERE PAYMENT.ACCOUNTID = ?");
            checkSt.setLong(1, account.getId());
            ResultSet rs = checkSt.executeQuery();
            
            while (rs.next()) {
                result = result.add(rs.getBigDecimal("amount"));
            }
        } catch (SQLException ex) {
            String msg = "Error when calculating total amount";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);              
        } finally {
            DBUtils.closeQuietly(null, checkSt);
        }
        
        return result;
    }
    
    @Override
    public BigDecimal getAccountBalanceForPeriod(Account account, LocalDate from, LocalDate to) throws ServiceFailureException, SQLException{
        PreparedStatement checkSt = null;
        BigDecimal result = new BigDecimal("0.0");
         
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }   
        
        if (account.getId() == null) {
            throw new IllegalEntityException("account id is null");
        }                            
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            checkSt = conn.prepareStatement(
                    "SELECT AMOUNT FROM PAYMENT WHERE PAYMENT.ACCOUNTID = ? " +
                    "AND PAYMENT.DATE >= ? AND PAYMENT.DATE <= ?");
            checkSt.setLong(1, account.getId());
            checkSt.setDate(2, java.sql.Date.valueOf(from));
            checkSt.setDate(3, java.sql.Date.valueOf(to));
            ResultSet rs = checkSt.executeQuery();
            
            while (rs.next()) {
                result = result.add(rs.getBigDecimal("amount"));
            }
            
        } catch (SQLException ex) {
            String msg = "Error when calculating total amount for specific period.";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);   
        } finally {
            DBUtils.closeQuietly(null, checkSt);
        }
        
        return result;
    }
}
