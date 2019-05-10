package com.dppware.droolsDemo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.dppware.droolsDemo.bean.DeviceEvent;
import com.dppware.droolsDemo.services.CentralAlarmService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DroolsDemoApplicationTests {
	@Autowired
	private CentralAlarmService centralAlarmService;
	@Test
	public void executeCalculations() {
		DeviceEvent deviceEvent = new DeviceEvent("DoorEntrance", "1");//detect door open
		centralAlarmService.executeRules(deviceEvent);//Call service and internal BlackBox rules engine
		System.out.println(deviceEvent);//final object state
		
	}
}
