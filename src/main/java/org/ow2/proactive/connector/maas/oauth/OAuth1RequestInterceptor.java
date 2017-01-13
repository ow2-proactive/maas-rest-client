/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.connector.maas.oauth;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * ClientHttpRequestInterceptor implementation that performs OAuth1 request signing before a request for a protected resource is executed.
 *
 * @author ActiveEon Team
 * @since 10/01/17
 */
public class OAuth1RequestInterceptor implements ClientHttpRequestInterceptor {

    private final String consumerKey;

    private final String consumerSecret;

    private final String accessToken;

    private final String accessTokenSecret;

    private final SigningSupport signingUtils;

    /**
     * Creates an OAuth 1.0 protected resource request interceptor.
     * @param accessToken the access token and secret
     */
    public OAuth1RequestInterceptor(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        this.signingUtils = new SigningSupport();
    }

    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpRequest protectedResourceRequest = new HttpRequestDecorator(request);
        protectedResourceRequest.getHeaders().add("Authorization", getAuthorizationHeaderValue(request, body));
        return execution.execute(protectedResourceRequest, body);
    }

    // internal helpers

    private String getAuthorizationHeaderValue(HttpRequest request, byte[] body) {
        return signingUtils.buildAuthorizationHeaderValue(request, body, consumerKey, consumerSecret, accessToken, accessTokenSecret);
    }

}
