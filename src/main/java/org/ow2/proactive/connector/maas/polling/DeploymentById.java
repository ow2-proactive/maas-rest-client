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
package org.ow2.proactive.connector.maas.polling;

import java.util.List;
import java.util.concurrent.Callable;

import org.ow2.proactive.connector.maas.MaasClient;
import org.ow2.proactive.connector.maas.data.Machine;
import org.ow2.proactive.connector.maas.data.Tag;

/**
 * @author ActiveEon Team
 * @since 17/01/17
 */
public class DeploymentById implements Callable<Machine> {

    private MaasClient maasClient;
    private String systemId;
    private String userData;
    private List<Tag> tags;

    public DeploymentById(MaasClient maasClient, String systemId, String userData, List<Tag> tags) {
        this.maasClient = maasClient;
        this.systemId = systemId;
        this.userData = userData;
        this.tags = tags;
    }

    @Override
    public Machine call() throws Exception {

        // Acquire/Allocate the new machine
        Machine selectedMachine = maasClient.allocateMachineById(systemId);
        if (selectedMachine == null) {
            return null;
        }
        do {Thread.sleep(MaasClientPollingService.POLLING_INTERVAL); }
        while (maasClient.getMachineById(systemId).getStatus() != Machine.ALLOCATED);

        // Put tags
        tags.forEach(tag -> {
            maasClient.createTagIfNotExists(tag.getName(), tag.getComment());
            maasClient.addTagToMachines(tag.getName(), systemId);
        });

        // Deploy OS
        selectedMachine = maasClient.deployMachine(systemId, userData);
        if (selectedMachine == null) {
            return null;
        }
        do {Thread.sleep(MaasClientPollingService.POLLING_INTERVAL);}
        while (maasClient.getMachineById(systemId).getStatus() != Machine.DEPLOYED);

        return selectedMachine;
    }
}
