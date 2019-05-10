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
import com.dppware.droolsDemo.bean.device.Device;
import com.dppware.droolsDemo.bean.device.Lock;
import com.dppware.droolsDemo.services.CentralAlarmService;

@RestController
@RequestMapping("events")
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
	@PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE)
	@PermitAll
	@ResponseStatus(HttpStatus.NO_CONTENT)
    public void receiveEvent(@RequestBody DeviceEvent deviceEvent) {
		Device dev = centralAlarmService.getDeviceById(deviceEvent.getName());
		dev.setStatus(deviceEvent.getValue());
		centralAlarmService.processDeviceEvent((Lock)dev);
		
		//centralAlarmService.processDeviceEvent(new Lock("EntranceLock", "open"));
    }
	
	
	
	
	@PostMapping(path="/lock/{id}",consumes=MediaType.APPLICATION_JSON_VALUE)
	@PermitAll
	@ResponseStatus(HttpStatus.NO_CONTENT)
    public void receiveLockEvent(@PathVariable("id")String deviceId, @RequestBody DeviceEvent deviceEvent) {
		Device dev = centralAlarmService.getDeviceById(deviceId);
		dev.setStatus(deviceEvent.getValue());
		centralAlarmService.processDeviceEvent((Lock)dev);
		
		centralAlarmService.processDeviceEvent(new Lock("EntranceLock", "open"));
    }
    
}
