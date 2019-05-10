package com.dppware.droolsDemo.services;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class SMSServices {
	
	public void sendSMS(String text) {
		//calling twilio with message...
		log.info("Sended Email content[ {} ]", text);
	}
}
