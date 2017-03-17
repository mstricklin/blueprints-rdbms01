// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.rdbms.transactionalCache;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.Set;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Iterables.concat;

public class KeyCache {
    public KeyCache(XCache store_) {
        store = store_;
    }
    // prime/load
    // add
    // remove
    //
    // =================================
    public void load(Iterable<Long> it) {
        baseline = newConcurrentHashSet(it);
    }
    // =================================
    public boolean add(Long l) {
        boolean b0 = removed.get().remove(l);
        boolean b1 = revision.get().add(l);
        if (b0)
            return false;
        return b1 || baseline.contains(l);
    }
    // =================================
    public boolean remove(Long l) {
        boolean b0 = removed.get().add(l);
        boolean b1 = revision.get().remove(l);
        if (b0)
            return true;  // TODO: Arrrgh! what's the semantics of this return?
        return false;
    }
    // =================================
    public Iterable<Long> list() {
        Set<Long> present = Sets.union(revision.get(), baseline);
        return filter(present, not(in(removed.get())));
    }
    // =================================
    public void clear() {
        removed.get().addAll( baseline );
        revision.get().clear();
    }
    // =================================
    public int size() {
        Iterable<Long> keys = concat(baseline, revision.get());
        Iterable<Long> filtered = filter(keys, not(in(removed.get())));
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
            baseline.addAll(revision.get());
            baseline.removeAll(removed.get());
        }
    }
    // =================================
    private final XCache store;
    private Set<Long> baseline = newConcurrentHashSet();
    private final ThreadLocal<Set<Long>> revision = new ThreadLocal() {
        @Override
        protected Set<Long> initialValue() {
            return newHashSet();
        }
    };
    private final ThreadLocal<Set<Long>> removed = new ThreadLocal() {
        @Override
        protected Set<Long> initialValue() {
            return newHashSet();
        }
    };

}
