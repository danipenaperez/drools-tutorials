package com.dppware.droolsDemo.services;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dppware.droolsDemo.bean.CentralAlarm;
import com.dppware.droolsDemo.bean.DeviceEvent;
import com.dppware.droolsDemo.bean.device.Device;
import com.dppware.droolsDemo.bean.device.Lock;
import com.dppware.droolsDemo.bean.device.PresenceSensor;

import ch.qos.logback.core.net.SyslogOutputStream;

@Service
public class CentralAlarmService {

	private CentralAlarm alarm;
	
	private Map<String, Device> devices = new HashMap<String, Device>();
	
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
		devices.put(psGarage.getId(), psGarage);
		
		PresenceSensor psPasillo = new PresenceSensor("PasilloSensor","-");
		kieSession.insert(psPasillo);
		devices.put(psPasillo.getId(), psPasillo);
		
		
		Lock lock = new Lock("EntranceLock","-");
		kieSession.insert(lock);
		devices.put(lock.getId(), lock);
		
    	//kieSession.fireAllRules();
    	new Thread() {
    		@Override
    		public void run() {
    			kieSession.fireUntilHalt();
    		}
    	}.start();;
    	
	}
	/**
	 * Main drools engine Accessing
	 * @param productPrice
	 */
    public void executeRules(DeviceEvent deviceEvent) {
    	
    	//kieSession.dispose();
        System.out.println(alarm.getStatus());
    }
	public void processDeviceEvent(Device deviceEvent) {
		kieSession.insert(deviceEvent);
		//kieSession.fireAllRules();
		//kieSession.dispose();//borrar esto porque mata la session
		
	}
	public CentralAlarm getAlarm() {
		return alarm;
	}
	
	public Device getDeviceById(String id) {
		return devices.get(id);
	}
    
}
