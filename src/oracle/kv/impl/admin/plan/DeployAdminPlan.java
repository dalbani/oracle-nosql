/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2015 Oracle and/or its affiliates.  All rights reserved.
 *
 *  Oracle NoSQL Database is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation, version 3.
 *
 *  Oracle NoSQL Database is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License in the LICENSE file along with Oracle NoSQL Database.  If not,
 *  see <http://www.gnu.org/licenses/>.
 *
 *  An active Oracle commercial licensing agreement for this product
 *  supercedes this license.
 *
 *  For more information please contact:
 *
 *  Vice President Legal, Development
 *  Oracle America, Inc.
 *  5OP-10
 *  500 Oracle Parkway
 *  Redwood Shores, CA 94065
 *
 *  or
 *
 *  berkeleydb-info_us@oracle.com
 *
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  EOF
 *
 */

package oracle.kv.impl.admin.plan;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.jackson.node.ObjectNode;

import oracle.kv.impl.admin.IllegalCommandException;
import oracle.kv.impl.admin.NonfatalAssertionException;
import oracle.kv.impl.admin.Admin;
import oracle.kv.impl.admin.param.AdminParams;
import oracle.kv.impl.admin.param.Parameters;
import oracle.kv.impl.admin.param.StorageNodeParams;
import oracle.kv.impl.admin.plan.task.DeployAdmin;
import oracle.kv.impl.admin.plan.task.NewAdminParameters;
import oracle.kv.impl.admin.plan.task.UpdateAdminHelperHost;
import oracle.kv.impl.admin.plan.task.Utils;
import oracle.kv.impl.admin.plan.task.WaitForAdminState;
import oracle.kv.impl.param.ParameterMap;
import oracle.kv.impl.param.ParameterState;
import oracle.kv.impl.security.KVStorePrivilege;
import oracle.kv.impl.security.SystemPrivilege;
import oracle.kv.impl.topo.AdminId;
import oracle.kv.impl.topo.AdminType;
import oracle.kv.impl.topo.DatacenterType;
import oracle.kv.impl.topo.StorageNode;
import oracle.kv.impl.topo.StorageNodeId;
import oracle.kv.impl.topo.Topology;
import oracle.kv.impl.util.ConfigurableService.ServiceStatus;
import oracle.kv.impl.util.JsonUtils;

import com.sleepycat.persist.model.Persistent;

/**
 * A plan for deploying one or more instances of the Admin service onto
 * storage nodes.
 *
 * version 0: original
 * version 1: Added targetAP field
 */
@Persistent(version=1)
public class DeployAdminPlan extends AbstractPlan {

    private static final long serialVersionUID = 1L;

    private int httpPort;
    AdminParams targetAP;

    public DeployAdminPlan(AtomicInteger idGen,
                           String name,
                           Planner planner,
                           StorageNodeId snid,
                           int httpPort,
                           AdminType type) {

        super(idGen, name, planner);

        Admin admin = planner.getAdmin();
        this.httpPort = httpPort;

        final Topology topo = admin.getCurrentTopology();
        final StorageNode sn = topo.get(snid);
        if (sn == null) {
            throw new IllegalCommandException
                (snid + " is not a valid Storage Node id.  " +
                 "Please provide the id of an existing Storage Node.");
        }

        type = computeAdminType(type, topo, snid);
        targetAP = findExistingParams(admin, snid, type);
        AdminId newAdminId = null;
        if (targetAP == null) {
            newAdminId = admin.generateAdminId();
            ParameterMap pMap = admin.copyPolicy();
            targetAP = new AdminParams(pMap, newAdminId, snid,
                                       httpPort, type);
            removeSomePolicyParams(targetAP.getMap());

        } else {
            newAdminId = targetAP.getAdminId();
        }

        addTask(new DeployAdmin(this, snid, newAdminId));
        addTask(new WaitForAdminState(this, snid, newAdminId,
                                      ServiceStatus.RUNNING));

        /* Let the existing admin instances know about the new one. */
        Parameters parameters = admin.getCurrentParameters();
        for (AdminParams ap : parameters.getAdminParams()) {
            AdminId aid = ap.getAdminId();
            StorageNodeParams snp = parameters.get(ap.getStorageNodeId());
            String hostname = snp.getHostname();
            int registryPort = snp.getRegistryPort();
            addTask(new UpdateAdminHelperHost(this, aid));
            addTask(new NewAdminParameters(this, hostname, registryPort, aid));
        }
    }

