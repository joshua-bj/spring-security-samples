/*
 * Copyright 2012-2016 the original author or authors.
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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Hello Security application.
 *
 * @author Joe Grandja
 */
@SpringBootApplication
public class HelloSecurityApplication {

	@Bean
	SecurityFilterChain chain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated());
		http.formLogin();
		http.sessionManagement((session) -> session
				.invalidSessionUrl("/")
				.sessionConcurrency((concurrency) -> concurrency
						.maximumSessions(1)
						.maxSessionsPreventsLogin(true)
				)
		);
		return http.build();
	}

	@Bean
	HttpSessionEventPublisher publisher() {
		return new HttpSessionEventPublisher();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return new InMemoryUserDetailsManager(
				User.withDefaultPasswordEncoder()
						.username("user")
						.password("password")
						.authorities("app")
						.build()
		);
	}

	public static void main(String[] args) {
		SpringApplication.run(HelloSecurityApplication.class, args);
	}

}
