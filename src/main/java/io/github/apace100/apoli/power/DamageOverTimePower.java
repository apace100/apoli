package io.github.apace100.apoli.power;

import io.github.apace100.calio.mixin.DamageSourceAccessor;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.world.Difficulty;

import java.util.Map;

public class DamageOverTimePower extends VariableIntPower {

    public static final DamageSource GENERIC_DAMAGE = ((DamageSourceAccessor)((DamageSourceAccessor)DamageSourceAccessor.createDamageSource("genericDamageOverTime")).callSetBypassesArmor()).callSetUnblockable();

    private final int damageTickInterval;
    private final int beginDamageIn;
    private final float damageAmountEasy;
    private final float damageAmount;
    private final DamageSource damageSource;
    private final Enchantment protectingEnchantment;
    private final float protectionEffectiveness;

    private int outOfDamageTicks;

    public DamageOverTimePower(PowerType<?> type, LivingEntity entity, int beginDamageIn, int damageInterval, float damageAmountEasy, float damageAmount, DamageSource damageSource, Enchantment protectingEnchantment, float protectionEffectiveness) {
        super(type, entity, beginDamageIn, 0, Math.max(damageInterval, beginDamageIn));
        this.damageSource = damageSource;
        this.beginDamageIn = beginDamageIn;
        this.damageAmount = damageAmount;
        this.damageAmountEasy = damageAmountEasy;
        this.damageTickInterval = damageInterval;
        this.protectingEnchantment = protectingEnchantment;
        this.protectionEffectiveness = protectionEffectiveness;
        this.setTicking(true);
    }

    @Override
    public int getMax() {
        return Math.max(super.getMax(), getDamageBegin());
    }

    public int getDamageBegin() {
        int prot = getProtection();
        int delay = (int)(Math.pow(prot * 2, 1.3) * protectionEffectiveness);
        return beginDamageIn + delay * 20;
    }

    @Override
    public void tick() {
        if(this.isActive()) {
            doDamage();
        } else {
            resetDamage();
        }
    }

    public void doDamage() {
        outOfDamageTicks = 0;
        if(getValue() <= 0) {
            setValue(damageTickInterval);
            entity.damage(damageSource, entity.world.getDifficulty() == Difficulty.EASY ? damageAmountEasy : damageAmount);
        } else {
            decrement();
        }
    }

    public void resetDamage() {
        if(outOfDamageTicks >= 20) {
            this.setValue(getDamageBegin());
        } else {
            outOfDamageTicks++;
        }
    }

    private int getProtection() {
        if(protectingEnchantment == null) {
            return 0;
        } else {
            Map<EquipmentSlot, ItemStack> enchantedItems = protectingEnchantment.getEquipment(entity);
            Iterable<ItemStack> iterable = enchantedItems.values();
            int i = 0;
            for (ItemStack itemStack : iterable) {
                i += EnchantmentHelper.getLevel(protectingEnchantment, itemStack);
            }
            return i + enchantedItems.size();
        }
    }
}
