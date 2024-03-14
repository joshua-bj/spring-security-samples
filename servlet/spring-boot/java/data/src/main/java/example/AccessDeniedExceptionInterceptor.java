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

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.Pointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.method.AuthorizationAdvisor;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.stereotype.Component;

@Component
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AccessDeniedExceptionInterceptor implements AuthorizationAdvisor {
	AuthorizationAdvisor advisor = AuthorizationManagerBeforeMethodInterceptor.preAuthorize();

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			return invocation.proceed();
		} catch (AccessDeniedException ex) {
			return null;
		}
	}

	@Override
	public Pointcut getPointcut() {
		return this.advisor.getPointcut();
	}

	@Override
	public Advice getAdvice() {
		return this;
	}

	@Override
	public int getOrder() {
		return this.advisor.getOrder() - 1;
	}
}
