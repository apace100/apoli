package io.github.apace100.apoli.power;

import io.github.apace100.apoli.power.factory.PowerFactory;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class PowerTypeReference extends PowerType {

    public PowerTypeReference(Identifier id) {
        super(null, DATA.instance()
            .set("id", id)
            .set("name", Text.empty())
            .set("description", Text.empty())
            .set("hidden", true));
    }

    @Nullable
    @Override
    public PowerFactory<? extends Power>.Instance getFactoryInstance() {
        PowerType powerType = this.getReference();
        return powerType != null
            ? powerType.getFactoryInstance()
            : null;
    }

    @Nullable
    @Override
    public Power get(Entity entity) {
        PowerType powerType = this.getReference();
        return powerType != null
            ? powerType.get(entity)
            : null;
    }

    @Nullable
    public PowerType getReference() {
        return PowerTypeManager
            .getOptional(this.getId())
            .orElse(null);
    }

    @Override
    public void validate() throws Exception {
        PowerTypeManager.get(this.getId());
    }

}
