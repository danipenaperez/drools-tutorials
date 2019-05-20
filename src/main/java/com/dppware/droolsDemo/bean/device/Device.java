package com.dppware.droolsDemo.bean.device;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Device implements Serializable{
	protected String id;
	protected String status;
	
}
