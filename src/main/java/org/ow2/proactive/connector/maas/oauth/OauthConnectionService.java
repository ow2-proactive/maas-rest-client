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

import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestOperations;


public interface OauthConnectionService {

    // implementation of this method should be annotated with @Async
    @Async
    public <T> Future<T> getAsynchronousResults(String resourceUrl, Class<T> resultType, RestOperations restTemplate);

    public <T> T getResults(String resourceUrl, Class<T> resultType, RestOperations restTemplate);

    default <T> T getForObject(String resourceUrl, Class<T> responseType, RestOperations restTemplate) {
        return restTemplate.getForObject(resourceUrl, responseType);
    }
    /*
     * default <T> T getForObject(String resourceUrl, Class<T> responseType, RestOperations
     * restTemplate) {
     * 
     * HttpHeaders headers = new HttpHeaders();
     * headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
     * HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
     * 
     * return restTemplate.exchange(resourceUrl, HttpMethod.GET, entity, responseType);
     * }
     * 
     * headers.setAccept(Collections.singletonList(MediaType.APPLIC‌​ATION_JSON));
     */
}
