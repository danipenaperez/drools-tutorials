package com.dppware.droolsDemo.services;

import org.springframework.stereotype.Service;

@Service
public class CentralAlarmServiceStateless {
//
//	private CentralAlarm alarm;
//	
//	
//	@Autowired
//	private SMSServices smsService;
//	
//	
//	KieSession kieStatelessSession;
//	
//	@Autowired
//    private KieContainer kieContainer;
//	
//	@PostConstruct
//	private void initStateless() {
//		
//		StatelessKieSession kieStatelessSession = kieContainer.newStatelessKieSession();
//		kieStatelessSession.setGlobal("smsService", smsService);//adding globals
//		
//		
//		//Add Central Alarm
//		alarm = new CentralAlarm("centralAlarm", "-");
//		kieStatelessSession..insert(alarm);
//				
//		//Add Presence Sensor garage
//		PresenceSensor psGarage = new PresenceSensor("GarageSensor","-");
//		kieStatelessSession.insert(psGarage);
//		alarm.getDevices().put(psGarage.getId(), psGarage);
//		
//		PresenceSensor psPasillo = new PresenceSensor("PasilloSensor","-");
//		kieStatelessSession.insert(psPasillo);
//		alarm.getDevices().put(psPasillo.getId(), psPasillo);
//		
//		
//		Lock lock = new Lock("EntranceLock","-");
//		kieStatefullSession.insert(lock);
//		alarm.getDevices().put(lock.getId(), lock);
//		
//    	//kieSession.fireAllRules();
//    	new Thread() {
//    		@Override
//    		public void run() {
//    			kieStatefullSession.fireUntilHalt();
//    		}
//    	}.start();
//    	
//	}
//
//	
//	
//
//    
//    
//    /**
//     * FIRING INPUT METHODS
//     * 
//     */
//	
//	public void processDeviceEvent(Lock deviceEvent) {
//		System.out.println("hola");
//		System.out.println("ANTES DE ENTRAR hay "+kieStatefullSession.getFactCount());
//		Collection<Lock> myfacts = (Collection<Lock>) kieStatefullSession.getObjects( new ClassObjectFilter(Lock.class) );
//		kieStatefullSession.insert(deviceEvent);
//		System.out.println("Al SALIR HAY hay "+kieStatefullSession.getFactCount());
//		
//	}
//	
//	public void processDeviceEvent(DeviceLockEvent deviceEvent) {
//		kieStatefullSession.insert(deviceEvent);
//	}
//	public void processDeviceEvent(PresenceSensorEvent deviceEvent) {
//		kieStatefullSession.insert(deviceEvent);
//	}
//
//	
//	
//	public void processDeviceStatus(Device deviceStatus) {
//			kieStatefullSession.insert(deviceStatus);
//	}
//	
//	
//	
//	
//	/**
//	 * Main drools engine Accessing
//	 * @param productPrice
//	 */
//    public void executeRules(DeviceEvent deviceEvent) {
//    	
//    	//kieSession.dispose();
//        System.out.println(alarm.getStatus());
//    }
//	public CentralAlarm getAlarm() {
//		return alarm;
//	}
//	
//	public Device getDeviceById(String id) {
//		return alarm.getDevices().get(id);
//	}
//	
//	private byte[] toStore;
//	
//	
//	/***
//	 * SERIALIZE MARSHALLING
//	 * @throws IOException 
//	 */
//	public void stopSession() throws IOException {
//		KIESessionUtils.save(kieStatefullSession, new File("./sessionserialized"));
////		ByteArrayOutputStream baos = new ByteArrayOutputStream();
////		Marshaller marshaller = KieServices.Factory.get().getMarshallers().newMarshaller( kieSession.getKieBase() );
////		marshaller.marshall( baos, kieSession );
////		toStore = baos.toByteArray();
////		baos.close();
//	}
//	
//	public void restartSession() {
//		System.out.println(toStore);
//	
//		KieSession ksession = KIESessionUtils.load(new File("./sessionserialized"));
//		//kieContainer.getKieBase().getKieSessions().add(ksession);
//		
////		KieServices kieServices = KieServices.Factory.get();
////		Integer kieSessionId = kieSession.getId();
////		
////		KieSession ksession = kieServices.getStoreServices().loadKieSession( kieSessionId, kieSession.getKieBase(), null, kieSession.getEnvironment() );
//	}
//	
	
    
}
