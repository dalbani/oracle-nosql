/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2016 Oracle and/or its affiliates.  All rights reserved.
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

package oracle.kv.impl.arb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import oracle.kv.impl.measurement.ArbiterNodeStats;
import oracle.kv.impl.measurement.JVMStats;
import oracle.kv.impl.monitor.AgentRepository;
import oracle.kv.impl.param.DurationParameter;
import oracle.kv.impl.param.ParameterListener;
import oracle.kv.impl.param.ParameterMap;
import oracle.kv.impl.param.ParameterState;
import oracle.kv.impl.rep.ScheduleStart;
import oracle.kv.impl.rep.monitor.StatsPacket;
import oracle.kv.impl.topo.ArbNodeId;
import oracle.kv.impl.util.KVThreadFactory;
import oracle.kv.impl.util.server.LoggerUtils;

import com.sleepycat.je.rep.arbiter.ArbiterStats;

/**
 * Stats pertaining to an arbiter node.
 */
public class ArbStatsTracker implements ParameterListener {

    private final AgentRepository monitorBuffer;
    private final ScheduledExecutorService collector;
    private Future<?> collectorFuture;
    protected List<Listener> listeners = new ArrayList<Listener>();

    /* Timestamp for the end of the last collection period. */
    private long lastEnd;

    private final Logger logger;
    private final ArbNodeService arbNodeService;

    /**
     */
    public ArbStatsTracker(ArbNodeService arbNodeService,
                                  ParameterMap map,
                                  AgentRepository monitorBuffer) {

        this.arbNodeService = arbNodeService;
        this.monitorBuffer = monitorBuffer;
        ArbNodeService.Params params = arbNodeService.getParams();
        this.logger =
            LoggerUtils.getLogger(ArbStatsTracker.class, params);
        ThreadFactory factory = new CollectorThreadFactory
            (logger, params.getArbNodeParams().getArbNodeId());
        collector = new ScheduledThreadPoolExecutor(1, factory);
        initialize(map);
    }

    /**
     * Used for initialization during constructions and from newParameters()
     * NOTE: newParameters() results in loss of cumulative stats and reset of
     * trackingStart.
     */
    private void initialize(ParameterMap map) {
        if (collectorFuture != null) {
            logger.fine("Cancelling current ArbStatsCollector");
            collectorFuture.cancel(true);
        }

        DurationParameter dp =
            (DurationParameter) map.get(ParameterState.SP_INTERVAL);
        ScheduleStart start =
            ScheduleStart.calculateStart(Calendar.getInstance(),
                                         (int) dp.getAmount(), dp.getUnit());
        logger.fine("Starting operationStatsCollector " + start);
        collectorFuture =
            collector.scheduleAtFixedRate(new CollectStats(),
                                          start.getDelay(),
                                          start.getInterval(),
                                          start.getTimeUnit());
        lastEnd = System.currentTimeMillis();
    }

    @Override
    synchronized public void newParameters(ParameterMap oldMap,
                                           ParameterMap newMap) {

        /*
         * Caller ensures that the maps are different, check for
         * differences that matter to this class.  Re-init if *any* of the
         * parameters are different.
         */
        if (paramsDiffer(oldMap, newMap, ParameterState.SP_INTERVAL)) {
            initialize(newMap);
        }
    }

    private boolean paramsDiffer(ParameterMap map1,
                                 ParameterMap map2,
                                 String param) {
        return map1.get(param).equals(map2.get(param));
    }

    public void close() {
        collectorFuture.cancel(true);
    }

    /**
     * Invoked by the async collection job and at arb node close.
     */
    synchronized public void pushStats() {

        logger.fine("Collecting arbiter stats");
        long useStart = lastEnd;
        long useEnd = System.currentTimeMillis();

        StatsPacket packet = new StatsPacket(useStart, useEnd);

        if (arbNodeService.getParams().getArbNodeParams().
            getCollectEnvStats()) {
            ArbNode an = arbNodeService.getArbNode();
            if (an != null) {
                ArbiterStats anStats =
                    an.getStats(true);
                if (anStats != null) {
                    packet.add(new ArbiterNodeStats(useStart, useEnd, anStats));
                }
            }

            packet.add(new JVMStats(useStart, useEnd));
        }

        lastEnd = useEnd;

        monitorBuffer.add(packet);
        sendPacket(packet);
        logger.fine(packet.toString());
    }

    /**
     * Simple Runnable to send stats to the service's monitor agent
     */
    private class CollectStats implements Runnable {

        @Override
        public void run() {
            pushStats();
        }
    }

    /**
     * Collector threads are named KVAgentMonitorCollector and log uncaught
     * exceptions to the monitor logger.
     */
    private class CollectorThreadFactory extends KVThreadFactory {
        private final ArbNodeId arbNodeId;

        CollectorThreadFactory(Logger logger, ArbNodeId arbNodeId) {
            super(null, logger);
            this.arbNodeId = arbNodeId;
        }

        @Override
        public String getName() {
            return  arbNodeId + "_MonitorAgentCollector";
        }
    }

    /**
     * An ArbStatsTracker.Listener can be implemented by clients of this
     * interface to receive stats when they are collected.
     */
    public interface Listener {
        void receiveStats(StatsPacket packet);
    }

    public void addListener(Listener lst) {
        listeners.add(lst);
    }

    public void removeListener(Listener lst) {
        listeners.remove(lst);
    }

    private void sendPacket(StatsPacket packet) {
        for (Listener lst : listeners) {
            lst.receiveStats(packet);
        }
    }
}
