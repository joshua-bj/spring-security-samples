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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {
	@Bean
	SecurityFilterChain web(HttpSecurity http, Customizer<ExceptionHandlingConfigurer<HttpSecurity>> exceptions) throws Exception {
		http
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers("/login").permitAll()
				.anyRequest().authenticated()
			)
			.exceptionHandling(exceptions);
		return http.build();
	}

	@FormLoginController
	public static class AuthController {
		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

		@Autowired
		CustomUserRepository users;

		@GetMapping
		@ExceptionHandler(AuthenticationException.class)
		public String login() {
			return "login";
		}

		@PostMapping
		public Authentication login(@AuthenticationPrincipal UsernamePasswordAuthenticationToken token) {
			CustomUser user = this.users.findCustomUserByEmail(token.getPrincipal().toString());
			if (user == null) {
				throw new UsernameNotFoundException("user not found");
			}
			if (!this.passwordEncoder.matches((String) token.getCredentials(), user.getPassword())) {
				throw new BadCredentialsException("bad credentials");
			}
			return UsernamePasswordAuthenticationToken.authenticated(user, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
		}
	}
}
