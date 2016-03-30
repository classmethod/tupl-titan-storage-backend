/*
 * Copyright Classmethod, Inc. 2016, or its affiliates. All Rights Reserved.
 * Portions copyright 2013-2015 Apache Software Foundation
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
package jp.classmethod.titan.example;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import com.thinkaurelius.titan.core.Cardinality;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.schema.TitanManagement;

/**
 * Original class:
 * Apache TinkerPop :: TinkerGraph Gremlin :: TinkerFactory
 * 
 * Original authors:
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 * 
 * Modifications made by:
 * @author Alexander Patrikalakis (http://www.linkedin.com/in/amcpatrix)
 * 
 * The following changes are necessary to follow the tutorial when using
 * Titan's default configuration:
 * 1. Removed the ID assignments and instead use an "id" attribute
 * 2. Added a baby graph, a subset of the modern graph.
 * 3. Changed the edge weight types to be compatible with Titan in the baby graph
 * 4. The create* methods use the Classmethod Storage Backend for Titan (titan-tupl)
 * 5. The generate* methods just take a TP Structure Graph object
 * 6. Default schema cardinality cannot be set to List, so use the management system
 *    to create schema elements of list cardinality for each property in the "Crew"
 * 7. Added an index on "name" and "id" to silence the warnings saying that the
 *    "Query requires iterating over all vertices"
 * 
 * Note that the only difference between the modern and classic graphs is that the
 * modern graph employs vertex labels.
 *
 */
public class TuplTinkerFactory {
    public static final String PETER = "peter";
    public static final String RIPPLE = "ripple";
    public static final String JOSH = "josh";
    public static final String LOP = "lop";
    public static final String VADAS = "vadas";
    public static final String MARKO = "marko";
    public static final String JAVA = "java";
    public static final String LANG = "lang";
    public static final String WEIGHT = "weight";
    public static final String CREATED = "created";
    public static final String KNOWS = "knows";
    public static final String SOFTWARE = "software";
    public static final String PERSON = "person";
    public static final String SKILL = "skill";
    public static final String SINCE = "since";
    public static final String END_TIME = "endTime";
    public static final String START_TIME = "startTime";
    public static final String LOCATION = "location";
    public static final String USES = "uses";
    public static final String DEVELOPS = "develops";
    public static final String AGE = "age";
    public static final String NAME = "name";
    public static final String ID = "id";

    public static Graph createClassic() {
        final Graph g = openTuplGraph();
        generateClassic(g);
        return g;
    }

    private static Graph openTuplGraph() {
        final Configuration conf = new BaseConfiguration();
        conf.addProperty("storage.backend", "jp.classmethod.titan.diskstorage.tupl.TuplStoreManager");
        final TitanGraph g = TitanFactory.open(conf);
        final TitanManagement mgmt = g.openManagement();
        final PropertyKey nameKey = mgmt.makePropertyKey(NAME).cardinality(Cardinality.SINGLE).dataType(String.class).make();
        mgmt.buildIndex("nameKeyIndex", Vertex.class).addKey(nameKey).unique().buildCompositeIndex();
        mgmt.commit();
        g.tx().commit();
        return g;
    }

    public static void generateClassic(final Graph g) {
        final Vertex marko = g.addVertex(ID, 1, NAME, MARKO, AGE, 29);
        final Vertex vadas = g.addVertex(ID, 2, NAME, VADAS, AGE, 27);
        final Vertex lop = g.addVertex(ID, 3, NAME, LOP, LANG, JAVA);
        final Vertex josh = g.addVertex(ID, 4, NAME, JOSH, AGE, 32);
        final Vertex ripple = g.addVertex(ID, 5, NAME, RIPPLE, LANG, JAVA);
        final Vertex peter = g.addVertex(ID, 6, NAME, PETER, AGE, 35);
        marko.addEdge(KNOWS, vadas, ID, 7, WEIGHT, 0.5f);
        marko.addEdge(KNOWS, josh, ID, 8, WEIGHT, 1.0f);
        marko.addEdge(CREATED, lop, ID, 9, WEIGHT, 0.4f);
        josh.addEdge(CREATED, ripple, ID, 10, WEIGHT, 1.0f);
        josh.addEdge(CREATED, lop, ID, 11, WEIGHT, 0.4f);
        peter.addEdge(CREATED, lop, ID, 12, WEIGHT, 0.2f);
        g.tx().commit();
    }

    public static Graph createModern() {
        final Graph g = openTuplGraph();
        generateModern(g);
        return g;
    }

    public static void generateModern(final Graph g) {
        final Vertex marko = g.addVertex(ID, 1, T.label, PERSON, NAME, MARKO, AGE, 29);
        final Vertex vadas = g.addVertex(ID, 2, T.label, PERSON, NAME, VADAS, AGE, 27);
        final Vertex lop = g.addVertex(ID, 3, T.label, SOFTWARE, NAME, LOP, LANG, JAVA);
        final Vertex josh = g.addVertex(ID, 4, T.label, PERSON, NAME, JOSH, AGE, 32);
        final Vertex ripple = g.addVertex(ID, 5, T.label, SOFTWARE, NAME, RIPPLE, LANG, JAVA);
        final Vertex peter = g.addVertex(ID, 6, T.label, PERSON, NAME, PETER, AGE, 35);
        marko.addEdge(KNOWS, vadas, ID, 7, WEIGHT, 0.5d);
        marko.addEdge(KNOWS, josh, ID, 8, WEIGHT, 1.0d);
        marko.addEdge(CREATED, lop, ID, 9, WEIGHT, 0.4d);
        josh.addEdge(CREATED, ripple, ID, 10, WEIGHT, 1.0d);
        josh.addEdge(CREATED, lop, ID, 11, WEIGHT, 0.4d);
        peter.addEdge(CREATED, lop, ID, 12, WEIGHT, 0.2d);
        g.tx().commit();
    }

