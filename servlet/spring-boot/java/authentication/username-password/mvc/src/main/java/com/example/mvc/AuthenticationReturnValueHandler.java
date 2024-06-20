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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthenticationReturnValueHandler implements HandlerMethodReturnValueHandler {
	SecurityContextHolderStrategy strategy = SecurityContextHolder.getContextHolderStrategy();

	SecurityContextRepository contexts = new HttpSessionSecurityContextRepository();

	SessionAuthenticationStrategy session  = new ChangeSessionIdAuthenticationStrategy();

	SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return Authentication.class.isAssignableFrom(returnType.getParameterType());
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
		Authentication authentication = (Authentication) returnValue;
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
		SecurityContext context = new SecurityContextImpl(authentication);
		this.session.onAuthentication(authentication, request, response);
		this.contexts.saveContext(context, request, response);
		this.strategy.setContext(context);
		this.successHandler.onAuthenticationSuccess(request, response, authentication);
	}
}
