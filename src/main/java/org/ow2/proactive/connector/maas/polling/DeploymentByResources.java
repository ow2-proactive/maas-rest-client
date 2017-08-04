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
public class DeploymentByResources implements Callable<Machine> {

    // Poll every X sec
    private MaasClient maasClient;

    private int cpu, ram;

    private String userData;

    private List<Tag> tags;

    private static final CharSequence INSTANCE_ID_PATTERN = "<INSTANCE_ID>";

    public DeploymentByResources(MaasClient maasClient, int cpu, int ram, String userData, List<Tag> tags) {
        this.maasClient = maasClient;
        this.cpu = cpu;
        this.ram = ram;
        this.userData = userData;
        this.tags = tags;
    }

    @Override
    public Machine call() throws Exception {

        // Acquire/Allocate the new machine
        Machine selectedMachine = maasClient.allocateMachineByResources(cpu, ram);
        if (selectedMachine == null) {
            return null;
        }
        String systemId = selectedMachine.getSystemId();
        do {
            Thread.sleep(MaasClientPollingService.POLLING_INTERVAL);
        } while (maasClient.getMachineById(systemId).getStatus() != Machine.ALLOCATED);

        // Replace the connector IaaS instance ID if provided
        userData = userData.replace(INSTANCE_ID_PATTERN, systemId);

        // Put tags
        tags.forEach(tag -> {
            maasClient.createTagIfNotExists(tag.getName(), tag.getComment());
            maasClient.addTagToMachines(tag.getName(), systemId);
        });

        // Deploy OS
        selectedMachine = maasClient.deployMachine(systemId, userData);
        do {
            Thread.sleep(MaasClientPollingService.POLLING_INTERVAL);
        } while (maasClient.getMachineById(systemId).getStatus() != Machine.DEPLOYED);

        return selectedMachine;
    }
}
