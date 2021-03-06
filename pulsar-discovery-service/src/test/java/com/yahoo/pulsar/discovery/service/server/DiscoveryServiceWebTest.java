/**
 * Copyright 2016 Yahoo Inc.
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
package com.yahoo.pulsar.discovery.service.server;

import static org.apache.bookkeeper.test.PortManager.nextFreePort;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.testng.annotations.Test;

import com.yahoo.pulsar.common.configuration.PulsarConfigurationLoader;

/**
 * 1. starts discovery service a. loads broker list from zk 2. http-client calls multiple http request: GET, PUT and
 * POST. 3. discovery service redirects to appropriate brokers in round-robin 4. client receives unknown host exception
 * with redirected broker
 *
 */
public class DiscoveryServiceWebTest {

    
    @Test
    public void testWebDiscoveryServiceStarter() throws Exception {

        int port = nextFreePort();
        File testConfigFile = new File("tmp." + System.currentTimeMillis() + ".properties");
        if (testConfigFile.exists()) {
            testConfigFile.delete();
        }
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(testConfigFile)));
        printWriter.println("zookeeperServers=z1.pulsar.com,z2.pulsar.com,z3.pulsar.com");
        printWriter.println("globalZookeeperServers=z1.pulsar.com,z2.pulsar.com,z3.pulsar.com");
        printWriter.println("webServicePort=" + port);
        printWriter.close();
        testConfigFile.deleteOnExit();
        final ServiceConfig config = PulsarConfigurationLoader.create(testConfigFile.getAbsolutePath(), ServiceConfig.class);
        final ServerManager server = new ServerManager(config);
        DiscoveryServiceStarter.startWebService(server, config);
        assertTrue(server.isStarted());
        server.stop();
        testConfigFile.delete();
    }

}
