package io.github.apace100.apoli.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;

import java.util.List;

public class RemovePowerLootFunction extends ConditionalLootFunction {

    public static final Codec<RemovePowerLootFunction> CODEC = RecordCodecBuilder.create(instance -> method_53344(instance)
        .and(EquipmentSlot.CODEC.fieldOf("slot").forGetter(RemovePowerLootFunction::getSlot))
        .and(Identifier.CODEC.fieldOf("power").forGetter(RemovePowerLootFunction::getPowerId))
        .apply(instance, RemovePowerLootFunction::new));
    public static final LootFunctionType TYPE = new LootFunctionType(CODEC);

    private final EquipmentSlot slot;
    private final Identifier powerId;

    private RemovePowerLootFunction(List<LootCondition> conditions, EquipmentSlot slot, Identifier powerId) {
        super(conditions);
        this.slot = slot;
        this.powerId = powerId;
    }

    @Override
    public LootFunctionType getType() {
        return TYPE;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        StackPowerUtil.removePower(stack, slot, powerId);
        return stack;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    public Identifier getPowerId() {
        return powerId;
    }

}
