/*
 * Copyright 2016 Classmethod, Inc. or its affiliates. All Rights Reserved.
 * Portions copyright Titan: Distributed Graph Database - Copyright 2012 and onwards Aurelius.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package jp.classmethod.titan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.common.collect.Iterables;
import org.junit.Test;

import com.thinkaurelius.titan.StorageSetup;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.VertexLabel;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.util.TitanId;
import com.thinkaurelius.titan.diskstorage.BackendException;
import com.thinkaurelius.titan.diskstorage.configuration.BasicConfiguration;
import com.thinkaurelius.titan.diskstorage.configuration.WriteConfiguration;
import com.thinkaurelius.titan.diskstorage.configuration.backend.CommonsConfiguration;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.KeyColumnValueStoreManager;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.keyvalue.OrderedKeyValueStoreManagerAdapter;
import com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration;
import com.thinkaurelius.titan.graphdb.database.StandardTitanGraph;
import com.thinkaurelius.titan.graphdb.transaction.StandardTitanTx;

import jp.classmethod.titan.diskstorage.tupl.TuplStoreManager;

/**
 * Utility methods to create graph configurations
 * @author Alexander Patrikalakis
 *
 */
public class TuplStorageSetup extends StorageSetup {
    public static final String SIMILAR = "similar";
    public static final String NODE_COMMUNITY = "nodeCommunity";
    public static final String COMMUNITY = "community";
    public static final String NODE_ID = "nodeId";
    public static final String NODE_LABEL = "node";

//BEGIN adaptation of:
// https://github.com/thinkaurelius/titan/blob/1.0.0/titan-berkeleyje/src/test/java/com/thinkaurelius/titan/BerkeleyStorageSetup.java#L8
    public static Configuration getTuplGraphBaseConfiguration() {
        BaseConfiguration config = new BaseConfiguration();
        Configuration storage = config.subset("storage");
        storage.addProperty(GraphDatabaseConfiguration.STORAGE_DIRECTORY.getName(), getHomeDir());
        storage.addProperty(GraphDatabaseConfiguration.STORAGE_BACKEND.getName(),
                "jp.classmethod.titan.diskstorage.tupl.TuplStoreManager");
        return config;
    }
    public static WriteConfiguration getTuplStorageWriteConfiguration() {
        return new CommonsConfiguration(getTuplGraphBaseConfiguration());
    }
    public static BasicConfiguration getTuplStorageConfiguration() {
        final WriteConfiguration wc = getTuplStorageWriteConfiguration();
        final BasicConfiguration config = new BasicConfiguration(GraphDatabaseConfiguration.ROOT_NS, wc,
                BasicConfiguration.Restriction.NONE);
        return config;
    }
//END adaptation of:
// https://github.com/thinkaurelius/titan/blob/1.0.0/titan-berkeleyje/src/test/java/com/thinkaurelius/titan/BerkeleyStorageSetup.java#L28
    public static KeyColumnValueStoreManager getKCVStorageManager() throws BackendException {
        final TuplStoreManager sm = new TuplStoreManager(getTuplStorageConfiguration());
        return new OrderedKeyValueStoreManagerAdapter(sm);
    }

    public Vertex getOrCreate(String value, StandardTitanTx tx, VertexLabel label, boolean custom)
    {
        final Long longVal = Long.valueOf(value); //the value used in data files
        final long titanVertexId =
                TitanId.toVertexId((longVal << 1) + 1 /*move over 1 bit for 2 partitions (2^1 = 2)*/);
        final GraphTraversal<Vertex, Vertex> t = tx.traversal().V().has(NODE_ID, value);
        final TitanVertex vertex;
        if(t.hasNext()) {
            vertex = (TitanVertex) t.next();
        } else {
            if(custom) {
                vertex = tx.addVertex(titanVertexId, label);
                vertex.property(NODE_ID, longVal);
            } else {
                vertex = tx.addVertex(T.label, label.name(), NODE_ID, longVal);
            }
        }
        System.out.println("logical id: " + value +
                (custom ? "; custom" : "; auto") + " titan id: " + (custom ? titanVertexId : vertex.id()) +
                "; binary: " + Long.toBinaryString(custom ? titanVertexId : Long.valueOf(vertex.id().toString())));

        return vertex;
    }

