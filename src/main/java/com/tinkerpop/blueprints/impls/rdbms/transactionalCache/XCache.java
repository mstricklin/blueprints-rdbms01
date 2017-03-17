// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.rdbms.transactionalCache;

import com.tinkerpop.blueprints.impls.rdbms.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Queues.newArrayDeque;


@Slf4j
public class XCache {
    public interface ElementCache<E extends XElement> {
        E add(E e);
        E get(int id);
        E remove(int id);
        Iterable<E> list();
        void clear();
        boolean contains(int id);

        void prime(Iterable<E> it);
        void reset();
        void merge(); // throws?
        void dump();
        int size();
        // begin, commit, rollback, merge
    }
    ElementCache<XVertex> vertexCache = new ObjectCache2<>(this);
    ElementCache<XEdge> edgeache = new ObjectCache2<>(this);
    public ElementCache<XVertex> vertexCache() {
        return vertexCache;
    }
    public ElementCache<XEdge> edgeCache() {
        return edgeache;
    }

    public <V extends Keyed & Copyable<V>> ObjectCache<V> objects() {
        // args: graph? read-though provider?
        // local args: this XCache?
        ObjectCache c = new ObjectCache<>(this);
        // cache the cache
        return c;
    }
    // =================================
    public KeyCache keys() {
        // args: graph? read-though provider?
        // local args: this XCache?
        KeyCache c = new KeyCache(this);
        // cache the cache
        return c;
    }
    // =================================
    Action queueAction(Action a) {
        transactionWork.get().add(a);
        return a;
    }
    public void commit() { // throws?

    }
    // =================================
    public void rollback() {

    }

    private final ThreadLocal<Queue<Action>> transactionWork = new ThreadLocal() {
        @Override
        protected Queue<Action> initialValue() {
            return newArrayDeque();
        }
    };
}
