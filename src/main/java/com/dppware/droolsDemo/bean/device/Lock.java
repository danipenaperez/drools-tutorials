package com.dppware.droolsDemo.bean.device;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter @Setter @ToString @Slf4j
public class Lock extends Device{

	public void close() {
		this.status="closed";
		log.info("Cerrando la puerta...");
	}

	public void open() {
		this.status="open";
		log.info("Abriendo la puerta...");
		
	}

	public Lock(String id) {
		this.id=id;
	}
	
	public Lock(String id, String status) {
		this.id=id;
		this.status=status;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean eq=false;
		if(obj instanceof Lock) {
			Lock _o =(Lock)obj;
			if(_o.getId().equals(this.id)) {
				eq=true;
			}
		}
		return eq;
	}
	
}
