package io.github.apace100.apoli.power.factory;

import io.github.apace100.apoli.power.type.PowerType;

@FunctionalInterface
public interface PowerFactorySupplier<T extends PowerType> {

    default PowerTypeFactory<T> createFactory() {
        return getFactory();
    }

    PowerTypeFactory<T> getFactory();

}
