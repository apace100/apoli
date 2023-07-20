package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.DamageSourceDescription;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.Difficulty;

import java.util.Map;

public class DamageOverTimePower extends Power {

    public static final RegistryKey<DamageType> GENERIC_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Apoli.identifier("damage_over_time"));

    private final int damageTickInterval;
    private final int beginDamageIn;
    private final float damageAmountEasy;
    private final float damageAmount;
    private final DamageSourceDescription damageSourceDescription;
    private final RegistryKey<DamageType> damageType;
    private final Enchantment protectingEnchantment;
    private final float protectionEffectiveness;

    private int outOfDamageTicks;
    private int inDamageTicks;

    private DamageSource damageSource;

    public DamageOverTimePower(PowerType<?> type, LivingEntity entity, int beginDamageIn, int damageInterval, float damageAmountEasy, float damageAmount, DamageSourceDescription damageSourceDescription, RegistryKey<DamageType> damageType, Enchantment protectingEnchantment, float protectionEffectiveness) {
        super(type, entity);
        this.damageSourceDescription = damageSourceDescription;
        this.damageType = damageType;
        this.beginDamageIn = beginDamageIn;
        this.damageAmount = damageAmount;
        this.damageAmountEasy = damageAmountEasy;
        this.damageTickInterval = damageInterval;
        this.protectingEnchantment = protectingEnchantment;
        this.protectionEffectiveness = protectionEffectiveness;
        this.setTicking(true);
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
        if(inDamageTicks - getDamageBegin() >= 0) {
            if((inDamageTicks - getDamageBegin()) % damageTickInterval == 0) {
                DamageSource source = getDamageSource(entity.getDamageSources());
                entity.damage(source, entity.getWorld().getDifficulty() == Difficulty.EASY ? damageAmountEasy : damageAmount);
            }
        }
        inDamageTicks++;
    }

    private DamageSource getDamageSource(DamageSources damageSources) {
        if(damageSource == null) {
            damageSource = MiscUtil.createDamageSource(damageSources, damageSourceDescription, damageType);
        }
        return damageSource;
    }

    public void resetDamage() {
        if(outOfDamageTicks >= 20) {
            inDamageTicks = 0;
        } else {
            outOfDamageTicks++;
        }
    }

    @Override
    public void onRespawn() {
        inDamageTicks = 0;
        outOfDamageTicks = 0;
    }

    private int getProtection() {
        if(protectingEnchantment == null) {
            return 0;
        } else {
            Map<EquipmentSlot, ItemStack> enchantedItems = protectingEnchantment.getEquipment(entity);
            Iterable<ItemStack> iterable = enchantedItems.values();
            int i = 0;
            int items = 0;
            for (ItemStack itemStack : iterable) {
                int enchLevel = EnchantmentHelper.getLevel(protectingEnchantment, itemStack);
                i += enchLevel;
                if(enchLevel > 0)
                    items++;
            }
            return i + items;
        }
    }

    @Override
    public NbtElement toTag() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("InDamage", inDamageTicks);
        nbt.putInt("OutDamage", outOfDamageTicks);
        return nbt;
    }

    @Override
    public void fromTag(NbtElement tag) {
        if(tag instanceof NbtCompound nbt) {
            inDamageTicks = nbt.getInt("InDamage");
            outOfDamageTicks = nbt.getInt("OutDamage");
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("damage_over_time"),
            new SerializableData()
                .add("interval", SerializableDataTypes.INT, 20)
                .addFunctionedDefault("onset_delay", SerializableDataTypes.INT, data -> data.getInt("interval"))
                .add("damage", SerializableDataTypes.FLOAT)
                .addFunctionedDefault("damage_easy", SerializableDataTypes.FLOAT, data -> data.getFloat("damage"))
                .add("damage_source", ApoliDataTypes.DAMAGE_SOURCE_DESCRIPTION, null)
                .add("damage_type", SerializableDataTypes.DAMAGE_TYPE, GENERIC_DAMAGE)
                .add("protection_enchantment", SerializableDataTypes.ENCHANTMENT, null)
                .add("protection_effectiveness", SerializableDataTypes.FLOAT, 1.0F),
            data ->
                (type, player) -> new DamageOverTimePower(type, player,
                    data.getInt("onset_delay"),
                    data.getInt("interval"),
                    data.getFloat("damage_easy"),
                    data.getFloat("damage"),
                    data.get("damage_source"),
                    data.get("damage_type"),
                    data.get("protection_enchantment"),
                    data.getFloat("protection_effectiveness")))
            .allowCondition();
    }
}
