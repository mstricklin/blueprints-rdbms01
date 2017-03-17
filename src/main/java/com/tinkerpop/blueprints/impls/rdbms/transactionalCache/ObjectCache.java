// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.rdbms.transactionalCache;

import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.impls.rdbms.Copyable;
import com.tinkerpop.blueprints.impls.rdbms.Keyed;
import com.tinkerpop.blueprints.impls.rdbms.transactionalCache.Action.AbstractAction;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;

// vertex cache: Map<Long, Vertex>
// edge cache:   Map<Long, Edge>
// vertex property cache: Map<Long, Map<String, Object>>
// edge property cache: Map<Long, Map<String, Object>>
// key-index: Map<String, Set<Long>>
// index: Map<Pair<K,V>, Set<Long>>
// =================================
// keyindex (key): list of elements having that property-key
// index: external structure, looking up elements based on key-value pair

// Should properties be immutable? An update is a delete old+add new

// TODO: migrate to FastUtil?
// vertex cache: Map<int, Vertex>                          Int2ObjectMap<Vertex>
// edge cache:   Map<int, Edge>                            Int2ObjectMap<Edge>
// vertex property cache: Map<int, Map<String, Object>>    Int2ObjectMap<Map<String, Object>>
// edge property cache: Map<int, Map<String, Object>>      Int2ObjectMap<Map<String, Object>>
// key-index: Map<String, Set<int>>                        Map<String, IntSet>
// index: Map<Pair<K,V>, IntSet>                           Map<Pair<K,V>, IntSet>

// Scenarios:
// 1. underlying modified, merge deleted  SUCCESS - delete underlying
// 2. underlying modified, merge modified SUCCESS - merge modification
// 3. underlying deleted, merge deleted   SUCCESS - no-op
// 4. underlying deleted, merge modified  ERROR
// 5. add with clashing ids (atomic id generator)

// No modification!!!
// 3. underlying deleted, merge deleted   SUCCESS - no-op
// 5. add with clashing ids (atomic id generator)
// 6. new element, merge deleted SUCCESS - need to keep modified/new lists

@Slf4j
public class ObjectCache<V extends Keyed & Copyable<V>> {
    public ObjectCache(XCache store_) {
        store = store_;
    }
    // prime/load
    // add
    // read/get
    // update
    // remove
    // list
    // clear
    // containsKey
    // reset
    // =================================
    public void load(Iterable<V> it) {
        baseline.clear();
        for (V v : it) {
            baseline.put(v.getKey(), v);
        }
        Map<Integer, Boolean> m = newHashMap();
        Collections.newSetFromMap(m);
        Sets.newIdentityHashSet();
    }
    // =================================
    public V add(V v) {
        removed.get().remove(v.getKey());
        revision.get().put(v.getKey(), v);
        return v;
    }
    // =================================
    public V read(long key) {
        if (removed.get().contains(key))
            return null;
        V v = revision.get().get(key);
        return (null != v) ? v : baseline.get(key);
    }
    // =================================
    public V update(int key) {
        V v = baseline.get(key);
        if (null != v) {
            revision.get().put(key, v.copy());
        }
        return v;
    }
    // =================================
    public V remove(int key) {
        log.info("remove {}", key);
        V v = revision.get().remove(key);
        if (null == v) {
            v = baseline.get(key);
        }
        removed.get().add(key);
        return v;
    }
    // =================================
    public Iterable<V> list() {
        Map<Integer, V> revMap  = Maps.filterKeys(revision.get(), not(in(removed.get())));
        Map<Integer, V> baseMap = Maps.filterKeys(baseline,       not(in(removed.get())));
        return concat(baseMap.values(), revMap.values());
    }
    // =================================
    public void clear() {
        removed.get().addAll( baseline.keySet() );
        revision.get().clear();
    }
    // =================================
    public boolean containsKey(long key) {
        if (removed.get().contains(key)) return false;
        return revision.get().containsKey(key) || baseline.containsKey(key);
    }
    // =================================
    public int size() {
        Iterable<Integer> keys = concat(baseline.keySet(), revision.get().keySet());
        Iterable<Integer> filtered = filter(keys, not(in(removed.get())));
        return Iterables.size(filtered);
    }
    // =================================
    protected void reset() {
        revision.get().clear();
        removed.get().clear();
    }
    // =================================
    protected void merge() {
        synchronized (baseline) {
            baseline.putAll(revision.get());
            for (Integer k : removed.get())
                baseline.remove(k);
        }
    }
    // =================================
    public void dump() {
        log.info("===== Baseline ===");
        for (Map.Entry<Integer, V> e: baseline.entrySet()) {
            log.info(" {} => {}", e.getKey(), e.getValue());
        }
        log.info("===== Revision ===");
        for (Map.Entry<Integer, V> e: revision.get().entrySet()) {
            log.info(" {} => {}", e.getKey(), e.getValue());
        }
        log.info("===== Removed ===");
        log.info(" {}", removed.get());
    }
    // =================================
    private final XCache store;
    private final ConcurrentMap<Integer, V> baseline = new MapMaker()
            .concurrencyLevel(4)
            .makeMap();
    private final ThreadLocal<Map<Integer, V>> revision = new ThreadLocal() {
        @Override
        protected Map<Integer, V> initialValue() {
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
