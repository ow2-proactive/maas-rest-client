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
package org.ow2.proactive.connector.maas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.ow2.proactive.connector.maas.data.CommissioningScript;
import org.ow2.proactive.connector.maas.data.Machine;
import org.ow2.proactive.connector.maas.oauth.OauthClientConfig;
import org.ow2.proactive.connector.maas.rest.RestClient;
import org.ow2.proactive.connector.maas.rest.RestClientErrorHandler;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @author Vincent Kherbache
 * @since 09/01/17
 */
public class MaasClient {

    private RestClient restClient;

    public MaasClient(String apiUrl, String token, boolean ignoreHttpsCert) {

        // Check API key format
        if (!checkCredentials(token)) {
            throw new RemoteConnectFailureException("Unable to parse API key", new Throwable("Wrong API key format"));
        }

        // Parse API token
        String[] tokenParts = token.split(":");
        String consumerKey = tokenParts[0];
        String accessKey = tokenParts[1];
        String accessSecret = tokenParts[2];
        RestTemplate restTemplate = new OauthClientConfig().restTemplate(consumerKey, "", accessKey, accessSecret, ignoreHttpsCert);
        restClient= new RestClient(restTemplate, apiUrl);

        // Try to retrieve maas_name config parameter
        if (!tryToConnect()) {
            throw new RemoteConnectFailureException("Remote authentication failure", new Throwable("Wrong API key content"));
        }
    }

    public String getMaasConfig(String configName) {
        return restClient.getRequest(String.class, "/maas/?op=get_config&name=" + configName).getBody();
    }

    public List<Machine> getMachines() {
        ResponseEntity response = restClient.getRequest(Machine[].class, "/machines/");
        if (RestClientErrorHandler.hasError(response.getStatusCode())) {
            return null;
        }
        return Arrays.asList(restClient.getRequest(Machine[].class, "/machines/").getBody());
    }

    public Machine getMachineById(String systemId) {
        HashMap<String, String> args = new HashMap<>();
        args.put("system_id", systemId);
        return restClient.getRequestWithArgs(Machine.class, "/machines/{system_id}/", args).getBody();
    }

    public Machine createMachine(Machine.Builder machineBuilder) {
        return restClient.postRequest(Machine.class, "/machines/", machineBuilder.buildAsArgs()).getBody();
        //return !RestClientErrorHandler.hasError(response.getStatusCode());
        //return (String)response.getBody();
    }

    public boolean deleteMachine(String systemId) {
        HashMap<String, String> args = new HashMap<>();
        args.put("system_id", systemId);
        ResponseEntity response = restClient.deleteRequestWithArgs(String.class, "/machines/{system_id}/", args);
        return !RestClientErrorHandler.hasError(response.getStatusCode());
    }

    public List<String> getCommissioningScripts() {
        return Arrays.asList(restClient.getRequest(String[].class, "/commissioning-scripts/").getBody());
    }

    public ByteArrayResource getCommissioningScriptByName(String name) {
        HashMap<String, String> args = new HashMap<>();
        args.put("name", name);
        return restClient.getRequestWithArgs(ByteArrayResource.class, "/commissioning-scripts/{name}", args).getBody();
    }

    public CommissioningScript postCommissioningScript(String file) throws IOException {
        Path path = Paths.get(file);
        byte[] fileData = Files.readAllBytes(path);
        String fileName = path.getFileName().toString();

        return postCommissioningScript(fileData, fileName);
    }

    public CommissioningScript postCommissioningScript(byte[] data, String name) {

        HttpHeaders nameHeaders = new HttpHeaders();
        nameHeaders.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> namePart = new HttpEntity<>(name, nameHeaders);

        HttpHeaders dataHeaders = new HttpHeaders();
        dataHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(new ByteArrayResource(data){
            @Override
            public String getFilename(){
                return name;
            }
        }, dataHeaders);

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("name", namePart);
        parts.add("content", filePart);
        return restClient.postRequest(CommissioningScript.class, "/commissioning-scripts/", parts).getBody();
    }

    public String commissionMachine(String systemId) {
        return commissionMachine(systemId, true, false, false);
    }

    public String commissionMachine(String systemId, boolean enableSSH, boolean skipNetworking, boolean skipStorage) {
        HashMap<String, String> args = new HashMap<>();
        args.put("system_id", systemId);

        // Mimics the CLI behavior by using base64 encoding (waiting for an API upgrade)
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.TEXT_PLAIN);
        partHeaders.set("Content-Transfer-Encoding", "base64");
        partHeaders.set("MIME-Version", "1.0");
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("enable_ssh", new HttpEntity<>(encodeToBase64(enableSSH), partHeaders));
        parts.add("skip_networking", new HttpEntity<>(encodeToBase64(skipNetworking), partHeaders));
        parts.add("skip_storage", new HttpEntity<>(encodeToBase64(skipStorage), partHeaders));

        return restClient.postRequestWithArgs(Machine.class, "/machines/{system_id}/?op=commission", parts, args).getBody();
    }

    public Machine deployMachine(String systemId) {
        return deployMachine(systemId, null, null, null);
    }

    // Missing 'user_data' parameter that provides metadata access from the new deployed machine
    public Machine deployMachine(String systemId, String distroSeries, String hweKernel, String comment) {
        HashMap<String, String> args = new HashMap<>();
        args.put("system_id", systemId);
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        if (distroSeries != null && !distroSeries.isEmpty()) {
            parts.add("distro_series", distroSeries);
        }
        if (hweKernel != null && !hweKernel.isEmpty()) {
            parts.add("hwe_kernel", hweKernel);
        }
        if (comment != null && !comment.isEmpty()) {
            parts.add("comment", comment);
        }

        return restClient.postRequestWithArgs(Machine.class, "/machines/{system_id}/?op=deploy", parts, args).getBody();
    }

    private boolean checkCredentials(String token) {
        String[] tokenParts = token.split(":");
        return tokenParts.length == 3;
    }

    private boolean tryToConnect() {
        return getMaasConfig("maas_name") != null;
    }

    private String encodeToBase64(Object src) {
        return Base64.getMimeEncoder().encodeToString(src.toString().getBytes());
    }
}
