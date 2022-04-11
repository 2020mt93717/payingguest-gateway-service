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
package payingguest.gateway.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import payingguest.gateway.exception.AuthTokenMalformedException;
import payingguest.gateway.exception.AuthTokenMissingException;
import payingguest.gateway.service.AuthenticationService;

@Component
public class AuthenticationServiceImpl implements AuthenticationService {

    @Value("${jwt.secret}")
    private String mJwtSecret;

    @Override
    public Claims getClaim(String pJwtToken) {
        try {
            return Jwts.parser().setSigningKey(mJwtSecret).parseClaimsJws(pJwtToken).getBody();
        } catch (Exception e) {
            System.out.println(e.getMessage() + " => " + e);
        }
        return null;
    }

    @Override
    public void validateToken(String pJwtToken) throws AuthTokenMalformedException, AuthTokenMissingException {
        try {
            Jwts.parser().setSigningKey(mJwtSecret).parseClaimsJws(pJwtToken);
        } catch (MalformedJwtException ex) {
            throw new AuthTokenMalformedException("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            throw new AuthTokenMalformedException("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            throw new AuthTokenMalformedException("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            throw new AuthTokenMissingException("No JWT claim is empty for the token");
        }
    }
}