    @Test
    public void testCustomIds() {
        final boolean custom = true;

        final Configuration conf = new MapConfiguration(new HashMap<String, String>());
        final Configuration graphNs = conf.subset(GraphDatabaseConfiguration.GRAPH_NS.getName());
        final Configuration storage = conf.subset(GraphDatabaseConfiguration.STORAGE_NS.getName());
        final Configuration cluster = conf.subset(GraphDatabaseConfiguration.CLUSTER_NS.getName());

        //graph NS config
        if(custom) {
            graphNs.addProperty(GraphDatabaseConfiguration.ALLOW_SETTING_VERTEX_ID.getName(),  "true");
        }
        graphNs.addProperty(GraphDatabaseConfiguration.UNIQUE_INSTANCE_ID.getName(), "DEADBEEF");

        //cluster NS config. only two partitions for now
        //recall the number of partitions is a FIXED property so user cant override
        //initial value stored in system_properties the first time the graph is loaded.
        //default is 32
        cluster.addProperty(GraphDatabaseConfiguration.CLUSTER_MAX_PARTITIONS.getName(), 2);

        // storage NS config. FYI, storage.idauthority-wait-time is 300ms
        storage.addProperty(GraphDatabaseConfiguration.STORAGE_BACKEND.getName(), "jp.classmethod.titan.diskstorage.tupl.TuplStoreManager");
        // TODO(amcp) why does this break persistence when custom ids are off
        // (may break persistence when custom ids on as well)???
        storage.addProperty(GraphDatabaseConfiguration.STORAGE_BATCH.getName(), true /*batchLoading*/);
        storage.addProperty(GraphDatabaseConfiguration.STORAGE_TRANSACTIONAL.getName(), false /*transactional*/);
        storage.addProperty(GraphDatabaseConfiguration.PARALLEL_BACKEND_OPS.getName(), "false" /*no pool, single threaded access*/);

        //create graph
        final CommonsConfiguration cc = new CommonsConfiguration(conf);
        final GraphDatabaseConfiguration dbconfig = new GraphDatabaseConfiguration(cc);
        try(final StandardTitanGraph graph = new StandardTitanGraph(dbconfig)) {
            //schema
            final TitanManagement mgmt = graph.openManagement();
            mgmt.makeVertexLabel(NODE_LABEL).make();
            final PropertyKey key = mgmt.makePropertyKey(NODE_ID).dataType(Integer.class).make();
            mgmt.buildIndex(NODE_ID, Vertex.class).addKey(key).unique().buildCompositeIndex();
            mgmt.commit();
            graph.tx().commit();

            //Need to explicitly pull the vertex label vertex into the current transaction for bulk loading
            //with custom ids to work.
            graph.tx().open();
            final VertexLabel nodeLabel = graph.getVertexLabel(NODE_LABEL);
            assertEquals(1, graph.getOpenTransactions().size());
            final StandardTitanTx tx = (StandardTitanTx) Iterables.getOnlyElement(graph.getOpenTransactions());

            getOrCreate("1" /*logicalid*/, tx, nodeLabel, custom);
            getOrCreate("2" /*logicalid*/, tx, nodeLabel, custom);
            getOrCreate("3" /*logicalid*/, tx, nodeLabel, custom);
            graph.tx().commit();
            assertEquals(0, graph.getOpenTransactions().size());

            final GraphTraversalSource g = graph.traversal();
            assertTrue(graph.getOpenTransactions().isEmpty());
            assertEquals(3, g.V().hasLabel(NODE_LABEL).toList().size());

            assertEquals(1, graph.getOpenTransactions().size());
            graph.tx().close();
            assertEquals(0, graph.getOpenTransactions().size());
        }
    }
}
