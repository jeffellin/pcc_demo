##  Cache Server for local development
Use Spring Boot

```
@SpringBootApplication
@CacheServerApplication(name = "SpringBootGemFireServer")
@EnableLocator
@EnableManager
public class SpringBootGemFireServer {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootGemFireServer.class);
    }
```
    
This code will start up a single node Gemfire Server with both a locator and a cache node.  Once you have the cache server running, you will need to create a region in which to store data. 

```
    @Bean(name = "Persons")
    ReplicatedRegionFactoryBean personsRegion(Cache gemfireCache) {

        ReplicatedRegionFactoryBean person = new ReplicatedRegionFactoryBean<>();

        person.setCache(gemfireCache);
        person.setClose(false);
        person.setPersistent(false);

        return person;

    }
```

The `Customer` region will store Customer objects with a Long type used as the key.

Start the cache server by running the main class.

```
[info 2018/04/12 12:16:10.786 EDT <main> tid=0x1] Initializing region Customers
[info 2018/04/12 12:16:10.786 EDT <main> tid=0x1] Initialization of region Customers completed
Cache server connection listener bound to address 0.0.0.0/0.0.0.0:40404
```    
    
### Simple Client

Next, we will create a simple client also using Spring Boot which will do most of the heavy lifting for us.

```
@SpringBootApplication
@ClientCacheApplication(name = "AccessingDataGemFireApplication", logLevel = "error")
@EnableEntityDefinedRegions(basePackages = {"com.example.demogemfire.model"},
        clientRegionShortcut = ClientRegionShortcut.CACHING_PROXY)
@EnableGemfireRepositories
@EnablePdx()
public class DemoGemfireApplication {
...
}
```


1. This class is a SpringBootApplication
2. This application requires a client cache
3. Automatically define client regions based on Repositories found on the classpath
4. Make Repositories found,  GemFire repositories
5. Enable PDX Serialization.  

We then can define a typical Spring Data Repository.

```
interface PersonRepository extends CrudRepository<Person, String> {

    Person findByName(String name);
 
    ...
}    
```    
Lastly, we need to tell Spring where to find the cache locator by adding a property to application.properties. The correct host and port should be visible in your CacheServer startup log.

```
spring.data.gemfire.pool.locators=localhost[10334]
```

When you run the application, you should see output indicating data was placed in the cache and subsequently retrieved from the cache.

## Using Pivotal Cloud Cache (PCC) 
PCC is a cloud-based cache that can be deployed to Cloud Foundry.  Assuming your PCF instance has PCC already installed you can efficiently utilize the `cf` command line to create and maintain your cache.

### Create the cache
1. Verify that PCC is available.
   
   ```
    cf marketplace
    ``` 
    
    Look for ```p-cloudcache```. If it isn't available, you will need to work with your cloud operator to have them install the tile.

2. Create the service

    ```
    cf create-service p-cloudcache dev-plan pcc
    ```
    Create a service instance of the cloud cache called `pcc` This may take some time to complete so you can monitor its progress with

    ```
    cf service pcc
    ```

3. Service Key

    Once the instance creation succeeds, we will need a service key. The service key will provide the required credentials for working with the cache. By default, you will have two users one with `developer` access and one with `operator` access.  This information will also be exposed via `VCAP_SERVICES` to allow applications in other deployed containers to connect.

    ```
    cf create-service-key pcc pcc-key
    ```
    ```
    cf service-key pcc pcc-key                                                         
    Getting key pcc-key for service instance pcc as jellin@pivotal.io...
    
    {
     "distributed_system_id": "12",
     "locators": [
      "192.168.12.186[55221]"
     ],
     "urls": {
      "gfsh": "https://cloudcache-yourserver.io/gemfire/v1",
      "pulse": "https://cloudcache-yourserver.io/pulse"
     },
     "users": [
      {
       "password": "**********",
       "roles": [
        "developer"
       ],
       "username": "developer_*******"
      },
      {
       "password": "***********",
       "roles": [
        "cluster_operator"
       ],
       "username": "cluster_operator_*******"
      }
     ],
     "wan": {
      "sender_credentials": {
       "active": {
        "password": "**********",
        "username": "gateway_sender_*******"
       }
      }
     }
    }
```    

Create a Region in PCC to hold the data. Use the locator URL and GFSH operator credentials from above.

Load the GFSH utility included with the GemFire distribution.

```
./gfsh

```
Connect to the cache

```
gfsh>connect --use-http --url https://cloudcache-yourserver.io/gemfire/v1 --user=cluster_operator_DnrQ139FKwjTaLpBJsuQ --password=OxKlo8GXHGgWcRNGPx6nw
Successfully connected to: GemFire Manager HTTP service @ org.apache.geode.management.internal.web.http.support.HttpRequester@a34930a

Cluster-12 gfsh>
```

Create the region

```
Cluster-12 gfsh>create region --name=Person --type=REPLICATE
                     Member                      | Status
------------------------------------------------ | ------------------------------------------------------------------------
cacheserver-3418fce1-13dd-4104-97ba-083b11b7a936 | Region "/Person" created on "cacheserver-3418fce1-13dd-4104-97ba-083b1..
```

### Service Discovery
When binding a service to an application container in PCF, we can expose connection information such as URLs and credentials that may change over time.  Spring Cloud for Gemfire can automate the retrieval of these credentials.

<div class="alert alert-success">       
**NOTE**: While it would be ideal to use Spring Cloud Gemfire to automate the connection we can't currently extend additional configuration parameters such as PDX Serialization.  This is because the connector creates the `ClientCache` before the `@ClientCacheApplication` annotation.  In order to work around this add the `@EnableSecurity` annotation and the following config properties.
</div>

```spring.data.gemfire.pool.locators=192.168.12.185[55221]
spring.data.gemfire.security.username=cluster_operator_****
spring.data.gemfire.security.password=****
```
*This is being addressed in a future release of Spring Boot.*


Create a PCF manifest to bind the cache to your application

```
---
applications:
- name: client
  path: target/gs-accessing-data-gemfire-0.1.0.jar
  no-hostname: true
  no-route: true
  health-check-type: none
  services:
  - pcc

``` 
Push your app as normal

```
cf push
```

use `cf` client to view the results

```
cf logs client
```