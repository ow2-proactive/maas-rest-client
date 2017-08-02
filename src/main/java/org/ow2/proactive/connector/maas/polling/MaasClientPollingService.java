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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ow2.proactive.connector.maas.MaasClient;
import org.ow2.proactive.connector.maas.data.Machine;
import org.ow2.proactive.connector.maas.data.Tag;

/**
 * @author ActiveEon Team
 * @since 17/01/17
 */
public class MaasClientPollingService {

    private final static int DEFAULT_TIMEOUT = 15;
    final static int POLLING_INTERVAL = 5 * 1000;

    private MaasClient maasClient;
    private ScheduledExecutorService executor;

    public MaasClientPollingService(MaasClient maasClient, int nbThreads) {
        this.maasClient = maasClient;
        executor = Executors.newScheduledThreadPool(nbThreads);
    }

    public Future<Machine> deployMachine(String systemId, String userData, List<Tag> tags) {
        return deployMachineById(systemId, userData, tags, DEFAULT_TIMEOUT);
    }

    public Future<Machine> deployMachine(int cpu, int ram, String userData, List<Tag> tags) {
        return deployMachineByResources(cpu, ram, userData, tags, DEFAULT_TIMEOUT);
    }

    public Future<Machine> deployMachineById(String systemId, String userData, List<Tag> tags, int timeoutMinutes) {
        Callable<Machine> deploymentPolling = new DeploymentById(maasClient, systemId, userData, tags);

        Future<Machine> future = executor.submit(deploymentPolling);
        executor.schedule(() -> {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }, timeoutMinutes, TimeUnit.MINUTES);

        return future;
    }

    public Future<Machine> deployMachineByResources(int cpu, int ram, String userData, List<Tag> tags) {
        return deployMachineByResources(cpu, ram, userData, tags, DEFAULT_TIMEOUT);
    }

    public Future<Machine> deployMachineByResources(int cpu, int ram, String userData, List<Tag> tags, int timeoutMinutes) {
        Callable<Machine> deploymentPolling = new DeploymentByResources(maasClient, cpu, ram, userData, tags);

        Future<Machine> future = executor.submit(deploymentPolling);
        executor.schedule(() -> {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }, timeoutMinutes, TimeUnit.MINUTES);

        return future;
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
