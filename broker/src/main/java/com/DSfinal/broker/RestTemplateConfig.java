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
import org.springframework.http.HttpMethod;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(
            Auth0TokenService tokenService) {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();
        RestTemplate restTemplate =
                new RestTemplate(factory);

                restTemplate.getInterceptors().add((request, body, execution) -> {
                        // avoid fetching a token for simple GETs (public endpoints)
                        // only add Authorization header for non-GET methods (reserve/confirm/cancel)
                        try {
                                System.out.println("Broker -> calling: " + request.getMethod() + " " + request.getURI());
                                if (request.getMethod() != null && request.getMethod() != HttpMethod.GET) {
                                        String token = tokenService.getAccessToken();
                                        if (token != null && !token.isBlank()) {
                                                request.getHeaders().setBearerAuth(token);
                                                System.out.println("Added bearer token for call to " + request.getURI());
                                        } else {
                                                System.out.println("No token available for protected call to " + request.getURI());
                                        }
                                } else {
                                        // public GET - do not attempt to fetch token (keeps supplier discovery resilient)
                                        System.out.println("Skipping token for public GET " + request.getURI());
                                }
                        } catch (Exception ex) {
                                System.out.println("Failed to obtain token: " + ex.getMessage());
                                // for GET requests we can continue without token, for others rethrow to fail fast
                                if (request.getMethod() != null && request.getMethod() != HttpMethod.GET) {
                                        throw ex;
                                }
                        }

                        return execution.execute(request, body);
                });

        return restTemplate;
    }
}