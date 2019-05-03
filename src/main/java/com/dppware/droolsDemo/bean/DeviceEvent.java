package com.dppware.droolsDemo.bean;

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
public class DeviceEvent {
	
	private String name;
	
	private Integer measure;
	
}
