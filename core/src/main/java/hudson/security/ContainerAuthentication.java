/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.security;

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;
import java.util.ArrayList;

import hudson.model.Hudson;

/**
 * {@link Authentication} implementation for {@link Principal}
 * given through {@link HttpServletRequest}.
 *
 * <p>
 * This is used to plug the container authentication to Acegi,
 * for backward compatibility with Hudson &lt; 1.160.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ContainerAuthentication implements Authentication {
    private final HttpServletRequest request;
    private GrantedAuthority[] authorities;

    public ContainerAuthentication(HttpServletRequest request) {
        this.request = request;
    }

    public GrantedAuthority[] getAuthorities() {
        if(authorities==null) {
            // Servlet API doesn't provide a way to list up all roles the current user
            // has, so we need to ask AuthorizationStrategy what roles it is going to check against.
            List<GrantedAuthority> l = new ArrayList<GrantedAuthority>();
            for( String g : Hudson.getInstance().getAuthorizationStrategy().getGroups()) {
                if(request.isUserInRole(g))
                    l.add(new GrantedAuthorityImpl(g));
            }
            l.add(SecurityRealm.AUTHENTICATED_AUTHORITY);
            authorities = l.toArray(new GrantedAuthority[l.size()]);
        }
        return authorities;
    }

    public Object getCredentials() {
        return null;
    }

    public Object getDetails() {
        return null;
    }

    public String getPrincipal() {
        return request.getUserPrincipal().getName();
    }

    public boolean isAuthenticated() {
        return true;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        // noop
    }

    public String getName() {
        return getPrincipal();
    }
}
