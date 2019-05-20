package com.dppware.droolsDemo.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.drools.core.ClassObjectFilter;
import org.kie.api.KieServices;
import org.kie.api.marshalling.Marshaller;
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
import com.dppware.droolsDemo.utils.KIESessionUtils;

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
		System.out.println("ANTES DE ENTRAR hay "+kieSession.getFactCount());
		Collection<Lock> myfacts = (Collection<Lock>) kieSession.getObjects( new ClassObjectFilter(Lock.class) );
		kieSession.insert(deviceEvent);
		System.out.println("Al SALIR HAY hay "+kieSession.getFactCount());
		
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
	
	private byte[] toStore;
	
	
	/***
	 * SERIALIZE MARSHALLING
	 * @throws IOException 
	 */
	public void stopSession() throws IOException {
		KIESessionUtils.save(kieSession, new File("./sessionserialized"));
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		Marshaller marshaller = KieServices.Factory.get().getMarshallers().newMarshaller( kieSession.getKieBase() );
//		marshaller.marshall( baos, kieSession );
//		toStore = baos.toByteArray();
//		baos.close();
	}
	
	public void restartSession() {
		System.out.println(toStore);
	
		KieSession ksession = KIESessionUtils.load(new File("./sessionserialized"));
		//kieContainer.getKieBase().getKieSessions().add(ksession);
		
//		KieServices kieServices = KieServices.Factory.get();
//		Integer kieSessionId = kieSession.getId();
//		
//		KieSession ksession = kieServices.getStoreServices().loadKieSession( kieSessionId, kieSession.getKieBase(), null, kieSession.getEnvironment() );
	}
	
	
    
}
