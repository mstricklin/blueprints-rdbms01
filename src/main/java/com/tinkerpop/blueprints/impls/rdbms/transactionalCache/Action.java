// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.rdbms.transactionalCache;


import com.tinkerpop.blueprints.impls.rdbms.Copyable;
import com.tinkerpop.blueprints.impls.rdbms.Keyed;

public interface Action<V extends Keyed & Copyable<V>> {
    V apply() throws CacheException;

    class CacheException extends Exception {
    }

    abstract class AbstractAction<V extends Keyed & Copyable<V>> implements Action {
        AbstractAction(ObjectCache<V> cache_) {
            cache = cache_;
        }
        protected final ObjectCache<V> cache;
    }

}
