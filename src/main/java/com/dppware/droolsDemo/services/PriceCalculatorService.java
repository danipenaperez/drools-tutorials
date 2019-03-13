package com.dppware.droolsDemo.services;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dppware.droolsDemo.bean.ProductPrice;

@Service
public class PriceCalculatorService {
	
	@Autowired
    private KieContainer kieContainer;
	
	@Autowired
	private PushSubService pushSubService;
	/**
	 * Main drools engine Accessing
	 * @param productPrice
	 */
    public void executeRules(ProductPrice productPrice) {
    	KieSession kieSession = kieContainer.newKieSession();
    	kieSession.setGlobal("publishTool", pushSubService);//adding globals
    	kieSession.insert(productPrice);
        kieSession.fireAllRules();
        kieSession.dispose();
    }
    
    
    /**
     * Access to KIE container
     * @param p
     */
    public void trace(Object p) {
    	kieContainer.getKieBase().getKiePackage("com.demo.product").getFactTypes(); 
    	kieContainer.getKieBase().getKiePackage("com.demo.product").getFunctionNames();
    	kieContainer.getKieBase().getKiePackage("com.demo.product").getGlobalVariables();
    	kieContainer.getKieBase().getKiePackage("com.demo.product").getRules();
    	
    }
    
    
}



/**

@Autowired
private KieContainer kieContainer;

public int calculateFare(ProductPrice productPrice) {
    KieSession kieSession = kieContainer.newKieSession();
    kieSession.setGlobal("productPrice", productPrice);
    kieSession.setGlobal("productPriceService", this);
    kieSession.insert(productPrice);
    kieSession.fireAllRules();
    kieSession.dispose();
    return productPrice.getBasePrice();
}



public void trace(Object p) {
	System.out.println(p+ " SERVICE INTERNAL EXECUTION "+kieContainer.getReleaseId());
}
**/