package com.resetrix.horaion.shared.securities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static com.resetrix.horaion.shared.constants.SecurityConstants.CLAIM_COGNITO_GROUPS;

@Component
public class CognitoJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CognitoJwtAuthenticationConverter.class);

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        LOGGER.debug("Converted JWT for user {} with authorities: {}",
                jwt.getClaimAsString("username"), authorities);

        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extract authorities from Cognito groups only
        List<String> groups = jwt.getClaimAsStringList(CLAIM_COGNITO_GROUPS);
        if (groups != null) {
            authorities.addAll(groups.stream()
                .map(group -> new SimpleGrantedAuthority(
                    "ROLE_" + group.toUpperCase(Locale.ROOT).replace("-", "_")))
                .toList());

            LOGGER.debug("Extracted authorities from Cognito groups {}: {}", groups,
                    groups.stream().map(g -> "ROLE_" + g.toUpperCase(Locale.ROOT).replace("-", "_")).toList());
        } else {
            LOGGER.debug("No Cognito groups found in JWT token");
        }

        return authorities;
    }
}