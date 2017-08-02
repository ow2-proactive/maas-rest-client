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
import org.ow2.proactive.connector.maas.data.MaasVersion;
import org.ow2.proactive.connector.maas.data.Machine;
import org.ow2.proactive.connector.maas.data.Tag;
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
        RestTemplate restTemplate = new OauthClientConfig().restTemplate(consumerKey,
                                                                         "",
                                                                         accessKey,
                                                                         accessSecret,
                                                                         ignoreHttpsCert);
        restClient = new RestClient(restTemplate, apiUrl);

        // Try to retrieve a config option
        if (!tryToConnect()) {
            throw new RemoteConnectFailureException("Remote authentication failure",
                                                    new Throwable("Wrong API key content"));
        }
    }

    public MaasVersion getMaasVersion() {
        return restClient.getRequest(MaasVersion.class, "/version/").getBody();
    }

    public String getMaasConfig(String configName) {
        return restClient.getRequest(String.class, "/maas/?op=get_config&name=" + configName).getBody();
    }

    public List<Machine> getMachines() {
        ResponseEntity<Machine[]> response = restClient.getRequest(Machine[].class, "/machines/");
        if (RestClientErrorHandler.hasError(response.getStatusCode())) {
            return null;
        }
        return Arrays.asList(response.getBody());
    }

    public List<Machine> getMachinesByTagName(String tagName) {
        HashMap<String, String> args = new HashMap<>();
        args.put("name", tagName);
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        ResponseEntity<Machine[]> response = restClient.getRequestWithArgs(Machine[].class,
                                                                           "/tags/{name}/?op=machines",
                                                                           args);
        if (RestClientErrorHandler.hasError(response.getStatusCode())) {
            return null;
        }
        return Arrays.asList(response.getBody());
    }

    /**
     * Retrieve the list of machines tagged with the provided tag.
     * Note: The tag name must be unique, so there is no need to check the description field of the tag.
     *
     * @param tag   The tag with which the machines must be tagged
     * @return      The list of desired tagged machines
     */
    public List<Machine> getMachinesByTag(Tag tag) {
        return getMachinesByTagName(tag.getName());
    }

    public List<Machine> getAllocatedMachines() {
        ResponseEntity<Machine[]> response = restClient.getRequest(Machine[].class, "/machines/?op=list_allocated");
        if (RestClientErrorHandler.hasError(response.getStatusCode())) {
            return null;
        }
        return Arrays.asList(response.getBody());
    }

    public Machine getMachineById(String systemId) {
        HashMap<String, String> args = new HashMap<>();
        args.put("system_id", systemId);
        return restClient.getRequestWithArgs(Machine.class, "/machines/{system_id}/", args).getBody();
    }

    public Machine getMachineByName(String hostName) {
        ResponseEntity<Machine[]> response = restClient.getRequest(Machine[].class, "/machines/?hostname=" + hostName);
        if (response.getBody() != null && response.getBody().length > 0) {
            return response.getBody()[0];
        }
        return null;
    }

    public Machine getMachineByMacAddress(String macAddress) {
        return restClient.getRequest(Machine.class, "/machines/?mac_address=" + macAddress).getBody();
    }

    public Machine createMachine(Machine.Builder machineBuilder) {
        return restClient.postRequest(Machine.class, "/machines/", machineBuilder.buildAsArgs()).getBody();
    }

    public List<String> releaseMachines(String... systemIds) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("machines", Arrays.asList(systemIds));
        ResponseEntity response = restClient.postRequest(String[].class, "/machines/?op=release", parts);
        if (RestClientErrorHandler.hasError(response.getStatusCode())) {
            return null;
        }
        return Arrays.asList((String[]) response.getBody());
    }

    public boolean releaseMachineById(String systemId) {
        return releaseMachineById(systemId, null, false, false, false);
    }

    public boolean releaseMachineById(String systemId, String comment) {
        return releaseMachineById(systemId, comment, false, false, false);
    }

    public boolean releaseMachineById(String systemId, String comment, boolean eraseDisk, boolean secureErase,
            boolean quickErase) {
        HashMap<String, String> args = new HashMap<>();
        args.put("system_id", systemId);
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        if (comment != null && !comment.isEmpty()) {
            parts.add("comment", comment);
        }
        parts.add("erase", eraseDisk);
        parts.add("secure_erase", secureErase);
        parts.add("quick_erase", quickErase);
        ResponseEntity response = restClient.postRequestWithArgs(String[].class,
                                                                 "/machines/{system_id}/?op=release",
                                                                 parts,
                                                                 args);
        return !RestClientErrorHandler.hasError(response.getStatusCode()) && response.getBody().equals(systemId);
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
        HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return name;
            }
        }, dataHeaders);

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("name", namePart);
        parts.add("content", filePart);
        return restClient.postRequest(CommissioningScript.class, "/commissioning-scripts/", parts).getBody();
    }

    public Machine commissionMachine(String systemId) {
        return commissionMachine(systemId, true, false, false);
    }

    public Machine allocateMachineById(String systemId) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("system_id", systemId);
        return restClient.postRequest(Machine.class, "/machines/?op=allocate", parts).getBody();
    }

    public Machine allocateMachineByHostname(String hostname) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("name", hostname);
        return restClient.postRequest(Machine.class, "/machines/?op=allocate", parts).getBody();
    }

    public Machine allocateMachineByResources(int cpu_count, int mem, String arch) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("cpu_count", cpu_count);
        parts.add("mem", mem);
        parts.add("arch", arch);
        return restClient.postRequest(Machine.class, "/machines/?op=allocate", parts).getBody();
    }

    public Machine allocateMachineByResources(int cpu_count, int mem) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("cpu_count", cpu_count);
        parts.add("mem", mem);
        try {
            return restClient.postRequest(Machine.class, "/machines/?op=allocate", parts).getBody();
        } catch (ClassCastException e) {
            return null;
            //e.printStackTrace();
        }
    }

    public Machine commissionMachine(String systemId, boolean enableSSH, boolean skipNetworking, boolean skipStorage) {
        HashMap<String, String> args = new HashMap<>();
        args.put("system_id", systemId);
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("enable_ssh", enableSSH);
        parts.add("skip_networking", skipNetworking);
        parts.add("skip_storage", skipStorage);
        return restClient.postRequestWithArgs(Machine.class, "/machines/{system_id}/?op=commission", parts, args)
                         .getBody();
    }

    public Machine _commissionMachineBase64(String systemId, boolean enableSSH, boolean skipNetworking,
            boolean skipStorage) {
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

        return restClient.postRequestWithArgs(Machine.class, "/machines/{system_id}/?op=commission", parts, args)
                         .getBody();
    }

    public Machine deployMachine(String systemId, String userData) {
        return deployMachine(systemId, userData, null, null, null);
    }

    public Machine deployMachine(String systemId) {
        return deployMachine(systemId, null, null, null, null);
    }

    public Machine deployMachine(String systemId, String userData, String distroSeries, String hweKernel,
            String comment) {
        HashMap<String, String> args = new HashMap<>();
        args.put("system_id", systemId);

        /*
         * Strangely, these extra - multi part - headers are not necessary to embed base64 encoded
         * content
         * HttpHeaders partHeaders = new HttpHeaders();
         * partHeaders.setContentType(MediaType.TEXT_PLAIN);
         * partHeaders.set("Content-Transfer-Encoding", "base64");
         * partHeaders.set("MIME-Version", "1.0");
         * partHeaders.set("Content-Disposition", "form-data; name=\"user_data\"");
         * partHeaders.set("Charset", "utf-8");
         */

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        if (userData != null && !userData.isEmpty()) {
            parts.add("user_data", encodeToBase64(userData));
        }
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

    public boolean powerOffMachine(String systemId) {
        return powerOffMachine(systemId, "hard");
    }

    public boolean powerOffMachine(String systemId, String stopMode) {
        return powerOffMachine(systemId, stopMode, null);
    }

    public boolean powerOffMachine(String systemId, String stopMode, String comment) {
        HashMap<String, String> args = new HashMap<>();
        args.put("system_id", systemId);
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        if (stopMode != null && !stopMode.isEmpty()) {
            parts.add("stopMode", stopMode);
        }
        if (comment != null && !comment.isEmpty()) {
            parts.add("comment", comment);
        }
        ResponseEntity response = restClient.postRequestWithArgs(Machine.class,
                                                                 "/machines/{system_id}/?op=power_off",
                                                                 parts,
                                                                 args);
        return !RestClientErrorHandler.hasError(response.getStatusCode());
    }

    public boolean powerOnMachine(String systemId) {
        return powerOnMachine(systemId, null);
    }

    // Missing 'user_data' parameter that provides metadata access from the new machine
    public boolean powerOnMachine(String systemId, String comment) {
        HashMap<String, String> args = new HashMap<>();
        args.put("system_id", systemId);
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        if (comment != null && !comment.isEmpty()) {
            parts.add("comment", comment);
        }
        ResponseEntity response = restClient.postRequestWithArgs(Machine.class,
                                                                 "/machines/{system_id}/?op=power_on",
                                                                 parts,
                                                                 args);
        return !RestClientErrorHandler.hasError(response.getStatusCode());
    }

    public List<Tag> getTags() {
        ResponseEntity<Tag[]> response = restClient.getRequest(Tag[].class, "/tags/");
        if (RestClientErrorHandler.hasError(response.getStatusCode())) {
            return null;
        }
        return Arrays.asList(response.getBody());
    }

    public boolean createTagIfNotExists(String name, String description) {
        if (getTags().stream().anyMatch(tag -> tag.getName().equals(name))) {
            return false;
        }
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("name", name);
        if (description != null && !description.isEmpty()) {
            parts.add("comment", description);
        }
        // Set kernel options as None to avoid overriding options by an empty string
        parts.add("kernel_opts", null);
        ResponseEntity response = restClient.postRequest(CommissioningScript.class, "/tags/", parts);
        return !RestClientErrorHandler.hasError(response.getStatusCode());
    }

    public void updateTagNodesMapping(String name) {
        HashMap<String, String> args = new HashMap<>();
        args.put("name", name);
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        restClient.postRequestWithArgs(String.class, "/tags/{name}/op=rebuild", parts, args);
    }

    public boolean addTagToMachines(String tagName, String systemId, String... systemIds) {
        HashMap<String, String> args = new HashMap<>();
        args.put("name", tagName);
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("add", systemId);
        Arrays.stream(systemIds).forEach(additionalSystemId -> parts.add("add", additionalSystemId));
        ResponseEntity response = restClient.postRequestWithArgs(String.class,
                                                                 "/tags/{name}/?op=update_nodes",
                                                                 parts,
                                                                 args);
        return !RestClientErrorHandler.hasError(response.getStatusCode());
    }

    private boolean checkCredentials(String token) {
        String[] tokenParts = token.split(":");
        return tokenParts.length == 3;
    }

    private boolean tryToConnect() {
        return getMaasConfig("maas_name") != null;
    }

    private String encodeToBase64(Object src) {
        return Base64.getMimeEncoder(76, "\r\n".getBytes()).encodeToString(src.toString().getBytes());
    }
}
