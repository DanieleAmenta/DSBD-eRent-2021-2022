package com.erent.gateway;

import com.erent.gateway.config.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Autowired
    AuthenticationFilter filter;

    @Value("${services}")
    private String services;

    @Value("${api_services}")
    private String api;

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder b = builder.routes();

        String[] s = services.split(",");
        String[] a = api.split(",");

        for(int i = 0; i < s.length; i++){
            String service = s[i];
            String api_base = a[i];
            b = b.route(service, r -> r.path( String.format("/%s/**", api_base))
                    .filters(f -> f.filter(filter))
                    .uri("lb://" + service));

            System.out.println("Added route for service " + service + " -> /" + api_base + "/**");
        }

        return b.build();
    }
}