    public static Graph createBaby() {
        final Graph g = openTuplGraph();
        generateBaby(g);
        return g;
    }

    public static void generateBaby(final Graph g) {
        final Vertex marko = g.addVertex(ID, 1, T.label, PERSON, NAME, MARKO, AGE, 29);
        final Vertex lop = g.addVertex(ID, 3, T.label, SOFTWARE, NAME, LOP, LANG, JAVA);
        marko.addEdge(CREATED, lop, ID, 9, WEIGHT, 0.4d);
        g.tx().commit();
    }

    public static Graph createTheCrew() {
        final Configuration conf = new BaseConfiguration();
        conf.addProperty("storage.backend", "jp.classmethod.titan.diskstorage.tupl.TuplStoreManager");
        final TitanGraph g = TitanFactory.open(conf);
        final TitanManagement mgmt = g.openManagement();
        mgmt.makePropertyKey(LOCATION).cardinality(Cardinality.LIST).dataType(String.class).make();
        mgmt.makePropertyKey(START_TIME).cardinality(Cardinality.LIST).dataType(Integer.class).make();
        mgmt.makePropertyKey(END_TIME).cardinality(Cardinality.LIST).dataType(Integer.class).make();
        mgmt.commit();
        g.tx().commit();
        generateTheCrew(g);
        return g;
    }

    public static void generateTheCrew(final Graph g) {
        final Vertex marko = g.addVertex(ID, 1, T.label, PERSON, NAME, MARKO);
        final Vertex stephen = g.addVertex(ID, 7, T.label, PERSON, NAME, "stephen");
        final Vertex matthias = g.addVertex(ID, 8, T.label, PERSON, NAME, "matthias");
        final Vertex daniel = g.addVertex(ID, 9, T.label, PERSON, NAME, "daniel");
        final Vertex gremlin = g.addVertex(ID, 10, T.label, SOFTWARE, NAME, "gremlin");
        final Vertex tinkergraph = g.addVertex(ID, 11, T.label, SOFTWARE, NAME, "tinkergraph");

        marko.property(VertexProperty.Cardinality.list, LOCATION, "san diego", START_TIME, 1997, END_TIME, 2001);
        marko.property(VertexProperty.Cardinality.list, LOCATION, "santa cruz", START_TIME, 2001, END_TIME, 2004);
        marko.property(VertexProperty.Cardinality.list, LOCATION, "brussels", START_TIME, 2004, END_TIME, 2005);
        marko.property(VertexProperty.Cardinality.list, LOCATION, "santa fe", START_TIME, 2005);

        stephen.property(VertexProperty.Cardinality.list, LOCATION, "centreville", START_TIME, 1990, END_TIME, 2000);
        stephen.property(VertexProperty.Cardinality.list, LOCATION, "dulles", START_TIME, 2000, END_TIME, 2006);
        stephen.property(VertexProperty.Cardinality.list, LOCATION, "purcellville", START_TIME, 2006);

        matthias.property(VertexProperty.Cardinality.list, LOCATION, "bremen", START_TIME, 2004, END_TIME, 2007);
        matthias.property(VertexProperty.Cardinality.list, LOCATION, "baltimore", START_TIME, 2007, END_TIME, 2011);
        matthias.property(VertexProperty.Cardinality.list, LOCATION, "oakland", START_TIME, 2011, END_TIME, 2014);
        matthias.property(VertexProperty.Cardinality.list, LOCATION, "seattle", START_TIME, 2014);

        daniel.property(VertexProperty.Cardinality.list, LOCATION, "spremberg", START_TIME, 1982, END_TIME, 2005);
        daniel.property(VertexProperty.Cardinality.list, LOCATION, "kaiserslautern", START_TIME, 2005, END_TIME, 2009);
        daniel.property(VertexProperty.Cardinality.list, LOCATION, "aachen", START_TIME, 2009);

        marko.addEdge(DEVELOPS, gremlin, ID, 13, SINCE, 2009);
        marko.addEdge(DEVELOPS, tinkergraph, ID, 14, SINCE, 2010);
        marko.addEdge(USES, gremlin, ID, 15, SKILL, 4);
        marko.addEdge(USES, tinkergraph, ID, 16, SKILL, 5);

        stephen.addEdge(DEVELOPS, gremlin, ID, 17, SINCE, 2010);
        stephen.addEdge(DEVELOPS, tinkergraph, ID, 18, SINCE, 2011);
        stephen.addEdge(USES, gremlin, ID, 19, SKILL, 5);
        stephen.addEdge(USES, tinkergraph, ID, 20, SKILL, 4);

        matthias.addEdge(DEVELOPS, gremlin, ID, 21, SINCE, 2012);
        matthias.addEdge(USES, gremlin, ID, 22, SKILL, 3);
        matthias.addEdge(USES, tinkergraph, ID, 23, SKILL, 3);

        daniel.addEdge(USES, gremlin, ID, 24, SKILL, 5);
        daniel.addEdge(USES, tinkergraph, ID, 25, SKILL, 3);

        gremlin.addEdge("traverses", tinkergraph, ID, 26);

        g.variables().set("creator", MARKO);
        g.variables().set("lastModified", 2014);
        g.variables().set("comment", "this graph was created to provide examples and test coverage for tinkerpop3 api advances");
        g.tx().commit();
    }

}
