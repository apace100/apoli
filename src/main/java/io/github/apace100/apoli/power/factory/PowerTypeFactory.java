package io.github.apace100.apoli.power.factory;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.factory.Factory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;

public class PowerTypeFactory<P extends PowerType> implements Factory {

    protected final Identifier id;

    protected SerializableData serializableData;
    protected Function<SerializableData.Instance, BiFunction<Power, LivingEntity, P>> constructorFactory;

    private boolean hasConditions = false;

    public PowerTypeFactory(Identifier id, SerializableData serializableData, Function<SerializableData.Instance, BiFunction<Power, LivingEntity, P>> constructorFactory) {
        this.id = id;
        this.serializableData = serializableData.copy();
        this.constructorFactory = constructorFactory;
    }

    @Override
    public Identifier getSerializerId() {
        return id;
    }

    @Override
    public SerializableData getSerializableData() {
        return serializableData;
    }

    public PowerTypeFactory<P> allowCondition() {

        if (!hasConditions) {
            hasConditions = true;
            serializableData.add("condition", ApoliDataTypes.ENTITY_CONDITION, null);
        }

        return this;

    }

    @Override
    public Instance receive(RegistryByteBuf buffer) {
        return new Instance(serializableData.receive(buffer));
    }

    @Override
    public Instance fromData(SerializableData.Instance data) {
        return new Instance(data);
    }

    public class Instance implements Factory.Instance, BiFunction<Power, LivingEntity, P> {

        protected final SerializableData.Instance data;
        protected final BiFunction<Power, LivingEntity, P> constructor;

        protected Instance(SerializableData.Instance data) {
            this.constructor = constructorFactory.apply(data);
            this.data = data;
        }

        @Override
        public SerializableData.Instance getData() {
            return data;
        }

        @Override
        public PowerTypeFactory<P> getFactory() {
            return PowerTypeFactory.this;
        }

        @Override
        public P apply(Power pPower, LivingEntity livingEntity) {

            P power = constructor.apply(pPower, livingEntity);

            if (hasConditions && data.isPresent("condition")) {
                power.addCondition(data.get("condition"));
            }

            return power;

        }

        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }

            else if (obj instanceof PowerTypeFactory<?>.Instance other) {
                return this.getFactory().equals(other.getFactory())
                    && this.getData().equals(other.getData());
            }

            else {
                return false;
            }

        }

    }

}
