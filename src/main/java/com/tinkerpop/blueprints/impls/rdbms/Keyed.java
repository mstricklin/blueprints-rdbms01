// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.rdbms;

import com.google.common.base.Function;

public interface Keyed {
    int getKey();
    final Function<Keyed, Integer> extractKey = new Function<Keyed, Integer>() {
        @Override
        public Integer apply(Keyed v) {
            return v.getKey();
        }
    };

}
