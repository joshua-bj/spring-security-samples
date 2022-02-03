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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider.ResponseToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.stereotype.Component;

@Component
public class Saml2ResponseAuthenticationConverter implements Converter<ResponseToken, Saml2Authentication> {
    private final UserDetailsService users;
    private final Converter<ResponseToken, Saml2Authentication> authenticationConverter =
            OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter();

    public Saml2ResponseAuthenticationConverter(UserDetailsService users) {
        this.users = users;
    }

    @Override
    public Saml2Authentication convert(ResponseToken source) {
        Response response = source.getResponse();
        String name = response.getAssertions().get(0).getSubject().getNameID().getValue();
        Saml2Authentication authentication = this.authenticationConverter.convert(source);
        UserDetails user = this.users.loadUserByUsername(name);
        Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
        return new Saml2Authentication(new MyAuthenticatedPrincipal(user, principal),
                authentication.getSaml2Response(), user.getAuthorities());
    }

    private static class MyAuthenticatedPrincipal extends User implements Saml2AuthenticatedPrincipal {
        private final Saml2AuthenticatedPrincipal principal;

        MyAuthenticatedPrincipal(UserDetails user, Saml2AuthenticatedPrincipal principal) {
            super(user.getUsername(), user.getPassword(), user.getAuthorities());
            this.principal = principal;
        }

        @Override
        public String getName() {
            return getUsername();
        }

        @Override
        public Map<String, List<Object>> getAttributes() {
            return this.principal.getAttributes();
        }

        @Override
        public String getRelyingPartyRegistrationId() {
            return this.principal.getRelyingPartyRegistrationId();
        }
    }
}
