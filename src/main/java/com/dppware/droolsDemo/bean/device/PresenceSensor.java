package com.dppware.droolsDemo.bean.device;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString 
public class PresenceSensor extends Device{
	public PresenceSensor(String id, String status) {
		this.id=id;
		this.status=status;
	}
}
