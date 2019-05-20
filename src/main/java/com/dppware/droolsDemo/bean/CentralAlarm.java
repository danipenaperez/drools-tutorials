package com.dppware.droolsDemo.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.dppware.droolsDemo.bean.device.Device;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @NoArgsConstructor @ToString @AllArgsConstructor
public class CentralAlarm implements Serializable{
	public CentralAlarm(String name, String status) {
		this.name=name;
		this.status=status;
	}

	private String name;
	/**
	 * ready or Alarm!!
	 */
	private String status;
	
	Map<String, Device> devices = new HashMap<String, Device>();
}
