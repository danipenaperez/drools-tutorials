package com.dppware.droolsDemo.config.rules;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BPMConfigurations {
	//private static final String[] drlFiles = { "rules/discountRules.drl"};
	
	private static final String[] drlFiles = { 	"rules/product_beans.drl", 
												"rules/product_dependencies.drl",
												"rules/product_functions.drl",
												"rules/product_rules.drl"
											};
	
	
	@Bean
	public KieContainer kieContainer() {
		KieServices kieServices = KieServices.Factory.get(); 
		//Load Rules and Ecosystem Definitions
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		for (String ruleFile : drlFiles) {
			kieFileSystem.write(ResourceFactory.newClassPathResource(ruleFile));
		}
		//Generate Modules and all internal Structures
		KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
		kieBuilder.buildAll();

		KieModule kieModule = kieBuilder.getKieModule();

		return kieServices.newKieContainer(kieModule.getReleaseId());
	}
}
