// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.rdbms.transactionalCache;

import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.impls.rdbms.XElement;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

@Slf4j
public class ObjectCache2<E extends XElement> implements XCache.ElementCache<E> {
    ObjectCache2(XCache c) {
        store = c;
    }
    @Override
    public E add(E e) {
        added.get().put(e.getKey(), e);
        return e;
    }
    @Override
    public E get(int id) {
        if (removed.get().contains(id))
            return null;
        E e = added.get().get(id);
        return (null != e) ? e
                : baseline.get(id);
    }
    @Override
    public E remove(int id) {
        removed.get().add(id);
        added.get().remove(id);
        return null;
    }
    @Override
    public Iterable<E> list() {
        // Use Int2ObjectMap?
        Map<Integer, E> revMap = Maps.filterKeys(added.get(), not(in(removed.get())));
        Map<Integer, E> baseMap = Maps.filterKeys(baseline, not(in(removed.get())));
        return concat(baseMap.values(), revMap.values());
    }
    @Override
    public void clear() {
        removed.get().addAll(baseline.keySet());
        added.get().clear();
    }
    @Override
    public boolean contains(int id) {
        if (removed.get().contains(id))
            return false;
        return baseline.containsKey(id)
                || added.get().containsKey(id);
    }
    @Override
    public synchronized void prime(Iterable<E> it) {
        for (E e : it)
            baseline.put(e.getKey(), e);
    }
    @Override
    public void reset() {
        added.get().clear();
        removed.get().clear();
    }
    @Override
    public void merge() {
        synchronized (baseline) {
            baseline.putAll(added.get());
            for (Integer id : removed.get())
                baseline.remove(id);
        }
        added.get().clear();
        removed.get().clear();
    }
    @Override
    public void dump() {
        if ( ! baseline.isEmpty() ) {
            log.info("Baseline");
            for (Map.Entry<Integer, E> e : baseline.entrySet()) {
                log.info("\t{} => {}", e.getKey(), e.getValue());
            }
        }
        if ( ! added.get().isEmpty() ) {
            log.info("Added");
            for (Map.Entry<Integer, E> e : added.get().entrySet()) {
                log.info("\t{} => {}", e.getKey(), e.getValue());
            }
        }
        if ( ! removed.get().isEmpty() ) {
            log.info("Removed");
            log.info("\t{}", removed.get());
        }
    }
    @Override
    public int size() {
        Iterable<Integer> keys = concat(baseline.keySet(), added.get().keySet());
        Iterable<Integer> filtered = filter(keys, not(in(removed.get())));
        return Iterables.size(filtered);
    }
    // =================================
    private final XCache store;
    private final ConcurrentMap<Integer, E> baseline = new MapMaker()
            .concurrencyLevel(4)
            .makeMap();
    private final ThreadLocal<Map<Integer, E>> added = new ThreadLocal() {
        @Override
        protected Map<Integer, E> initialValue() {
            return newHashMap();
        }
    };
    private final ThreadLocal<Set<Integer>> removed = new ThreadLocal() {
        @Override
        protected Set<Integer> initialValue() {
            return newHashSet();
        }
    };
}
