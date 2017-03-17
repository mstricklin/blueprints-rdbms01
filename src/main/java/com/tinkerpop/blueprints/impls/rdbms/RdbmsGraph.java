// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.rdbms;

import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.impls.rdbms.transactionalCache.ObjectCache;
import com.tinkerpop.blueprints.impls.rdbms.transactionalCache.XCache;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class RdbmsGraph implements TransactionalGraph, IndexableGraph, KeyIndexableGraph, MetaGraph<RdbmsGraph> {
    private static final Features FEATURES = new Features();

    static {
        // TODO: revisit this...
        FEATURES.supportsSerializableObjectProperty = false;
        FEATURES.supportsBooleanProperty = true;
        FEATURES.supportsDoubleProperty = true;
        FEATURES.supportsFloatProperty = true;
        FEATURES.supportsIntegerProperty = true;
        FEATURES.supportsPrimitiveArrayProperty = true;
        FEATURES.supportsUniformListProperty = true;
        FEATURES.supportsMixedListProperty = false;
        FEATURES.supportsLongProperty = true;
        FEATURES.supportsMapProperty = false;
        FEATURES.supportsStringProperty = true;

        FEATURES.supportsDuplicateEdges = true;
        FEATURES.supportsSelfLoops = true;
        FEATURES.isPersistent = true;
        FEATURES.isWrapper = false;
        FEATURES.supportsVertexIteration = true;
        FEATURES.supportsEdgeIteration = true;
        FEATURES.supportsVertexIndex = true;
        FEATURES.supportsEdgeIndex = true;
        FEATURES.ignoresSuppliedIds = true;
        FEATURES.supportsTransactions = false; // true
        FEATURES.supportsIndices = true;
        FEATURES.supportsKeyIndices = true;
        FEATURES.supportsVertexKeyIndex = true;
        FEATURES.supportsEdgeKeyIndex = true;
        FEATURES.supportsEdgeRetrieval = true;
        FEATURES.supportsVertexProperties = true;
        FEATURES.supportsEdgeProperties = true;
        FEATURES.supportsThreadedTransactions = false;
    }

    private final XCache xcache = new XCache();
    private final XCache.ElementCache<XVertex> vertexCache;
    private final XCache.ElementCache<XEdge> edgeCache;

    // props x 2
    // indices
    // =================================
    public RdbmsGraph() {
        vertexCounter.set(77);
        edgeCounter.set(77);
        vertexCache = xcache.vertexCache();
        edgeCache = xcache.edgeCache();

    }
    // =================================
    public void cacheDump() {
        log.info("=========== Vertices ================");
        vertexCache.dump();
        log.info("=========== Edges ===================");
        edgeCache.dump();
    }
    // =================================
    public void dump() {
        if (log.isDebugEnabled()) {
            log.debug("RdbmsGraph vertices...");
            for (Vertex v : getVertices()) {
                log.debug(v.toString());
            }
            log.debug("RdbmsGraph edges...");
            for (Edge e : getEdges()) {
                log.debug(e.toString());
            }
        }
    }
    // =================================

    @Override
    public RdbmsGraph getRawGraph() {
        return this;
    }

    @Override
    public Features getFeatures() {
        return FEATURES;
    }
    // =================================
    @Override
    public Vertex addVertex(Object id) {
        XVertex v = vertexCache.add(XVertex.of(vertexCounter.getAndIncrement(), this));
        // queue W-B
        return v;
    }
    @Override
    public Vertex getVertex(Object id) {
        if (null == id)
            throw ExceptionFactory.vertexIdCanNotBeNull();
        try {
            final Integer intID = (id instanceof Integer) ? (Integer) id
                    : Integer.valueOf(id.toString());
            return vertexCache.get(intID);
        } catch (NumberFormatException | ClassCastException e) {
            log.error("could not use vertex id {}", id);
        }
        return null;
    }
    @Override
    public void removeVertex(Vertex vertex) {
        checkNotNull(vertex);
        for (Edge e : vertex.getEdges(Direction.BOTH))
            removeEdge(e);
        Integer longId = (Integer) vertex.getId();
        // remove from indices
        vertexCache.remove(longId);
    }
    @Override
    public Iterable<Vertex> getVertices() {
        return new CovariantIterable<Vertex>(vertexCache.list());
    }
    @Override
    public Iterable<Vertex> getVertices(String key, Object value) {
        return null;
    }
    @Override
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        XVertex oV = (XVertex) outVertex;
        XVertex iV = (XVertex) inVertex;

        XEdge e = XEdge.of(edgeCounter.getAndIncrement(), oV.getKey(), iV.getKey(), label, this);
        edgeCache.add(e);
        oV.addOutEdge(e.getKey());
        iV.addInEdge(e.getKey());
        return e;
    }
    @Override
    public Edge getEdge(Object id) {
        if (null == id)
            throw ExceptionFactory.edgeIdCanNotBeNull();
        try {
            final Integer intID = (id instanceof Integer) ? (Integer) id
                    : Integer.valueOf(id.toString());
            return edgeCache.get(intID);
        } catch (NumberFormatException | ClassCastException e) {
            log.error("could not use vertex id {}", id);
        }
        return null;
    }
    @Override
    public void removeEdge(Edge edge) {
        checkNotNull(edge);
    }
    @Override
    public Iterable<Edge> getEdges() {
        return new CovariantIterable<Edge>(edgeCache.list());
    }
    @Override
    public Iterable<Edge> getEdges(String key, Object value) {
        return null;
    }
    // =================================
    @Override
    public GraphQuery query() {
        return null;
    }
    // =================================
    // ======== Transaction ============
    @Override
    @Deprecated
    public void stopTransaction(Conclusion conclusion) {
    }
    @Override
    public void shutdown() {

    }
    @Override
    public void commit() {
        vertexCache.merge();
        edgeCache.merge();

    }
    @Override
    public void rollback() {

    }
    // =================================
    // ======== Indices ================
    @Override
    public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass, Parameter[] indexParameters) {
        return null;
    }
    @Override
    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass) {
        return null;
    }
    @Override
    public Iterable<Index<? extends Element>> getIndices() {
        return null;
    }
    @Override
    public void dropIndex(String indexName) {

    }
    @Override
    public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {

    }
    @Override
    public <T extends Element> void createKeyIndex(String key, Class<T> elementClass, Parameter[] indexParameters) {

    }
    @Override
    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        return null;
    }
    // =================================
    public String toString() {
        return StringFactory.graphString(this, "vertices:" + vertexCache.size() + " edges:" + edgeCache.size());
    }
    // =================================
    private final AtomicInteger vertexCounter = new AtomicInteger();
    private final AtomicInteger edgeCounter = new AtomicInteger();

}
