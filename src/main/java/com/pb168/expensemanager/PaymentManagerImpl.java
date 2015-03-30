package com.pb168.expensemanager;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrea Turiaková
 */
public class PaymentManagerImpl implements PaymentManager{

    
    final static Logger log = LoggerFactory.getLogger(AccountManagerImpl.class);

    private final DataSource dataSource;

    public PaymentManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    

    @Override
    public void createPayment(Payment payment) throws ServiceFailureException {
        log.debug("createPayment({})",payment);
        validate(payment);
        if(payment.getId() != null) throw new IllegalArgumentException("payment id is already set");
        
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO Payment (description,date,amount) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, payment.getDescription() );
                if(payment.getDate() != null){
                    st.setDate(2, java.sql.Date.valueOf(payment.getDate()));
                }else{
                    st.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                }
                st.setBigDecimal(3, payment.getAmount());
           
                st.executeUpdate();
                
                int addedRows = st.executeUpdate();
                if (addedRows != 1) {
                    throw new ServiceFailureException("Internal Error: More rows inserted when trying to insert payment " + payment);
                }
                ResultSet keyRS = st.getGeneratedKeys();
                payment.setId(getKey(keyRS, payment));        
            }
            
        } catch (SQLException ex) {
            log.error("cannot insert payment", ex);
            throw new ServiceFailureException("database insert failed", ex);
        }
             
    }

    @Override
    public void updatePayment(Payment payment) throws ServiceFailureException {
        log.debug("updatePayment({})", payment);
        validate(payment);
        if (payment.getId() == null) throw new IllegalArgumentException("payment id is null");
        
        try (Connection conn = dataSource.getConnection()) {
            try(PreparedStatement st = conn.prepareStatement("UPDATE Payment SET description=?,date=?,amount=? WHERE id=?")) {
                st.setString(1, payment.getDescription() );
                if(payment.getDate() != null){
                    st.setDate(2, java.sql.Date.valueOf(payment.getDate()));
                }else{
                    st.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                }
                st.setBigDecimal(3, payment.getAmount());
                st.setLong(4, payment.getId());
                
                if(st.executeUpdate()!=1) {
                    throw new IllegalArgumentException("cannot update payment "+payment);
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when updating payment", ex);
        }
    }

    @Override
    public void deletePayment(Payment payment) throws ServiceFailureException {
        log.debug("deletePayment({})", payment);
        if (payment == null) throw new IllegalArgumentException("payment is null");
        
        try (Connection conn = dataSource.getConnection()) {
            try(PreparedStatement st = conn.prepareStatement("DELETE FROM Payment WHERE id=?")) {
                st.setLong(1,payment.getId());
                if(st.executeUpdate()!=1) {
                    throw new ServiceFailureException("did not delete Payment with id ="+payment.getId());
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when deleting payment", ex);
        }
    }

    @Override
    public Payment getPayment(Long id) throws ServiceFailureException {
        log.debug("getPayment({})", id);
        if (id == null) throw new IllegalArgumentException("id is null");
        
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id, description, date, amount FROM Payment WHERE id = ?")) {
                st.setLong(1, id);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    Payment payment = resultSetToPayment(rs);
                    if (rs.next()) {
                        throw new ServiceFailureException(
                                "Internal error: More entities with the same id found "
                                        + "(source id: " + id + ", found " + payment + " and " + resultSetToPayment(rs));
                    }
                    return payment;
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving payment", ex);
        }
    }
    
    private static Payment resultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getLong("id"));
        payment.setDescription(rs.getString("description")); 
        payment.setDate(rs.getDate("date").toLocalDate());
        payment.setAmount(rs.getBigDecimal("amount"));
        return payment;
    }
    

    @Override
    public List<Payment> findAllPayments() throws ServiceFailureException {
        log.debug("findAllPayments()");
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,description,date,amount FROM payment")) {
                ResultSet rs = st.executeQuery();
                List<Payment> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToPayment(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all payment", ex);
        }
    }

    static private void validate(Payment payment) {
        if (payment == null) throw new IllegalArgumentException("payment is null");
        if (payment.getDescription() == null) throw new IllegalArgumentException("description is null");
        if (payment.getAmount() == null) throw new IllegalArgumentException("amount is null");
    }

    private Long getKey(ResultSet keyRS, Payment payment) throws ServiceFailureException, SQLException{
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert payment " + payment
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert payment " + payment
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert payment " + payment
                    + " - no key found");
        }
    }
    
    static List<Payment> executeQueryForMultiplePayments(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Payment> result = new ArrayList<Payment>();
        while (rs.next()) {
            result.add(resultSetToPayment(rs));
        }
        return result;
    }
    
    
}
