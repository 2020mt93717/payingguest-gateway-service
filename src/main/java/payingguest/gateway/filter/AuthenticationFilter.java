/***************************************************************************************
 * MIT License
 *
 * Copyright (c) 2022 2020mt93717
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * **************************************************************************************/
package payingguest.gateway.filter;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import payingguest.gateway.exception.AuthTokenMalformedException;
import payingguest.gateway.exception.AuthTokenMissingException;
import payingguest.gateway.service.AuthenticationService;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GatewayFilter {

    @Autowired
    private AuthenticationService mAuthenticationService;

    @Override
    public Mono<Void> filter(ServerWebExchange pServerWebExchange, GatewayFilterChain pFilterChain) {
        ServerHttpRequest myRequest = pServerWebExchange.getRequest();

        // Endpoints for Register User and Login user
        List<String> myUnsecureEndpoints = List.of("/register", "/login");

        Predicate<ServerHttpRequest> myApiSecuredPredicate = pHttpRequest -> myUnsecureEndpoints.stream()
                .noneMatch(myUrl -> pHttpRequest.getURI().getPath().contains(myUrl));

        if (myApiSecuredPredicate.test(myRequest)) {
            if (!myRequest.getHeaders().containsKey("Authorization")) {
                ServerHttpResponse response = pServerWebExchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            String myJwtToken = myRequest.getHeaders().getOrEmpty("Authorization").get(0);
            try {
                mAuthenticationService.validateToken(myJwtToken);
            } catch (AuthTokenMalformedException e) {
                ServerHttpResponse response = pServerWebExchange.getResponse();
                response.setStatusCode(HttpStatus.BAD_REQUEST);
                return response.setComplete();
            } catch (AuthTokenMissingException e) {
                ServerHttpResponse response = pServerWebExchange.getResponse();
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return response.setComplete();
            }
            Claims myClaim = mAuthenticationService.getClaim(myJwtToken);
            pServerWebExchange.getRequest().mutate().header("id", String.valueOf(myClaim.getId())).build();
        }
        return pFilterChain.filter(pServerWebExchange);
    }
}
