// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package com.tinkerpop.blueprints.impls.rdbms;

public interface Copyable<C extends Copyable<C>> {
    // intention is shallow...
    C copy();
}
