package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RelativeItemCooldownItemConditionType extends ItemConditionType {

    public static final TypedDataObjectFactory<RelativeItemCooldownItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataType.boundNumber(SerializableDataTypes.FLOAT, 0F, 1F)),
        data -> new RelativeItemCooldownItemConditionType(
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Comparison comparison;
    private final float compareTo;

    public RelativeItemCooldownItemConditionType(Comparison comparison, float compareTo) {
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(World world, ItemStack stack) {

        if (!stack.isEmpty() && ((EntityLinkedItemStack) stack).apoli$getEntity(true) instanceof PlayerEntity player) {

            ItemCooldownManager cooldownManager = player.getItemCooldownManager();
            float cooldownProgress = cooldownManager.getCooldownProgress(stack.getItem(), 0F);

            return comparison.compare(cooldownProgress, compareTo);

        }

        else {
            return false;
        }

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return ItemConditionTypes.RELATIVE_ITEM_COOLDOWN;
    }

}
