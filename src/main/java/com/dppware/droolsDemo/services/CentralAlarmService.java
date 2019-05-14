package com.dppware.droolsDemo.services;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.drools.core.ClassObjectFilter;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dppware.droolsDemo.bean.CentralAlarm;
import com.dppware.droolsDemo.bean.DeviceEvent;
import com.dppware.droolsDemo.bean.device.Device;
import com.dppware.droolsDemo.bean.device.Lock;
import com.dppware.droolsDemo.bean.device.PresenceSensor;
import com.dppware.droolsDemo.bean.event.DeviceLockEvent;
import com.dppware.droolsDemo.bean.event.PresenceSensorEvent;

@Service
public class CentralAlarmService {

	private CentralAlarm alarm;
	
	
	@Autowired
	private SMSServices smsService;
	
	KieSession kieSession;
	
	@Autowired
    private KieContainer kieContainer;
	
	@PostConstruct
	private void init() {
		
		kieSession = kieContainer.newKieSession();
		kieSession.setGlobal("smsService", smsService);//adding globals
		
		
		//Add Central Alarm
		alarm = new CentralAlarm("centralAlarm1", "-");
		kieSession.insert(alarm);
				
		//Add Presence Sensor garage
		PresenceSensor psGarage = new PresenceSensor("GarageSensor","-");
		kieSession.insert(psGarage);
		alarm.getDevices().put(psGarage.getId(), psGarage);
		
		PresenceSensor psPasillo = new PresenceSensor("PasilloSensor","-");
		kieSession.insert(psPasillo);
		alarm.getDevices().put(psPasillo.getId(), psPasillo);
		
		
		Lock lock = new Lock("EntranceLock","-");
		kieSession.insert(lock);
		alarm.getDevices().put(lock.getId(), lock);
		
    	//kieSession.fireAllRules();
    	new Thread() {
    		@Override
    		public void run() {
    			kieSession.fireUntilHalt();
    		}
    	}.start();
    	
	}

    
    
    /**
     * FIRING INPUT METHODS
     * 
     */
	
	public void processDeviceEvent(Lock deviceEvent) {
		System.out.println("hola");
		Collection<Lock> myfacts = (Collection<Lock>) kieSession.getObjects( new ClassObjectFilter(Lock.class) );
		kieSession.insert(deviceEvent);
	}
	
	public void processDeviceEvent(DeviceLockEvent deviceEvent) {
		kieSession.insert(deviceEvent);
	}
	public void processDeviceEvent(PresenceSensorEvent deviceEvent) {
		kieSession.insert(deviceEvent);
	}

	
	
	public void processDeviceStatus(Device deviceStatus) {
			kieSession.insert(deviceStatus);
	}
	
	
	
	
	/**
	 * Main drools engine Accessing
	 * @param productPrice
	 */
    public void executeRules(DeviceEvent deviceEvent) {
    	
    	//kieSession.dispose();
        System.out.println(alarm.getStatus());
    }
	public CentralAlarm getAlarm() {
		return alarm;
	}
	
	public Device getDeviceById(String id) {
		return alarm.getDevices().get(id);
	}
    
}
