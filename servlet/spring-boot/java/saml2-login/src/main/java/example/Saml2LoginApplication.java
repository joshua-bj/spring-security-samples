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

import java.io.File;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;

import org.opensaml.security.x509.X509Support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
public class Saml2LoginApplication {

	@Value("${classpath:credentials/rp-private.key}")
	RSAPrivateKey key;

	@Value("${classpath:credentials/rp-certificate.crt}")
	File file;

	@Bean
	SecurityFilterChain app(HttpSecurity http) throws Exception {
		http
			.authorizeRequests((authorize) -> authorize
				.mvcMatchers("/error").permitAll()
				.anyRequest().authenticated()
			)
			.saml2Login(Customizer.withDefaults())
			.saml2Logout(Customizer.withDefaults());

		return http.build();
	}

	@Bean
	RelyingPartyRegistrationRepository registrations() throws Exception {
		X509Certificate certificate = X509Support.decodeCertificate(this.file);
		Saml2X509Credential signing = Saml2X509Credential.signing(this.key, certificate);
		RelyingPartyRegistration registration = RelyingPartyRegistrations.fromMetadataLocation("https://simplesaml-for-spring-saml.apps.pcfone.io/saml2/idp/metadata.php")
				.registrationId("one")
				.signingX509Credentials((credentials) -> credentials.add(signing))
				.singleLogoutServiceBinding(Saml2MessageBinding.REDIRECT)
				.build();
		return new InMemoryRelyingPartyRegistrationRepository(registration);
	}

	public static void main(String[] args) {
		SpringApplication.run(Saml2LoginApplication.class, args);
	}

}
