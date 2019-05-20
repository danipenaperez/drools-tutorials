package com.dppware.droolsDemo.bean;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
/**
 * Simple POJO
 * @author dpena
 *
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class DeviceEvent implements Serializable{
	
	private String name;
	
	private String value;
	
}
