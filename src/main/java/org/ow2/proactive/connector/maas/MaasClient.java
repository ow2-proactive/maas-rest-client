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
import java.util.HashMap;
import java.util.List;

import org.ow2.proactive.connector.maas.data.CommissioningScript;
import org.ow2.proactive.connector.maas.data.Machine;
import org.ow2.proactive.connector.maas.oauth.OauthClientConfig;
import org.ow2.proactive.connector.maas.oauth.OauthConnectionService;
import org.ow2.proactive.connector.maas.oauth.OauthConnectionServiceImpl;
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

        OauthConnectionService oauthConnectionService = new OauthConnectionServiceImpl();
        RestTemplate restTemplate = new OauthClientConfig().restTemplate(token.split(":"), ignoreHttpsCert);
        restClient= new RestClient(restTemplate, apiUrl);

        // Try to connect or return an error
        if (getMachines() == null) {
            throw new RemoteConnectFailureException("Authentication failure", new Throwable("Wrong API key"));
        }
    }

    public List<Machine> getMachines() {
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

        // Mimics the CLI behavior (waiting for an API upgrade)
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.TEXT_PLAIN);
        partHeaders.set("Content-Transfer-Encoding", "base64");
        partHeaders.set("MIME-Version", "1.0");
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("enable_ssh", new HttpEntity<>(Base64.getMimeEncoder().encodeToString(Boolean.toString(enableSSH).getBytes()), partHeaders));
        parts.add("skip_networking", new HttpEntity<>(Base64.getMimeEncoder().encodeToString(Boolean.toString(skipNetworking).getBytes()), partHeaders));
        parts.add("skip_storage", new HttpEntity<>(Base64.getMimeEncoder().encodeToString(Boolean.toString(skipStorage).getBytes()), partHeaders));

        return restClient.postRequestWithArgs(String.class, "/machines/{system_id}/?op=commission", parts, args).getBody();
    }

    public Machine deployMachine(String systemId) {
        return deployMachine(systemId, null, null, null);
    }

    public Machine deployMachine(String systemId, String distroSeries, String hweKernel, String comment) {
        HashMap<String, String> args = new HashMap<>();
        args.put("system_id", systemId);

        // Mimics the CLI behavior (waiting for an API upgrade)
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.TEXT_PLAIN);
        partHeaders.set("Content-Transfer-Encoding", "base64");
        partHeaders.set("MIME-Version", "1.0");
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        if (distroSeries != null) {
            parts.add("distro_series", new HttpEntity<>(Base64.getMimeEncoder().encodeToString(distroSeries.getBytes()), partHeaders));
        }
        if (hweKernel != null) {
            parts.add("hwe_kernel", new HttpEntity<>(Base64.getMimeEncoder().encodeToString(hweKernel.getBytes()), partHeaders));
        }
        if (comment != null) {
            parts.add("comment", new HttpEntity<>(Base64.getMimeEncoder().encodeToString(comment.getBytes()), partHeaders));
        }

        return restClient.postRequestWithArgs(Machine.class, "/machines/{system_id}/?op=deploy", parts, args).getBody();
    }
}
