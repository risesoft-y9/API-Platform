package net.risesoft.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedRequestContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import net.risesoft.y9.Y9Context;
/**
 * 
 * @author 乌力吉
 *
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	/*
	 * starter-log工程用到了RequestContextHolder
	 * https://github.com/spring-projects/spring-boot/issues/2637
	 * https://github.com/spring-projects/spring-boot/issues/4331
	 */	
	@Bean
	public static RequestContextFilter requestContextFilter() {
		return new OrderedRequestContextFilter();
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addRedirectViewController("/", "/index");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@DependsOn("y9Context")
	@Bean
	public FilterRegistrationBean checkUserLoginFilter() {
		FilterRegistrationBean filterBean = new FilterRegistrationBean();
		filterBean.setFilter(new CheckLoginFilter());
		filterBean.setAsyncSupported(false);
		filterBean.setOrder(50);
		filterBean.addUrlPatterns("/*");
		return filterBean;
	}

	@Bean
	public Y9Context y9Context() {
		return new Y9Context();
	}

	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
		supportedMediaTypes.add(MediaType.parseMediaType("text/html;charset=UTF-8"));
		converter.setSupportedMediaTypes(supportedMediaTypes);
		return converter;
	}
	
}