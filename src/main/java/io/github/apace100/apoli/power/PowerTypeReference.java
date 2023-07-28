package io.github.apace100.apoli.power;

import com.google.gson.JsonElement;
import io.github.apace100.apoli.power.factory.PowerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class PowerTypeReference<T extends Power> extends PowerType<T> {

    private PowerType<T> referencedPowerType;
    private final Identifier currentId;
    private final JsonElement jsonData;

    protected PowerTypeReference(Identifier id, Identifier currentId, JsonElement jsonObject) {
        super(id, null);
        this.currentId = currentId;
        this.jsonData = jsonObject;
    }

    public PowerTypeReference(Identifier id) {
        this(id, null, null);
    }

    @Override
    public PowerFactory<T>.Instance getFactory() {
        getReferencedPowerType();
        if(referencedPowerType == null) {
            return null;
        }
        return referencedPowerType.getFactory();
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
        return referencedPowerType.get(entity);
    }

    @Nullable
    protected JsonElement getJsonData() {
        return jsonData;
    }

    @Nullable
    protected Identifier getCurrentId() {
        return currentId;
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
        return referencedPowerType;
    }

    private boolean isReferenceInvalid() {
        if(referencedPowerType != null) {
            if(PowerTypeRegistry.contains(referencedPowerType.getIdentifier())) {
                PowerType<T> type = PowerTypeRegistry.get(referencedPowerType.getIdentifier());
                return type != referencedPowerType;
            }
        }
        return true;
    }
}
