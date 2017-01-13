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

import java.util.Arrays;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Factory for RestTemplate instances that execute requests for resources protected by the OAuth 1 protocol.
 * Encapsulates the configuration of the interceptor that adds the necessary Authorization header to each request before it is executed.
 * Also hides the differences between Spring 3.0.x and 3.1 implementation.
 *
 * <h4>Parameter Encoding</h4>
 *
 * <p>The underlying OAuth signing algorithm assumes that query parameters are encoded as application/x-www-form-urlencoded.
 * The RestTemplate methods that take String URL templates encode query parameters per RFC 3986 and not form-encoded.
 * This leads to problems where certain characters are improperly encoded. Spaces, for example are encoded as %20 instead of +;
 * and an actual + sign is left unencoded (and will be interpreted as a space when decoded as if it were form-encoded).</p>
 *
 * <p>However, RestTemplate's methods that take URIs will leave the URI's parameters untouched. Therefore, when consuming a REST operation
 * with query parameters that require encoding (for example, if passing a + sign in a parameter value) you should use RestTemplate's
 * URI-based methods constructed with form-encoded parameters. See URIBuilder for a convenient way to build up such URIs.</p>
 *
 * @author ActiveEon Team
 * @since 10/01/17
 */
public class ProtectedResourceClientFactory {

    /**
     * Constructs a RestTemplate that adds the OAuth1 Authorization header to each request before it is executed.
     */
    public static RestTemplate create(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        RestTemplate client = new RestTemplate(new SimpleClientHttpRequestFactory());

        // favored
        client.setInterceptors(Arrays.asList(new ClientHttpRequestInterceptor[]{new OAuth1RequestInterceptor(consumerKey, consumerSecret, accessToken, accessTokenSecret)}));

        return client;
    }
}

