package com.example.demogemfire;

import com.example.demogemfire.model.Person;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.RegionFactoryBean;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.*;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;

import static java.util.Arrays.asList;
import static java.util.stream.StreamSupport.stream;



@SpringBootApplication
@EnableEntityDefinedRegions(basePackages = {"com.example.demogemfire.model"},
		clientRegionShortcut = ClientRegionShortcut.CACHING_PROXY)
@EnableGemfireRepositories
public class DemoGemfireApplication {


	public static void main(String[] args) {
		SpringApplication.run(DemoGemfireApplication.class, args);
	}

	@Bean
	public RegionConfigurer regionConfigurer(){
		return new RegionConfigurer() {
			@Override
			public void configure(String beanName, ClientRegionFactoryBean<?, ?> bean) {
				if(beanName.equals("Person")){
					//bean.setCacheListeners(...);
				}
			}
		};
	}

	@Bean
	public ClientCacheConfigurer cacheConfigurer(){
		return new ClientCacheConfigurer() {
			@Override
			public void configure(String s, ClientCacheFactoryBean clientCacheFactoryBean) {
				//customize the cache
				clientCacheFactoryBean.setSubscriptionEnabled(false);
			}
		};
	}

	@Bean
	ApplicationRunner run(PersonRepository personRepository) {

		return args -> {

			Person alice = new Person("Alice", 40);
			Person bob = new Person("Baby Bob", 1);
			Person carol = new Person("Teen Carol", 13);

			System.out.println("Before accessing data in GemFire...");

			asList(alice, bob, carol).forEach(person -> System.out.println("\t" + person));

			System.out.println("Saving Alice, Bob and Carol to GemFire...");

			personRepository.save(alice);
			personRepository.save(bob);
			personRepository.save(carol);

			System.out.println("Lookup each person by name...");

			asList(alice.getName(), bob.getName(), carol.getName())
					.forEach(name -> System.out.println("\t" + personRepository.findByName(name)));

			System.out.println("Query adults (over 18):");

			stream(personRepository.findByAgeGreaterThan(18).spliterator(), false)
					.forEach(person -> System.out.println("\t" + person));

			System.out.println("Query babies (less than 5):");

			stream(personRepository.findByAgeLessThan(5).spliterator(), false)
					.forEach(person -> System.out.println("\t" + person));

			System.out.println("Query teens (between 12 and 20):");

			stream(personRepository.findByAgeGreaterThanAndAgeLessThan(12, 20).spliterator(), false)
					.forEach(person -> System.out.println("\t" + person));
		};
	}



}
