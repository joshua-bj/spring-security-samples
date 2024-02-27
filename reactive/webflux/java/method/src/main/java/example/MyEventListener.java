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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Mono;

import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.event.ReactiveAuthorizationEvent;
import org.springframework.stereotype.Component;

@Component
public class MyEventListener {
	private final Log logger = LogFactory.getLog(getClass());

	@EventListener
	public Mono<Void> onApplicationEvent(ReactiveAuthorizationEvent event) {
		return event.getAuthentication().doOnNext((a) -> this.logger.error("Authz denied for : " + a.getName() + "; " + event.getAuthorizationDecision())).then();
	}
}
