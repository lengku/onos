/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.mcast.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.onlab.packet.IpAddress;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.mcast.api.McastRoute;
import org.onosproject.mcast.api.McastRouteData;
import org.onosproject.mcast.api.MulticastRouteService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.HostId;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Installs a source, multicast group flow.
 */
@Command(scope = "onos", name = "mcast-host-join",
        description = "Installs a source, multicast group flow")
public class McastHostJoinCommand extends AbstractShellCommand {

    // Format for group line
    private static final String FORMAT_MAPPING = "Added the mcast route: " +
            "origin=%s, group=%s, source=%s";

    @Option(name = "-sAddr", aliases = "--sourceAddress",
            description = "IP Address of the multicast source. '*' can be used for any source (*, G) entry",
            valueToShowInHelp = "1.1.1.1",
            required = true, multiValued = false)
    String sAddr = null;

    @Option(name = "-gAddr", aliases = "--groupAddress",
            description = "IP Address of the multicast group",
            valueToShowInHelp = "224.0.0.0",
            required = true, multiValued = false)
    String gAddr = null;

    @Option(name = "-srcs", aliases = "--sources",
            description = "Ingress port of:XXXXXXXXXX/XX",
            valueToShowInHelp = "of:0000000000000001/1",
            multiValued = true)
    String[] sources = null;

    @Option(name = "-sinks",
            aliases = "--hostsinks",
            description = "Host sink format: MAC/VLAN",
            valueToShowInHelp = "00:00:00:00:00:00/None",
            multiValued = true)
    String[] hosts = null;

    @Override
    protected void execute() {
        MulticastRouteService mcastRouteManager = get(MulticastRouteService.class);

        IpAddress sAddrIp = null;
        //If the source Ip is * we want ASM so we leave it as null and the route will have it as an optional.empty()
        if (!sAddr.equals("*")) {
            sAddrIp = IpAddress.valueOf(sAddr);
        }

        McastRoute mRoute = new McastRoute(sAddrIp, IpAddress.valueOf(gAddr), McastRoute.Type.STATIC);
        log.info("Route {}", mRoute);
        mcastRouteManager.add(mRoute);

        if (sources != null) {
            Set<ConnectPoint> sourcesSet = Arrays.stream(sources)
                    .map(ConnectPoint::deviceConnectPoint)
                    .collect(Collectors.toSet());
            log.info("{}", sourcesSet);
            mcastRouteManager.addSources(mRoute, sourcesSet);
        }

        if (hosts != null) {
            for (String hostId : hosts) {
                mcastRouteManager.addSink(mRoute, HostId.hostId(hostId));

            }
        }
        printMcastRoute(mRoute);
        printMcastRouteData(mcastRouteManager.routeData(mRoute));
    }

    private void printMcastRoute(McastRoute mcastRoute) {
        print(FORMAT_MAPPING, mcastRoute.type(), mcastRoute.group(), mcastRoute.source());
    }

    private void printMcastRouteData(McastRouteData mcastRouteData) {
        print("%s", mcastRouteData);
    }
}
