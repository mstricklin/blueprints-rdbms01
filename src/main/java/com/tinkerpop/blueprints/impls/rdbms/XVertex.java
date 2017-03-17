// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.rdbms;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@ToString(callSuper=true)
@Slf4j
public class XVertex extends XElement implements Vertex, Copyable<XVertex> {
    public static XVertex of(int id, final RdbmsGraph graph) {
        return new XVertex(id, graph);
    }
    private XVertex(final XVertex rhs) {
        super(rhs);
    }
    private XVertex(int id, final RdbmsGraph graph) {
        super(id, graph);
    }
    // =================================
    @Override
    public Iterable<Edge> getEdges(Direction direction, String... labels) {
        return Collections.emptyList();
    }
    @Override
    public Iterable<Vertex> getVertices(Direction direction, String... labels) {
        return null;
    }
    @Override
    public VertexQuery query() {
        return null;
    }
    @Override
    public Edge addEdge(String label, Vertex inVertex) {
        return null;
    }
    // =================================
    void addOutEdge(int edgeId) {
        outEdges.add(edgeId);
    }
    // =================================
    void addInEdge(int edgeId) {
        inEdges.add(edgeId);
    }
    // =================================


    @Override
    public void remove() {

    }
    @Override
    public XVertex copy() {
        return new XVertex(this);
    }
    // =================================

    // Use IntSet?
    private final Set<Integer> outEdges = newHashSet();
    private final Set<Integer> inEdges  = newHashSet();
}
