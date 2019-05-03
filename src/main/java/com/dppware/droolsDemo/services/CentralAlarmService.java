package com.dppware.droolsDemo.services;

import javax.annotation.PostConstruct;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dppware.droolsDemo.bean.CentralAlarm;
import com.dppware.droolsDemo.bean.DeviceEvent;

@Service
public class CentralAlarmService {

	private CentralAlarm alarm;
	
	KieSession kieSession;
	
	@Autowired
    private KieContainer kieContainer;
	
	@PostConstruct
	private void init() {
		kieSession = kieContainer.newKieSession();
		alarm = new CentralAlarm("centralAlarm1", "-");
		kieSession.insert(alarm);
    	kieSession.fireAllRules();
	}
	/**
	 * Main drools engine Accessing
	 * @param productPrice
	 */
    public void executeRules(DeviceEvent deviceEvent) {
    	
    	//kieSession.dispose();
        System.out.println(alarm.getStatus());
    }
	public void processDeviceEvent(DeviceEvent deviceEvent) {
		kieSession.insert(deviceEvent);
		kieSession.fireAllRules();
		kieSession.dispose();
		
	}
    
}
