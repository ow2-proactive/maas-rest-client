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
package org.ow2.proactive.connector.maas.rest;

import java.util.Collections;
import java.util.HashMap;

import org.ow2.proactive.connector.maas.data.CommissioningScript;
import org.ow2.proactive.connector.maas.data.MaasVersion;
import org.ow2.proactive.connector.maas.data.Machine;
import org.ow2.proactive.connector.maas.data.Tag;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author ActiveEon Team
 * @since 11/01/17
 */
public class RestClient {

    private HashMap<Class, ParameterizedTypeReference> typesManaged;
    private RestTemplate restTemplate;
    private HttpHeaders headers;
    private String apiUrl;

    public RestClient(RestTemplate restTemplate, String apiUrl) {

        this.restTemplate = restTemplate;
        this.restTemplate.setErrorHandler(new RestClientErrorHandler());
        this.apiUrl = apiUrl;

        // Set default headers and types to handle
        setHeaders();
        populateManagedTypes();

        // Add message converters
        //restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
    }

    public <T> ResponseEntity<T> deleteRequestWithArgs(Class<T> valueType, String resourceUrl, HashMap args) {
        HttpEntity<String> httpEntity = new HttpEntity<>("parameters", headers);
        try {
            return restTemplate.exchange(apiUrl + resourceUrl, HttpMethod.DELETE, httpEntity, typesManaged.get(valueType), args);
        } catch(RestClientException e) {
            //return new ResponseEntity<T>(HttpStatus.EXPECTATION_FAILED);
            return new ResponseEntity<T>((T) e.getMostSpecificCause().getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public <T> ResponseEntity<T> postRequest(Class<T> valueType, String resourceUrl, MultiValueMap<String, Object> parts) {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts, headers);
        try {
            return  restTemplate.exchange(apiUrl + resourceUrl, HttpMethod.POST, httpEntity, typesManaged.get(valueType));
        } catch(RestClientException e) {
            return new ResponseEntity<T>((T) e.getMostSpecificCause().getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public <T> ResponseEntity<T> postRequestWithArgs(Class<T> valueType, String resourceUrl, MultiValueMap<String, Object> parts, HashMap args) {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, headers);
        try {
            return restTemplate.exchange(apiUrl + resourceUrl, HttpMethod.POST, httpEntity, typesManaged.get(valueType), args);
        } catch(RestClientException e) {
            return new ResponseEntity<T>((T) e.getMostSpecificCause().getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public <T> ResponseEntity<T> getRequest(Class<T> valueType, String resourceUrl) {
        HttpEntity<String> httpEntity = new HttpEntity<>("parameters", headers);
        try {
            return restTemplate.exchange(apiUrl + resourceUrl, HttpMethod.GET, httpEntity, typesManaged.get(valueType));
        } catch(RestClientException e) {
            return new ResponseEntity<T>((T) e.getMostSpecificCause().getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public <T> ResponseEntity<T> getRequestWithArgs(Class<T> valueType, String resourceUrl, HashMap args) {
        HttpEntity<String> httpEntity = new HttpEntity<>("parameters", headers);
        try {
            return restTemplate.exchange(apiUrl + resourceUrl, HttpMethod.GET, httpEntity, typesManaged.get(valueType), args);
        } catch(RestClientException e) {
            return new ResponseEntity<T>((T) e.getMostSpecificCause().getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    private void setHeaders() {
        headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    private void populateManagedTypes() {
        typesManaged = new HashMap<>();
        typesManaged.put(MaasVersion.class, new ParameterizedTypeReference<MaasVersion>(){});
        typesManaged.put(String.class, new ParameterizedTypeReference<String>(){});
        typesManaged.put(String[].class, new ParameterizedTypeReference<String[]>(){});
        typesManaged.put(Machine.class, new ParameterizedTypeReference<Machine>(){});
        typesManaged.put(Machine[].class, new ParameterizedTypeReference<Machine[]>(){});
        typesManaged.put(CommissioningScript.class, new ParameterizedTypeReference<CommissioningScript>(){});
        typesManaged.put(ByteArrayResource.class, new ParameterizedTypeReference<ByteArrayResource>(){});
    }
}
