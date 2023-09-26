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

public class AddPowerLootFunction extends ConditionalLootFunction {

    public static final Codec<AddPowerLootFunction> CODEC = RecordCodecBuilder.create(instance -> method_53344(instance)
        .and(EquipmentSlot.CODEC.fieldOf("slot").forGetter(AddPowerLootFunction::getSlot))
        .and(Identifier.CODEC.fieldOf("power").forGetter(AddPowerLootFunction::getPowerId))
        .and(Codec.BOOL.optionalFieldOf("hidden", false).forGetter(AddPowerLootFunction::isHidden))
        .and(Codec.BOOL.optionalFieldOf("negative", false).forGetter(AddPowerLootFunction::isNegative))
        .apply(instance, AddPowerLootFunction::new));
    public static final LootFunctionType TYPE = new LootFunctionType(CODEC);

    private final EquipmentSlot slot;
    private final Identifier powerId;
    private final boolean hidden;
    private final boolean negative;

    private AddPowerLootFunction(List<LootCondition> conditions, EquipmentSlot slot, Identifier powerId, boolean hidden, boolean negative) {
        super(conditions);
        this.slot = slot;
        this.powerId = powerId;
        this.hidden = hidden;
        this.negative = negative;
    }

    @Override
    public LootFunctionType getType() {
        return TYPE;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        StackPowerUtil.addPower(stack, slot, powerId, hidden, negative);
        return stack;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    public Identifier getPowerId() {
        return powerId;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isNegative() {
        return negative;
    }

}
