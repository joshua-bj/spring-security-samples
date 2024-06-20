/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mvc;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class MvcFormLoginConfiguration implements WebMvcConfigurer {

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new FormLoginMethodArgumentResolver());
	}

	@Override
	public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
		handlers.add(new AuthenticationReturnValueHandler());
	}

	@Bean
	public Customizer<ExceptionHandlingConfigurer<HttpSecurity>> loginPage(ListableBeanFactory beanFactory) {
		Map<String, Object> beans = beanFactory.getBeansWithAnnotation(FormLoginController.class);
		if (beans.isEmpty()) {
			return Customizer.withDefaults();
		}
		Object formLogin = beans.values().iterator().next();
		FormLoginController config = AnnotationUtils.findAnnotation(formLogin.getClass(), FormLoginController.class);
		AuthenticationEntryPoint entryPoint = new LoginUrlAuthenticationEntryPoint(config.value());
		return (exceptions) -> exceptions.authenticationEntryPoint(entryPoint);
	}

	/*@Bean
	public Customizer<ExceptionHandlingConfigurer<HttpSecurity>> loginPage(RequestMappingHandlerMapping mapping) {
		for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mapping.getHandlerMethods().entrySet()) {
			FormLoginController login = AnnotationUtils.findAnnotation(entry.getValue().getMethod().getDeclaringClass(), FormLoginController.class);
			if (login == null) {
				continue;
			}
			GetMapping get = AnnotationUtils.findAnnotation(entry.getValue().getMethod(), GetMapping.class);
			if (get == null) {
				continue;
			}
			String path = entry.getKey().getDirectPaths().iterator().next();
			return (exceptions) -> exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(path));
		}
		return Customizer.withDefaults();
	}*/

}
