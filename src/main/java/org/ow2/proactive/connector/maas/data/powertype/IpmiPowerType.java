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
package org.ow2.proactive.connector.maas.data.powertype;

import org.springframework.util.LinkedMultiValueMap;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * @author ActiveEon Team
 * @since 11/01/17
 */
@Getter(AccessLevel.PUBLIC)
public class IpmiPowerType implements PowerType {

    private String powerDriver;
    private String powerAddress;
    private String powerUser;
    private String powerPass;
    private String macAddress;

    public IpmiPowerType(Builder builder) {
        powerDriver = builder.powerDriver;
        powerAddress = builder.powerAddress;
        powerUser = builder.powerUser;
        powerPass = builder.powerPass;
        macAddress = builder.macAddress;
    }

    public LinkedMultiValueMap<String, Object> getMap() {
        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        if (powerDriver != null) {
            parts.add(PowerType.getArgLabel("power_driver"), powerDriver);
        }
        if (powerAddress != null) {
            parts.add(PowerType.getArgLabel("power_address"), powerAddress);
        }
        if (powerUser != null) {
            parts.add(PowerType.getArgLabel("power_user"), powerUser);
        }
        if (powerPass != null) {
            parts.add(PowerType.getArgLabel("power_pass"), powerPass);
        }
        if (macAddress != null) {
            parts.add(PowerType.getArgLabel("mac_address"), macAddress);
        }
        return parts;
    }

    @Override
    public String getType() {
        return PowerType.IPMI;
    }

    public static class Builder {

        private String powerDriver;
        private String powerAddress;
        private String powerUser;
        private String powerPass;
        private String macAddress;

        public Builder() {}

        public Builder powerDriver(String powerDriver) {
            this.powerDriver = powerDriver;
            return this;
        }

        public Builder powerAddress(String powerAddress) {
            this.powerAddress = powerAddress;
            return this;
        }

        public Builder powerUser(String powerUser) {
            this.powerUser = powerUser;
            return this;
        }

        public Builder powerPass(String powerPass) {
            this.powerPass = powerPass;
            return this;
        }

        public Builder macAddress(String macAddress) {
            this.macAddress = macAddress;
            return this;
        }

        public IpmiPowerType build() {
            return new IpmiPowerType(this);
        }
    }
}
