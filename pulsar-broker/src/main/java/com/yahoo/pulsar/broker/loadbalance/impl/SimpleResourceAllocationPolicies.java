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
package com.yahoo.pulsar.broker.loadbalance.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.pulsar.broker.PulsarService;
import com.yahoo.pulsar.broker.admin.AdminResource;
import com.yahoo.pulsar.broker.loadbalance.LoadReport;
import com.yahoo.pulsar.broker.loadbalance.ResourceUnit;
import com.yahoo.pulsar.broker.loadbalance.ServiceUnit;
import com.yahoo.pulsar.common.naming.NamespaceName;
import com.yahoo.pulsar.common.policies.NamespaceIsolationPolicy;
import com.yahoo.pulsar.common.policies.impl.NamespaceIsolationPolicies;
import com.yahoo.pulsar.zookeeper.ZooKeeperDataCache;

public class SimpleResourceAllocationPolicies {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleResourceAllocationPolicies.class);
    private final ZooKeeperDataCache<NamespaceIsolationPolicies> namespaceIsolationPolicies;
    private final PulsarService pulsar;

    SimpleResourceAllocationPolicies(PulsarService pulsar) {
        this.namespaceIsolationPolicies = pulsar.getConfigurationCache().namespaceIsolationPoliciesCache();
        this.pulsar = pulsar;
    }

    public boolean canAssign(ServiceUnit srvUnit, ResourceUnit rescrUnit, Map<ResourceUnit, LoadReport> loadReports) {
        // TODO: need to apply the policies to the assignment and decide whether it can be assigned or not
        return true;
    }

    private NamespaceIsolationPolicies getIsolationPolicies(String clusterName) {
        NamespaceIsolationPolicies policies = null;
        try {
            policies = namespaceIsolationPolicies
                    .get(AdminResource.path("clusters", clusterName, "namespaceIsolationPolicies")).orElse(null);
        } catch (Exception e) {
            LOG.warn("GetIsolationPolicies: Unable to get the namespaceIsolationPolicies [{}]", e);
        }
        return policies;
    }

    public boolean IsIsolationPoliciesPresent(NamespaceName namespace) {
        try {
            NamespaceIsolationPolicies policies = null;
            policies = getIsolationPolicies(pulsar.getConfiguration().getClusterName());
            if (policies != null)
                return policies.getPolicyByNamespace(namespace) != null;
        } catch (Exception e) {
            LOG.warn("IsIsolationPoliciesPresent: Unable to get the namespaceIsolationPolicies [{}]", e);
        }
        return false;
    }

    private NamespaceIsolationPolicy getNamespaceIsolationPolicy(NamespaceName namespace) {
        NamespaceIsolationPolicies policies = null;
        NamespaceIsolationPolicy policy = null;
        try {
            policies = getIsolationPolicies(pulsar.getConfiguration().getClusterName());
            policy = policies.getPolicyByNamespace(namespace);
        } catch (Exception e) {
            LOG.warn("Unable to get the namespaceIsolationPolicies [{}]", e);
        }
        return policy;
    }

    public boolean isPrimaryBroker(NamespaceName namespace, String broker) {
		NamespaceIsolationPolicy nsPolicy = getNamespaceIsolationPolicy(namespace);
		return (nsPolicy != null) ? nsPolicy.isPrimaryBroker(broker) : false;
    }

    public boolean isSecondaryBroker(NamespaceName namespace, String broker) {
		NamespaceIsolationPolicy nsPolicy = getNamespaceIsolationPolicy(namespace);
		return (nsPolicy != null) ? nsPolicy.isSecondaryBroker(broker) : false;
    }

    public boolean isSharedBroker(String broker) {
        try {
            NamespaceIsolationPolicies policies = getIsolationPolicies(pulsar.getConfiguration().getClusterName());
            if (policies == null)
                return true;
            return policies.isSharedBroker(broker);
        } catch (Exception e) {
            LOG.warn("isPrimaryForAnyNamespace: [{}]", e);
        }
        return false;
    }

    public boolean shouldFailoverToSecondaries(NamespaceName namespace, int totalPrimaryCandidates) {
		NamespaceIsolationPolicy nsPolicy = getNamespaceIsolationPolicy(namespace);
		return (nsPolicy != null) ? nsPolicy.shouldFailover(totalPrimaryCandidates) : false;
    }
}
