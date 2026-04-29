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

@Configuration
public class RestTemplateConfig {

    @Bean //creating it as a Bean object
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
