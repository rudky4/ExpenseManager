/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pb168.expensemanager;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

    final static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws ServiceFailureException {

        log.info("zaciname");
        ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        PaymentManager paymentManager = ctx.getBean("paymentManager", PaymentManager.class);

        List<Payment> allPayments = paymentManager.findAllPayments();
        System.out.println("allPayments = " + allPayments);

    }

}