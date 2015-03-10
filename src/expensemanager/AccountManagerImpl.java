/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package expensemanager;

import java.util.List;

/**
 *
 * @author rk
 */
public class AccountManagerImpl implements AccountManager{
    
    @Override
    public void createAccount(Account account) throws ServiceFailureException{
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void updateAccount(Account account) throws ServiceFailureException{
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void deleteAccount(Account account) throws ServiceFailureException{
        throw new UnsupportedOperationException("Not supported yet.");
    }  
    
    @Override
    public Account getAccount(Long id) throws ServiceFailureException{
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public List<Account> findAllAccounts() throws ServiceFailureException{
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
