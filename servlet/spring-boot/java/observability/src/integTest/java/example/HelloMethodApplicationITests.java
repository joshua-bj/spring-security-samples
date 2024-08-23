/*
 * Copyright 2020 the original author or authors.
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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests.
 *
 * @author Rob Winch
 * @since 5.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloMethodApplicationITests {

	@Autowired
	TestRestTemplate rest;

	// --- /message ---

	@Test
	void messageWhenNotAuthenticated() {
		// @formatter:off
		assertThat(this.rest.getForEntity("/message", String.class).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void messageWhenUserThenOk() {
		ResponseEntity<?> response = this.rest.exchange("/message", HttpMethod.GET, userCredentials(), String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().toString()).isEqualTo("Hello User!");
	}

	// --- /secret ---

	@Test
	void secretWhenNotAuthenticated() {
		assertThat(this.rest.getForEntity("/secret", String.class).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void secretWhenUserThenForbidden() {
		assertThat(this.rest.exchange("/secret", HttpMethod.GET, userCredentials(), String.class).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void secretWhenAdminThenOk() {
		ResponseEntity<?> response = this.rest.exchange("/secret", HttpMethod.GET, adminCredentials(), String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().toString()).isEqualTo("Hello Admin!");
	}

	private HttpEntity<?> userCredentials() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth("user", "password");
		return new HttpEntity<>(headers);
	}

	private HttpEntity<?> adminCredentials() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth("admin", "password");
		return new HttpEntity<>(headers);
	}

}
