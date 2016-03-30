/*
 * Copyright 2016 Classmethod, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package jp.classmethod.titan.diskstorage.tupl;

import java.io.IOException;

import org.cojen.tupl.Database;
import org.cojen.tupl.DurabilityMode;
import org.cojen.tupl.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.thinkaurelius.titan.diskstorage.BackendException;
import com.thinkaurelius.titan.diskstorage.BaseTransactionConfig;
import com.thinkaurelius.titan.diskstorage.PermanentBackendException;
import com.thinkaurelius.titan.diskstorage.StaticBuffer;
import com.thinkaurelius.titan.diskstorage.common.AbstractStoreTransaction;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.StoreTransaction;

/**
 * 
 * @author Alexander Patrikalakis
 *
 */
public class TuplStoreTransaction extends AbstractStoreTransaction {
    private static final Logger log = LoggerFactory.getLogger(TuplStoreTransaction.class);
    public static final String HEX_PREFIX = "0x";
    private final String id;
    private final Transaction txn;
    private final Database database;
    public TuplStoreTransaction(BaseTransactionConfig config, Transaction txn, Database database) {
        super(config);
        this.txn = txn;
        this.database = database;
        id = HEX_PREFIX + Long.toHexString(System.nanoTime()); //TODO(amcp) is this necessary?
    }

    public static TuplStoreTransaction getTx(StoreTransaction txh) {
        Preconditions.checkArgument(txh != null);
        Preconditions.checkArgument(txh instanceof TuplStoreTransaction,
                        "Unexpected transaction type %s", txh.getClass().getName());
        return (TuplStoreTransaction) txh;
    }

    public String getId() {
        return id;
    }

    public Transaction getTuplTxn() {
        return txn;
    }

    public boolean contains(StaticBuffer key) {
        return false; //TODO tupl supports conditional writes, should I use them?
    }

    public void put(StaticBuffer key, StaticBuffer expectedValue) {
        //TODO tupl supports conditional writes, should I use them?
    }

    @Override
    public void commit() throws BackendException {
        log.trace("commit txn={}, id={}", txn, id);

        try {
            txn.commit();
            txn.exit();
            if(DurabilityMode.NO_REDO == txn.durabilityMode()) { //TODO should I always call checkpoint?
                database.checkpoint();
            }
        } catch (IOException e) {
            throw new PermanentBackendException("unable to commit tx " + id, e);
        }
    }

    @Override
    public void rollback() throws BackendException {
        log.trace("rollback txn={}, id={}", txn, id);
        try {
            txn.reset();
        } catch (IOException e) {
            throw new PermanentBackendException("unable to commit tx " + id, e);
        }
    }
}
