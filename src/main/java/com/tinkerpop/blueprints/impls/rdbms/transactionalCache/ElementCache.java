// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.rdbms.transactionalCache;

import com.tinkerpop.blueprints.impls.rdbms.XElement;

import java.util.Iterator;

public interface ElementCache<E extends XElement> {
    E add(E e);
    E get(Long l);
    void update(E e);
    E remove(Long l);
    Iterator<E> list();
    void clear();
    boolean contains(Long key);

    void prime(Iterator<E> it);
    void reset();
    // begin, commit, rollback, merge
}