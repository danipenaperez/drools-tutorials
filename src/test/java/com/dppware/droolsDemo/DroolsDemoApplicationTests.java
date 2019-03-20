package com.dppware.droolsDemo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.dppware.droolsDemo.bean.ProductPrice;
import com.dppware.droolsDemo.services.PriceCalculatorService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DroolsDemoApplicationTests {
	@Autowired
	private PriceCalculatorService priceCalculatorService;
	@Test
	public void executeCalculations() {
		ProductPrice productPrice = new ProductPrice(5);//Create the Fact
		priceCalculatorService.executeRules(productPrice);//Call service and internal BlackBox rules engine
		System.out.println(productPrice);//final object state
		
	}
}
