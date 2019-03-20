package com.dppware.droolsDemo.services;

import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.KieSessionOption;
import org.kie.api.runtime.conf.MultiValueKieSessionOption;
import org.kie.api.runtime.conf.SingleValueKieSessionOption;
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
    	kieContainer.getKieSessionConfiguration();
    }
    
    
}
/**
use deo algoritm Rete
JSR-94 Rule Engine API.
**
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