// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.rdbms;

import com.tinkerpop.blueprints.Element;
import lombok.ToString;

import java.util.Set;

@ToString
public abstract class XElement implements Keyed, Element {
    protected XElement(final XElement rhs) {
        id = rhs.id;
        graph = rhs.graph;
    }
    XElement(int id_, final RdbmsGraph graph_) {
        id = id_;
        graph = graph_;
    }

    @Override
    public <T> T getProperty(String key) {
        return null;
    }
    @Override
    public Set<String> getPropertyKeys() {
        return null;
    }
    @Override
    public void setProperty(String key, Object value) {

    }
    @Override
    public <T> T removeProperty(String key) {
        return null;
    }
    // =================================
    public int getKey() {
        return id;
    }
    @Override
    public Object getId() {
        return id;
    }

    // =================================
    protected final int id;
    protected final RdbmsGraph graph;
}