    /*
     * Returns the type for creating an Admin on the specified SN. If the
     * stecified type is null, the returned type will be the same as the zone
     * in which the Admin will reside. Otherwise type is returned.
     */
    private AdminType computeAdminType(AdminType type,
                                       Topology topo,
                                       StorageNodeId snId) {
        if (type != null) {
            return type;
        }
        /* If the type is null use the zone type */
        final DatacenterType dcType =
                                topo.getDatacenter(snId).getDatacenterType();
        return Utils.getAdminType(dcType);
    }

    /*
     * Returns any Admin params that exist on this storage node already. Also
     * verifies that at least one Admin is configured as a primary. Throws an
     * IllegalCommandException if none are found. This means that the first
     * Admin configured must be a primary.
     */
    private AdminParams findExistingParams(Admin admin,
                                           StorageNodeId snid,
                                           AdminType type) {
        final Parameters parameters = admin.getCurrentParameters();
        boolean primaryConfigured = type.isPrimary();
        AdminParams existingAP = null;
        for (AdminId aid : parameters.getAdminIds()) {
            AdminParams ap = parameters.get(aid);
            if (snid.equals(ap.getStorageNodeId())) {
                if (existingAP != null) {
                    /* Should only be one Admin on any SN. */
                    throw new NonfatalAssertionException
                        ("More than one admin service exists on " + snid +
                         ". " + existingAP.getAdminId() + "[" +
                         existingAP.getHttpPort() + "] and " +
                         ap.getAdminId() + " [" + ap.getHttpPort() + "]");
                }

                if (httpPort != ap.getHttpPort()) {
                    throw new IllegalCommandException
                        (ap.getAdminId() + " is already configured with " +
                         "httpPort" + ap.getHttpPort() + " and cannot be " +
                         "configured for " + httpPort);
                }
                if (!ap.getType().equals(type)) {
                    throw new IllegalCommandException("Attempting to change " +
                                                      aid  + " from " +
                                                      ap.getType() + " to " +
                                                      type);
                }
                existingAP = ap;
            }
            if (ap.getType().isPrimary()) {
                primaryConfigured = true;
            }
        }
        if (!primaryConfigured) {
            throw new IllegalCommandException("No primary Admin configured");
        }
        return existingAP;
    }

    /*
     * Do not use these as policy params for the admin:
     * JE_CACHE_SIZE, JE_MISC, JVM_MISC.
     */
    private void removeSomePolicyParams(ParameterMap map) {
        map.remove(ParameterState.JE_CACHE_SIZE);
        map.remove(ParameterState.JE_MISC);
        map.remove(ParameterState.JVM_MISC);
    }

    /*
     * No-arg ctor for use by DPL.
     */
    @SuppressWarnings("unused")
    private DeployAdminPlan() {
    }

    @Override
    public void preExecutionSave() {
        Admin admin = getAdmin();

        /* We can only "add" the new Admin once. */
        Parameters p = admin.getCurrentParameters();
        if (p.get(targetAP.getAdminId()) == null) {
            admin.addAdminParams(targetAP);
        }
    }

    @Override
    public boolean isExclusive() {
        return true;
    }

    @Override
    public String getDefaultName() {
        return "Deploy Admin Service";
    }

    public int getHttpPort() {
        return httpPort;
    }

    @Override
    void stripForDisplay() {
        targetAP = null;
    }

    @Override
    public List<? extends KVStorePrivilege> getRequiredPrivileges() {
        /* Requires SYSOPER */
        return SystemPrivilege.sysoperPrivList;
    }

    @Override
    public boolean isJsonSupported() {
        return true;
    }

    @Override
    public String getOperation() {
        return "plan deploy-admin -sn " +
               targetAP.getStorageNodeId().getStorageNodeId() +
               " -port " + httpPort;
    }

    /*
     * TODO: replace field names with constants held in a json/command output
     * utility class.
     */
    @Override
    public ObjectNode getPlanJson() {
        ObjectNode jsonTop = JsonUtils.createObjectNode();
        jsonTop.put("plan_id", getId());
        jsonTop.put("resource_id", targetAP.getAdminId().toString());
        jsonTop.put("sn_id", targetAP.getStorageNodeId().toString());
        jsonTop.put("port", httpPort);
        return jsonTop;
    }
}
