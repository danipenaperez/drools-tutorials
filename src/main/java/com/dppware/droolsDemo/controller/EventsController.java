package com.dppware.droolsDemo.controller;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dppware.droolsDemo.bean.DeviceEvent;
import com.dppware.droolsDemo.services.CentralAlarmService;

@RestController
@RequestMapping("events")
public class EventsController {
	
	@Autowired
	CentralAlarmService centralAlarmService;
	
	@GetMapping
	public DeviceEvent getSample() {
		return new DeviceEvent("DoorEntrance", 0);
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
		centralAlarmService.processDeviceEvent(deviceEvent);
    }
    
}
