package net.risesoft;

import net.risesoft.y9.configuration.Y9Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;


@SpringBootApplication
@EnableEurekaClient
@EnableConfigurationProperties(Y9Properties.class)
public class InterfaceExecuteApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterfaceExecuteApplication.class, args);
    }
}
