package com.example.demoserver;

import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.pdx.PdxSerializer;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.apache.geode.cache.Cache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.ReplicatedRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.config.annotation.EnablePdx;

@SpringBootApplication
@CacheServerApplication(name = "SpringBootGemFireServer")
@EnableLocator
@EnableManager
public class DemoServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoServerApplication.class, args);
	}




	@Bean(name = "Person")
	ReplicatedRegionFactoryBean personRegion(Cache gemfireCache
												 ) {

		ReplicatedRegionFactoryBean customers = new ReplicatedRegionFactoryBean<>();

		//factorials.setAttributes(factorialRegionAttributes);
		customers.setCache(gemfireCache);
		customers.setClose(false);
		customers.setPersistent(false);

		return customers;

	}
}
