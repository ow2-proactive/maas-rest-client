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

import java.util.concurrent.Callable;

import org.ow2.proactive.connector.maas.MaasClient;
import org.ow2.proactive.connector.maas.data.Machine;

/**
 * @author ActiveEon Team
 * @since 17/01/17
 */
public class DeploymentPolling implements Callable<Machine> {

    // Poll every X sec
    private static final int POLLING_INTERVAL = 5 * 1000;
    private MaasClient maasClient;
    //private String systemId;
    private int cpu, ram;

    public DeploymentPolling(MaasClient maasClient, int cpu, int ram) { //} String systemId) {
        this.maasClient = maasClient;
        this.cpu = cpu;
        this.ram = ram;
        //this.systemId = systemId;
    }

    public String _call() throws Exception {

        /* Commission the new machine
        maasClient.commissionMachine(systemId);
        do {Thread.sleep(POLLING_INTERVAL);}
        while (maasClient.getMachineById(systemId).getStatus() != Machine.READY);

        // Acquire/Allocate the new machine
        maasClient.allocateMachineById(systemId);
        do {Thread.sleep(POLLING_INTERVAL);}
        while (maasClient.getMachineById(systemId).getStatus() != Machine.ALLOCATED);

        // Deploy OS
        maasClient.deployMachine(systemId);
        do {Thread.sleep(POLLING_INTERVAL);}
        while (maasClient.getMachineById(systemId).getStatus() != Machine.DEPLOYED);
        */

        return "OK";
    }


    @Override
    public Machine call() throws Exception {

        // Acquire/Allocate the new machine
        Machine selectedMachine = maasClient.allocateMachineByResource(cpu, ram);

        if (selectedMachine == null) {
            return null;
        }

        do {Thread.sleep(POLLING_INTERVAL); }
        while (maasClient.getMachineById(selectedMachine.getSystemId()).getStatus() != Machine.ALLOCATED);

        /* Acquire/Allocate the new machine
        maasClient.allocateMachineById(selectedMachine.getSystemId());
        do {Thread.sleep(POLLING_INTERVAL);}
        while (maasClient.getMachineById(selectedMachine.getSystemId()).getStatus() != Machine.ALLOCATED);
        */

        // Deploy OS
        /*maasClient.deployMachine(selectedMachine.getSystemId());
        do {Thread.sleep(POLLING_INTERVAL);}
        while (maasClient.getMachineById(selectedMachine.getSystemId()).getStatus() != Machine.DEPLOYED);
*/
        return selectedMachine;
    }
}
