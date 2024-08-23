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

    public static PowerReference of(String namespace, String path) {
        return new PowerReference(Identifier.of(namespace, path));
    }

    public static PowerReference of(String str) {
        return new PowerReference(Identifier.of(str));
    }

    @Override
    public PowerTypeFactory<? extends PowerType>.Instance getFactoryInstance() {
        return this.getReferenceStrict().getFactoryInstance();
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
        return PowerManager
            .getOptional(this.getId())
            .orElse(null);
    }

    public Power getReferenceStrict() {
        return PowerManager.get(this.getId());
    }

    @Override
    public void validate() throws Exception {
        this.getReferenceStrict();
    }

}
