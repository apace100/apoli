package io.github.apace100.apoli.power;

import io.github.apace100.apoli.power.factory.PowerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

@SuppressWarnings("unchecked")
public class PowerTypeReference<T extends Power> extends PowerType<T> {

    private PowerType<?> referencedPowerType;

    public PowerTypeReference(Identifier id) {
        super(id, null);
    }

    @Override
    public PowerFactory<T>.Instance getFactory() {
        getReferencedPowerType();
        if(referencedPowerType == null) {
            return null;
        }
        return ((PowerType<T>) referencedPowerType).getFactory();
    }

    @Override
    public boolean isActive(Entity entity) {
        getReferencedPowerType();
        if(referencedPowerType == null) {
            return false;
        }
        return referencedPowerType.isActive(entity);
    }

    @Override
    public T get(Entity entity) {
        getReferencedPowerType();
        if(referencedPowerType == null) {
            return null;
        }
        return ((PowerType<T>) referencedPowerType).get(entity);
    }

    public PowerType<T> getReferencedPowerType() {
        if(isReferenceInvalid()) {
            try {
                referencedPowerType = null;
                referencedPowerType = PowerTypeRegistry.get(getIdentifier());
            } catch(IllegalArgumentException e) {
                //cooldown = 600;
                //Apoli.LOGGER.warn("Invalid PowerTypeReference: no power type exists with id \"" + getIdentifier() + "\"");
            }
        }
        return (PowerType<T>) referencedPowerType;
    }

    private boolean isReferenceInvalid() {
        if(referencedPowerType != null) {
            if(PowerTypeRegistry.contains(referencedPowerType.getIdentifier())) {
                PowerType<T> type = (PowerType<T>) PowerTypeRegistry.get(referencedPowerType.getIdentifier());
                return type != referencedPowerType;
            }
        }
        return true;
    }
}
