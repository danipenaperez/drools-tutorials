package com.dppware.droolsDemo.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @NoArgsConstructor @ToString @AllArgsConstructor
public class CentralAlarm {
	private String name;
	/**
	 * ready or Alarm!!
	 */
	private String status;
}
