/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2014 Oracle and/or its affiliates.  All rights reserved.
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

package oracle.kv.impl.rep.stats;

import java.util.Date;

import oracle.kv.impl.rep.stats.PartitionLeaseManager.PartitionLeaseInfo;
import oracle.kv.impl.rep.stats.StatsLeaseManager.LeaseInfo;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.TableAPI;

/**
 * PartitionLeaseTable is used to manage the partition-wide lease table
 * associated with the gathering of table statistics for a partition.
 *
 */
public class PartitionLeaseManager extends
                    StatsLeaseManager<PartitionLeaseInfo> {
    /* Table name */
    public static final String TABLE_NAME = "PartitionStatsLease";

    /* The partition-specific columns in the lease table.  */
    public static final String COL_NAME_PARTITION_ID = "partitionId";

    public PartitionLeaseManager(TableAPI tableAPI) {
        super(tableAPI);
    }

    @Override
    protected PrimaryKey createPrimaryKey(PartitionLeaseInfo info) {
        final PrimaryKey primaryKey = leaseTable.createPrimaryKey();
        primaryKey.put(COL_NAME_PARTITION_ID, info.getParitionId());
        return primaryKey;
    }

    @Override
    protected Row createRow(PartitionLeaseInfo info, boolean terminated) {
        Row row = leaseTable.createRow();
        row.put(COL_NAME_PARTITION_ID, info.getParitionId());

        row.put(COL_NAME_LEASE_RN, info.getLeaseRN());

        final long currentTime = System.currentTimeMillis();
        final Date currentDate = new Date(currentTime);
        final String currentDateStr =
                StatsLeaseManager.DATE_FORMAT.format(currentDate);

        /*
         * Mark last updated data means we are done gathering statistics
         * for the index and no longer need to extend the lease.
         */
        if (terminated) {
            row.put(COL_NAME_LEASE_DATE, currentDateStr);

            /* Update the lastUpdated after the lease scanning completes */
            row.put(COL_NAME_LAST_UPDATE, currentDateStr);
        } else {
            final long nextTime = currentTime + info.getLeaseTime();
            final Date nextDate = new Date(nextTime);
            final String nextDateStr =
                    StatsLeaseManager.DATE_FORMAT.format(nextDate);
            row.put(COL_NAME_LEASE_DATE, nextDateStr);

            /*
             * Set the lastUpdated as the old one when the lease scanning is on
             * progress
             */
            row.put(COL_NAME_LAST_UPDATE, lastUpdatedDateStr);
        }

        return row;
    }

    @Override
    protected String getLeaseTableName() {
        return TABLE_NAME;
    }

    /**
     * This class is to save a row of the table PartitionStatsLease, and it
     * also converts passed arguments into a table row.
     */
    public static class PartitionLeaseInfo extends LeaseInfo {

        private final int partitionId;

        public PartitionLeaseInfo(int partitionId,
                                  String leaseRN,
                                  long leaseTime) {
            super(leaseRN, leaseTime);
            this.partitionId = partitionId;
        }

        public int getParitionId() {
            return partitionId;
        }

        @Override
        public String toString() {
            return "partition-" + String.valueOf(partitionId) + " by " +
                    leaseRN;
        }
    }
}
