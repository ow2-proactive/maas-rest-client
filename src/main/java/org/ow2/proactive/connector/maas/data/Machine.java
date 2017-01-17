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
package org.ow2.proactive.connector.maas.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.connector.maas.data.powertype.PowerType;
import org.springframework.util.LinkedMultiValueMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author ActiveEon Team
 * @since 10/01/17
 */
@Getter(AccessLevel.PUBLIC)
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Machine {

    @JsonProperty("blockdevice_set")
    private BlockDevice[] blockDeviceSet;
    @JsonProperty("domain")
    private Domain domain;
    @JsonProperty("cpu_count")
    private Long cpuCount;
    @JsonProperty("tag_names")
    private String[] tagNames;
    @JsonProperty("status_name")
    private String statusName;
    @JsonProperty("zone")
    private Zone zone;
    @JsonProperty("owner")
    private String owner;
    @JsonProperty("swap_size")
    private String swapSize;
    @JsonProperty("hwe_kernel")
    private String hweKernel;
    @JsonProperty("disable_ipv4")
    private Boolean disableIpv4;
    @JsonProperty("interface_set")
    private Interface[] interfaceSet;
    @JsonProperty("status_action")
    private String statusAction;
    @JsonProperty("fqdn")
    private String fqdn;
    @JsonProperty("node_type_name")
    private String nodeTypeName;
    @JsonProperty("power_state")
    private String powerState;
    @JsonProperty("node_type")
    private Long nodeType;
    @JsonProperty("status_message")
    private String statusMessage;
    @JsonProperty("min_hwe_kernel")
    private String minHweKernel;
    @JsonProperty("physicalblockdevice_set")
    private BlockDevice[] physicalBlockDeviceSet;
    @JsonProperty("netboot")
    private Boolean netboot;
    @JsonProperty("virtualblockdevice_set")
    private BlockDevice[] virtualBlockDeviceSet;
    @JsonProperty("system_id")
    private String systemId;
    @JsonProperty("distro_series")
    private String distroSeries;
    @JsonProperty("hostname")
    private String hostname;
    @JsonProperty("address_ttl")
    private String addressTtl;
    @JsonProperty("storage")
    private Double storage;
    @JsonProperty("architecture")
    private String architecture;
    @JsonProperty("osystem")
    private String osSystem;
    @JsonProperty("boot_disk")
    private Object bootDisk;
    @JsonProperty("resource_uri")
    private String resourceUri;
    @JsonProperty("ip_addresses")
    private String[] ipAddresses;
    @JsonProperty("owner_data")
    private Map ownerData;
    @JsonProperty("memory")
    private Long memory;
    @JsonProperty("status")
    private Long status;
    @JsonProperty("power_type")
    private String powerType;
    @JsonProperty("special_filesystems")
    private String[] specialFilesystems;
    @JsonProperty("boot_interface")
    private BootInterface bootInterface;

    public Machine(Builder builder) {
        architecture = builder.architecture;
        hostname = builder.hostName;
        minHweKernel = builder.minHweKernel;
        domain = builder.domain;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (!Machine.class.isAssignableFrom(obj.getClass())) { return false; }
        final Machine other = (Machine) obj;
        return this.systemId.equals(other.systemId);
    }

    public static class Builder {

        private String architecture;
        private String minHweKernel;
        private String subArchitecture;
        private List<String> macAddresses;
        private String hostName;
        private Domain domain;
        private PowerType powerType;

        public Builder() {
            macAddresses = new ArrayList<>();
        }

        public Builder(String architecture, String macAddress) {
            this.macAddresses = new ArrayList<>();
            this.macAddresses.add(macAddress);
            this.architecture = architecture;
        }

        public Builder architecture(String architecture) {
            this.architecture = architecture;
            return this;
        }

        public Builder minHweKernel(String minHweKernel) {
            this.minHweKernel = minHweKernel;
            return this;
        }

        public Builder subArchitecture(String subArchitecture) {
            this.subArchitecture = subArchitecture;
            return this;
        }

        public Builder macAddress(String macAddress) {
            this.macAddresses.add(macAddress);
            return this;
        }

        public Builder hostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public Builder domain(Domain domain) {
            this.domain = domain;
            return this;
        }

        public Builder powerType(PowerType powerType) {
            this.powerType = powerType;
            return this;
        }

        /**
         * Build as a Machine object.
         * <p>
         * There is currently no full support for builder<->object conversion as
         * parameters differs from get to post in MAAS API implementation.
         *
         * @return a Machine object
         * @see <a href="https://maas.ubuntu.com/docs/api.html">MAAS API documentation</a>
         * <p>
         * Arguments generation must thus be done through the builder
         * @see #buildAsArgs
         */
        public Machine build() {
            return new Machine(this);
        }

        public LinkedMultiValueMap<String, Object> buildAsArgs() {
            LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
            if (architecture != null) {
                parts.add("architecture", architecture);
            }
            if (minHweKernel != null) {
                parts.add("min_hwe_kernel", minHweKernel);
            }
            if (subArchitecture != null) {
                parts.add("subarchitecture", subArchitecture);
            }
            for (String macAddress : macAddresses) {
                parts.add("mac_addresses", macAddress);
            }
            if (hostName != null) {
                parts.add("hostname", hostName);
            }
            if (domain != null) {
                parts.add("domain", domain);
            }
            if (powerType != null) {
                parts.add("power_type", powerType.getType());
                parts.putAll(powerType.getMap());
            }
            return parts;
        }
    }
}
