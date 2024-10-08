package io.github.apace100.apoli.power;

import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.PowerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PowerReference extends Power {

    protected PowerReference(Identifier id) {
        super(id, null, null, null, true);
    }

    public static PowerReference of(Identifier id) {
        return new PowerReference(id);
    }

    public static PowerReference resource(Identifier id) {
        return new PowerReference(id) {

            @Override
            public void validate() throws Exception {
                PowerUtil.validateResource(create(null)).getOrThrow();
            }

        };
    }

    @Override
    public PowerType create(@Nullable LivingEntity entity) {
        return getFactoryInstance().apply(getStrictReference(), entity);
    }

    @Override
    public PowerTypeFactory<? extends PowerType>.Instance getFactoryInstance() {
        return this.getStrictReference().getFactoryInstance();
    }

    @Nullable
    @Override
    public PowerType getType(Entity entity) {
        Power power = this.getReference();
        return power != null
            ? power.getType(entity)
            : null;
    }

    @Nullable
    public Power getReference() {
        return getOptionalReference().orElse(null);
    }

    public Optional<Power> getOptionalReference() {
        return PowerManager.getOptional(this.getId());
    }

    public Power getStrictReference() {
        return PowerManager.get(this.getId());
    }

    @Override
    public void validate() throws Exception {
        getStrictReference();
    }

}
