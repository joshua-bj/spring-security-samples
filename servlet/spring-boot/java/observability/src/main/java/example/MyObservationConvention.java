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

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

import org.springframework.security.authorization.AuthorizationObservationContext;
import org.springframework.security.authorization.AuthorizationObservationConvention;

public class MyObservationConvention implements ObservationConvention<AuthorizationObservationContext<?>> {

	private final AuthorizationObservationConvention delegate = new AuthorizationObservationConvention();

	@Override
	public KeyValues getLowCardinalityKeyValues(AuthorizationObservationContext<?> context) {
		KeyValues keyValues = this.delegate.getLowCardinalityKeyValues(context);
		if (context.getDecision() instanceof MyAuthorizationDecision my) {
			return keyValues.and("some.value", my.getSomeValue());
		}
		return keyValues;
	}

	@Override
	public KeyValues getHighCardinalityKeyValues(AuthorizationObservationContext<?> context) {
		return this.delegate.getHighCardinalityKeyValues(context);
	}

	@Override
	public boolean supportsContext(Observation.Context context) {
		return context instanceof AuthorizationObservationContext<?>;
	}

	@Override
	public String getName() {
		return this.delegate.getName();
	}

	@Override
	public String getContextualName(AuthorizationObservationContext<?> context) {
		return this.delegate.getContextualName(context);
	}

}
