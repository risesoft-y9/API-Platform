package net.risesoft;

import net.risesoft.y9.configuration.Y9Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableTransactionManagement
@EnableConfigurationProperties(Y9Properties.class)
public class InterfacePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterfacePlatformApplication.class, args);
    }
}
