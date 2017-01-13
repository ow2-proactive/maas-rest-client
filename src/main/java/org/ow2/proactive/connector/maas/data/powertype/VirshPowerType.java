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
public class VirshPowerType implements PowerType {

    private final String powerAddress;
    private final String powerId;
    private final String password;

    public VirshPowerType(Builder builder) {
        powerAddress = builder.powerAddress;
        powerId = builder.powerId;
        password = builder.password;
    }

    public LinkedMultiValueMap<String, Object> getMap() {
        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
        if (powerAddress != null) {
            parts.add(PowerType.getArgLabel("power_address"), powerAddress);
        }
        if (powerId != null) {
            parts.add(PowerType.getArgLabel("power_id"), powerId);
        }
        if (password != null) {
            parts.add(PowerType.getArgLabel("power_pass"), password);
        }
        return parts;
    }

    @Override
    public String getType() {
        return PowerType.VIRSH;
    }

    public static class Builder {

        private String powerAddress;
        private String powerId;
        private String password;

        public Builder(String id, String address) {
            this.powerId = id;
            this.powerAddress = address;
        }

        public Builder() {}

        public Builder powerAddress(String address) {
            this.powerAddress = address;
            return this;
        }

        public Builder powerId(String id) {
            this.powerId = id;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public VirshPowerType build() {
            return new VirshPowerType(this);
        }
    }
}
