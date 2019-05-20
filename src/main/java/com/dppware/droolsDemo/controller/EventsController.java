package com.dppware.droolsDemo.controller;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dppware.droolsDemo.bean.DeviceEvent;
import com.dppware.droolsDemo.bean.device.Lock;
import com.dppware.droolsDemo.bean.event.DeviceLockEvent;
import com.dppware.droolsDemo.bean.event.PresenceSensorEvent;
import com.dppware.droolsDemo.services.CentralAlarmService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("events")
@Slf4j
public class EventsController {
	
	@Autowired
	CentralAlarmService centralAlarmService;
	
	@GetMapping
	public DeviceEvent getSample() {
		return new DeviceEvent("EntranceLock", "closed");
    }
	
	/**
	@PostMapping
	@PermitAll
	@ResponseStatus(HttpStatus.NO_CONTENT)
    public void receiveEvent() {
		centralAlarmService.processDeviceEvent(new DeviceEvent("DoorEntrance", 0));
    }
	**/
	@PostMapping(path="/presence/{id}",consumes=MediaType.APPLICATION_JSON_VALUE)
	@PermitAll
	@ResponseStatus(HttpStatus.NO_CONTENT)
    public void receivePresenceEvent(@RequestBody PresenceSensorEvent presenceSensorEvent) {
		centralAlarmService.processDeviceEvent(presenceSensorEvent);

    }
	
	
	
	
	@PostMapping(path="/lock/{id}",consumes=MediaType.APPLICATION_JSON_VALUE)
	@PermitAll
	@ResponseStatus(HttpStatus.NO_CONTENT)
    public void receiveLockEvent(@PathVariable("id")String deviceId, @RequestBody Lock deviceLockEvent) {
		
		centralAlarmService.processDeviceEvent(deviceLockEvent);
    }
	
	
	@PostMapping(path="/lockEvent/{id}",consumes=MediaType.APPLICATION_JSON_VALUE)
	@PermitAll
	@ResponseStatus(HttpStatus.NO_CONTENT)
    public void receiveLockEvent(@PathVariable("id")String deviceId, @RequestBody DeviceLockEvent deviceLockEvent) {
		
		centralAlarmService.processDeviceEvent(deviceLockEvent);
    }
    
}

/**
 * 
 * 
        ksession = SerializationHelper.getSerialisedStatefulKnowledgeSession(ksession, true);
        int fireAllRules = ksession.fireAllRules();
        Assert.assertEquals(0, fireAllRules);
        
        **/
