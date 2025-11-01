package io.github.apace100.apoli.power.factory;

import io.github.apace100.apoli.power.Power;

@FunctionalInterface
public interface PowerFactorySupplier<T extends Power> {

    PowerFactory<T> createFactory();
}
