/*
 * Copyright 2022 the original author or authors.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ListeningSecurityContextHolderStrategy;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextChangedEvent;
import org.springframework.security.core.context.SecurityContextHolderStrategy;

@Configuration
class SecurityObservationConfig {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    SecurityContextHolderStrategy listening() {
        return new ListeningSecurityContextHolderStrategy(this::recordEvent);
    }

    private void recordEvent(SecurityContextChangedEvent event) {
        Authentication previous = authentication(event.getOldContext());
        Authentication next = authentication(event.getNewContext());
        if (previous == null && next == null) {
            return;
        }
        if (previous == null) {
            this.logger.info("Set " + next.getClass().getSimpleName());
            return;
        }
        if (next == null) {
            this.logger.info("Clear " + previous.getClass().getSimpleName());
            return;
        }
        this.logger.info("Change from " + previous.getClass().getSimpleName() + " to " + next.getClass().getSimpleName());
    }

    private Authentication authentication(SecurityContext context) {
        if (context == null) {
            return null;
        }
        return context.getAuthentication();
    }
}
