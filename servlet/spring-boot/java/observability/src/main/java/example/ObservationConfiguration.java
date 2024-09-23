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

package example;

import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.ObservationTextPublisher;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.ObservationAuthorizationManager;
import org.springframework.security.config.observation.ObservationObjectPostProcessor;
import org.springframework.security.config.observation.SecurityObservationPredicate;

@Configuration(proxyBeanMethods = false)
public class ObservationConfiguration {

	@Bean
	ObservationHandler<?> observationHandler() {
		return new ObservationTextPublisher();
	}

	@Bean
	ObservationPredicate securityObservations() {
		return SecurityObservationPredicate.withDefaults().build();
	}

	// Changes the MethodInvocation AuthorizationManger instances managed by Spring Security
	// Use <AuthorizationManager<?>> to affect all AuthorizationManager instances managed by Spring Security
	@Bean
	ObservationObjectPostProcessor<AuthorizationManager<MethodInvocation>> methodAuthorizationObservations() {
		return new ObservationObjectPostProcessor<>() {
			@Override
			public AuthorizationManager<MethodInvocation> postProcess(ObservationRegistry registry, AuthorizationManager<MethodInvocation> object) {
				ObservationAuthorizationManager<MethodInvocation> manager = new ObservationAuthorizationManager<>(registry, object);
				manager.setObservationConvention(new MyObservationConvention());
				return manager;
			}
		};
	}

	// Do not wire filter chain observations. Handy for simplifying the stack trace.
	@Bean
	ObservationObjectPostProcessor<?> requestAuthorizationObservations() {
		return ObservationObjectPostProcessor.noop();
	}
}
