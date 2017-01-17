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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.ow2.proactive.connector.maas.MaasClient;

/**
 * @author ActiveEon Team
 * @since 17/01/17
 */
public class PollingService {

    MaasClient maasClient;
    ExecutorService executor;

    public PollingService(MaasClient maasClient, int nbThreads) {
        this.maasClient = maasClient;
        executor = Executors.newFixedThreadPool(nbThreads);
    }

    public Future<String> deployMachine(String systemId) {
        Callable<String> deploymentPolling = new DeploymentPolling(maasClient, systemId);
        Future<String> future = executor.submit(deploymentPolling);
        return future;
    }

    public void shutdown() {
        executor.shutdown();
    }
}
