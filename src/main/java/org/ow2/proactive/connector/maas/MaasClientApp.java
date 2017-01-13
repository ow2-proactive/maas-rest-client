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

import java.util.List;

import org.ow2.proactive.connector.maas.data.Machine;
import org.ow2.proactive.connector.maas.data.powertype.VirshPowerType;

/**
 * @author ActiveEon Team
 * @since 09/01/17
 */
//@EnableAsync
public class MaasClientApp {

    //private static final org.slf4j.Logger log = LoggerFactory.getLogger(MaasClientApp.class);

    public static void main(String args[]) {

        /*
        if (args.length < 2) {
            System.out.println("Usage: java " + MaasClientApp.class.getName() + " [url] [token]");
            System.exit(-1);
        }
        String url = args[0];
        String token = args[1];
        */

        String url = "***REMOVED***";
        String token = "***REMOVED***";

        MaasClient client = new MaasClient(url, token);

        /*
        Machine machine = client.getMachineById("fgwdm4");
        System.out.print(machine);
        */

        Machine machine = client.createMachine(new Machine.Builder()
                .hostName("test")
                .macAddress("54:52:00:11:22:33")
                .architecture("amd64")
                .subArchitecture("generic")
                .powerType(new VirshPowerType.Builder()
                        .powerAddress("qemu://system/")
                        .powerId("test")
                        .build()
                )
        );
        System.out.print(machine);

        List<Machine> machines = client.getMachines();
        //System.out.print(machines);

        /*
        List<String> scripts = client.getCommissioningScripts();
        System.out.print(scripts);

        CommissioningScript script = null;
        try {
            script = client.postCommissioningScript("/home/vinseon/scripts/clean_iptables");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print(script);

        ByteArrayResource script = client.getCommissioningScriptByName("clean_iptables");
        System.out.print(script.getByteArray());
        */

        /*
        String response = client.commissionMachine("r6k3b6");
        Machine machine = client.deployMachine("fgwdm4");
        System.out.print(machine);
        */

        boolean ok = client.deleteMachine(machines.get(machines.size()-1).getSystemId());
        System.out.print(ok);
    }
}
