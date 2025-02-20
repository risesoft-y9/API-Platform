package net.risesoft.config;

import net.risesoft.y9.configuration.Y9Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * @author 乌力吉
 *
 */
@Configuration
@EnableConfigurationProperties({Y9Properties.class})
@ConditionalOnProperty(
        name = {"y9.feature.oauth2.resource.enabled"},
        havingValue = "true",
        matchIfMissing = true
)
public class TokenFilterConfig {

    @Value("${spring.eureka.token}")
    private String token;

    @Bean
	public FilterRegistrationBean<TokenAuthenticationFilter> tokenAuthenticationFilter(Y9Properties y9Properties) {
		FilterRegistrationBean<TokenAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new TokenAuthenticationFilter(y9Properties,token));
		registrationBean.addUrlPatterns("/eureka/*"); // 设置过滤器应用的URL模式
		return registrationBean;
	}

}