package io.github.apace100.apoli.power;

import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.factory.PowerTypes;
import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class PowerReference extends Power {

    public PowerReference(Identifier id) {
        super(null, DATA.instance()
            .set("id", id)
            .set(TYPE_KEY, PowerTypes.SIMPLE)
            .set("name", Text.empty())
            .set("description", Text.empty())
            .set("hidden", true));
    }

    @Nullable
    @Override
    public PowerTypeFactory<? extends PowerType>.Instance getFactoryInstance() {
        Power power = this.getReference();
        return power != null
            ? power.getFactoryInstance()
            : null;
    }

    @Nullable
    @Override
    public PowerType get(Entity entity) {
        Power power = this.getReference();
        return power != null
            ? power.get(entity)
            : null;
    }

    @Override
    public boolean isMultiple() {
        return false;
    }

    @Override
    public boolean isSubPower() {
        return false;
    }

    @Nullable
    public Power getReference() {
        return PowerManager
            .getOptional(this.getId())
            .orElse(null);
    }

    @Override
    public void validate() throws Exception {
        PowerManager.get(this.getId());
    }

}
