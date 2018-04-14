package com.example.demogemfire.config;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.mapping.MappingPdxSerializer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

@Configuration
public class CloudCacheConfig {

   @Bean
   ClientCache clientCache() throws IOException, URISyntaxException {


       Properties props = new Properties();
       props.setProperty("security-client-auth-init", "com.example.demogemfire.config.ClientAuthInitialize.create");
       ClientCacheFactory ccf = new ClientCacheFactory(props);
       ccf.setPdxSerializer(new MappingPdxSerializer());
       List<URI> locatorList = EnvParser.getInstance().getLocators();

       for (URI locator : locatorList) {
           ccf.addPoolLocator(locator.getHost(), locator.getPort());
       }

       return ccf.create();

   }


}
