// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.rdbms;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString(callSuper=true)
@Slf4j
public class XEdge extends XElement implements Edge, Copyable<XEdge> {

    public static XEdge of(int id, long outID, long inID, String label, final RdbmsGraph graph) {
        return new XEdge(id, outID, inID, label, graph);
    }
    // =================================
    private XEdge(int id, long outID, long inID, final String label_, final RdbmsGraph graph) {
        super(id, graph);
        label = label_;
        outVertexID = outID;
        inVertexID = inID;
    }
    private XEdge(final XEdge rhs) {
        super(rhs);
        label = rhs.label;
        outVertexID = rhs.outVertexID;
        inVertexID = rhs.inVertexID;
    }
    // =================================
    @Override
    public Vertex getVertex(Direction direction) throws IllegalArgumentException {
        if (direction.equals(Direction.OUT))
            return getOutVertex();
        if (direction.equals(Direction.IN))
            return getInVertex();
        throw ExceptionFactory.bothIsNotSupported();

    }
    // =================================
    XVertex getOutVertex() {
        return (XVertex)graph.getVertex(outVertexID);
    }
    XVertex getInVertex() {
        return (XVertex)graph.getVertex(inVertexID);
    }
    // =================================
    @Override
    public String getLabel() {
        return label;
    }
    @Override
    public void remove() {
        graph.removeEdge(this);
    }
    @Override
    public XEdge copy() {
        return new XEdge(this);
    }
    // =================================
    private final String label;
    private final long outVertexID;
    private final long inVertexID;
}
