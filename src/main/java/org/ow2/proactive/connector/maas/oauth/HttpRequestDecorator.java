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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.support.HttpRequestWrapper;


/**
 * Extension of HttpRequestWrapper that supports adding new HttpHeaders to the wrapped HttpRequest.
 *
 * @author ActiveEon Team
 * @since 10/01/17
 */
public class HttpRequestDecorator extends HttpRequestWrapper {

    private HttpHeaders httpHeaders;

    private boolean existingHeadersAdded;

    public HttpRequestDecorator(HttpRequest request) {
        super(request);
    }

    public HttpHeaders getHeaders() {
        if (!existingHeadersAdded) {
            this.httpHeaders = new HttpHeaders();
            httpHeaders.putAll(getRequest().getHeaders());
            existingHeadersAdded = true;
        }
        return httpHeaders;
    }

}
