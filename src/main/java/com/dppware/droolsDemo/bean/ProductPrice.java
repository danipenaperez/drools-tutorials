package com.dppware.droolsDemo.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
/**
 * Simple POJO
 * @author dpena
 *
 */
@Getter @Setter @NoArgsConstructor @ToString
public class ProductPrice {
	
	private Integer basePrice;
	
	public ProductPrice(Integer basePrice) {
		this.basePrice=basePrice;
	}
	
}
