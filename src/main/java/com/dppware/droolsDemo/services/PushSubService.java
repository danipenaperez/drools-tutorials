package com.dppware.droolsDemo.services;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PushSubService {
	/**
	 * Example dependencie
	 * @param o
	 * @throws JsonProcessingException
	 */
	//@Autowired
	//private KafkaTemplate<String, String> kafkaTemplate;
	
	public void publishNewProductCreated(Object o) throws JsonProcessingException {
		String rawJSON = new ObjectMapper().writeValueAsString(o);
		//kafkaTemplate.send("newProduct", rawJSON); ...or anything
		System.out.println("Publishing newProduct Topic , content ["+rawJSON+"]");
	}
}
