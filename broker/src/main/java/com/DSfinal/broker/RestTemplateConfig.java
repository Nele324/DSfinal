//Rest Template is Spring’s built-in HTTP client, it lets your
// java code make HTTP calls to other services, the same way your
// browser make HTTP calls to other services,
// without it, broker has no way to call the catering or venue APIs.
// Without it, you would have to do ugly low level java url httpURLConnection thingy.
// With rest template, it’s easier.
// We create rest template as a Bean so that we can tell Spring
// “create this object once, keep it alive and inject in wherever I ask for it”.
//Anywhere in your app you write @Autowired RestTemplate restTemplate,
// Spring hands you that same instance. You never call new RestTemplate() yourself again.

package com.DSfinal.broker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // Connect timeout: 3000 milliseconden (= 3 seconden)
        factory.setConnectTimeout(3000);
        
        // Read timeout: 3000 milliseconden (= 3 seconden)
        factory.setReadTimeout(3000);
        
        return new RestTemplate(factory);
    }
}