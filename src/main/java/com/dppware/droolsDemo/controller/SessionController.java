package com.dppware.droolsDemo.controller;

import java.io.IOException;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dppware.droolsDemo.services.CentralAlarmService;

@RestController
@RequestMapping("session")
public class SessionController {

	@Autowired
	CentralAlarmService centralAlarmService;
	
	@PostMapping(path="stop")
	@PermitAll
	@ResponseStatus(HttpStatus.ACCEPTED)
    public void stop() throws IOException {
		centralAlarmService.stopSession();
    }
	
	@PostMapping(path="restart")
	@PermitAll
	@ResponseStatus(HttpStatus.ACCEPTED)
    public void restart() throws IOException {
		centralAlarmService.restartSession();
    }
	
}
