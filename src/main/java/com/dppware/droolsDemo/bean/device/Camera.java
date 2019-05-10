package com.dppware.droolsDemo.bean.device;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class Camera extends Device{

	public void startRecord() {
		this.status="recording";
	}
	public void stopRecord() {
		this.status="stop";
	}
	
	
}
