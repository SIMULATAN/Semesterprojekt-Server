package com.github.simulatan.semesterprojekt_server.user;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;

import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.identity.SecurityIdentity;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.json.JSONObject;

import java.util.UUID;

@Path("/api/users")
public class UserResource {

    @Inject
    SecurityIdentity keycloakSecurityContext;

    @GET
    @Path("/me")
    @NoCache
    public String me() throws MalformedClaimException {
        return new User(keycloakSecurityContext).getJson();
    }

    public static class User {

        private final long expiration;
        private final long issuedAt;
        private final UUID userId;

        User(SecurityIdentity securityContext) throws MalformedClaimException {
            if (securityContext.isAnonymous()) {
                throw new NotAuthorizedException("User is anonymous");
            }
            OidcJwtCallerPrincipal principal = (OidcJwtCallerPrincipal) securityContext.getPrincipal();
            JwtClaims claims = principal.getClaims();
            this.expiration = claims.getExpirationTime().getValueInMillis();
            this.issuedAt = claims.getIssuedAt().getValueInMillis();
            this.userId = UUID.fromString(claims.getSubject());
        }

        public String getJson() {
            return new JSONObject()
                .put("expiration", expiration)
                .put("issuedAt", issuedAt)
                .put("userId", userId)
                .toString();
        }
    }
}