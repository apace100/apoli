package io.github.apace100.apoli.power;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.factory.PowerFactory;
import net.minecraft.util.Identifier;

import java.util.List;

public class MultiplePowerType<T extends Power> extends PowerType<T> {

    private ImmutableList<Identifier> subPowers;

    public MultiplePowerType(Identifier id, PowerFactory<T>.Instance factory) {
        super(id, factory);
    }

    public void setSubPowers(List<Identifier> subPowers) {
        this.subPowers = ImmutableList.copyOf(subPowers);
    }

    public ImmutableList<Identifier> getSubPowers() {
        return subPowers;
    }

    @Override
    public JsonObject toJson() {

        JsonObject jsonObject = super.toJson();
        for (Identifier subPower : subPowers) {

            PowerType<?> subPowerType = PowerTypeRegistry.getNullable(subPower);

            if (subPowerType != null) {
                jsonObject.add(subPower.toString(), subPowerType.toJson());
            }

        }

        return jsonObject;

    }

}
