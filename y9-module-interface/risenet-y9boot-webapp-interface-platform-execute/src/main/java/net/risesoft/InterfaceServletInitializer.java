package net.risesoft;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


public class InterfaceServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
		return builder.sources(InterfaceExecuteApplication.class);
	}
}