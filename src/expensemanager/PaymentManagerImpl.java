package expensemanager;

import java.util.List;

/**
 *
 * @author Andrea Turiaková
 */
public class PaymentManagerImpl implements PaymentManager{

    @Override
    public void createPayment(Payment payment) throws ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void updatePayment(Payment payment) throws ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void deletePayment(Payment payment) throws ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Payment findPaymentById(Long id) throws ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public List<Payment> findAllPayments() throws ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
}
