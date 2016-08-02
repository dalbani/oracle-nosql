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

package oracle.kv.impl.topo;

import oracle.kv.impl.topo.ResourceId.ResourceType;

import com.sleepycat.persist.model.Persistent;

/**
 * Defines the RepGroup internal map used collect its arb nodes
 */
@Persistent
class ArbNodeComponentMap extends ComponentMap<ArbNodeId, ArbNode> {

    private static final long serialVersionUID = 1L;

    /* The owning rep group. */
    private final RepGroup repGroup;

    public ArbNodeComponentMap(RepGroup repGroup, Topology topology) {
        super(topology);
        this.repGroup = repGroup;
    }

    /**
     * Empty constructor to satisfy DPL. Though this class is never persisted,
     * because it is referenced from existing persistent classes it must be
     * annotated and define an empty constructor.
     */
    @SuppressWarnings("unused")
    private ArbNodeComponentMap() {
        throw new IllegalStateException("Should not be invoked");
    }

    /* (non-Javadoc)
     * @see oracle.kv.impl.topo.ComponentMap#nextId()
     */
    @Override
    ArbNodeId nextId() {
        return  new ArbNodeId(this.repGroup.getResourceId().getGroupId(),
                              nextSequence());
    }

    /* (non-Javadoc)
     * @see oracle.kv.impl.topo.ComponentMap#getResourceType()
     */
    @Override
    ResourceType getResourceType() {
       return ResourceType.ARB_NODE;
    }
}
