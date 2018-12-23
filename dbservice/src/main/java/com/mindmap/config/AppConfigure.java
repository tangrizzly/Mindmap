package com.mindmap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfigure {
	@Bean
	public SyncEndPointConfigure syncEndPointConfigure()
	{
		return new SyncEndPointConfigure();
	}
}