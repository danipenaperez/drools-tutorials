package com.dppware.droolsDemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dppware.droolsDemo.bean.CentralAlarm;
import com.dppware.droolsDemo.services.CentralAlarmService;

@RestController
@RequestMapping("alarm")
public class AlarmController {
	
	@Autowired
	CentralAlarmService centralAlarm;
	
	@GetMapping
	public CentralAlarm getAlarm() {
		return centralAlarm.getAlarm();
    }

}
