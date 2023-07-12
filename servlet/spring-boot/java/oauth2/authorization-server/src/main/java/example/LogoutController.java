/*
 * Copyright 2023 the original author or authors.
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Controller
public class LogoutController {
    private final Log logger = LogFactory.getLog(getClass());

    private final RestOperations rest = new RestTemplate();

    @Autowired
    JwtEncoder jwtEncoder;

    @GetMapping("/backchannel-logout")
    @ResponseBody
    List<String> backchannelLogout(Authentication authentication) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(Instant.now())
                .claim("events", Map.of("http://schemas.openid.net/event/backchannel-logout", Collections.emptyMap()))
                .audience(List.of("login-client"))
                .issuer("http://localhost:9000")
                .id(UUID.randomUUID().toString()).build();
        Jwt jwt = this.jwtEncoder.encode(JwtEncoderParameters.from(claims));
        this.logger.info(String.format("Logout Token: %s", jwt.getTokenValue()));
        String uri = "http://127.0.0.1:8080/logout/connect/back-channel/login-client";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("logout_token", jwt.getTokenValue());
        HttpEntity<?> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = this.rest.postForEntity(uri, request, String.class);
        return List.of("Sent token: " + jwt.getTokenValue(), "Received status: " + response.getStatusCode());
    }

}
