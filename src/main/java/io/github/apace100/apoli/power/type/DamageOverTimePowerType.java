package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.DamageSourceDescription;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.Difficulty;

import java.util.Map;

public class DamageOverTimePowerType extends PowerType {

    public static final RegistryKey<DamageType> GENERIC_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Apoli.identifier("damage_over_time"));

    private final int damageTickInterval;
    private final int beginDamageIn;
    private final float damageAmountEasy;
    private final float damageAmount;
    private final DamageSourceDescription damageSourceDescription;
    private final RegistryKey<DamageType> damageType;
    private final RegistryKey<Enchantment> protectingEnchantmentKey;
    private final float protectionEffectiveness;

    private int outOfDamageTicks;
    private int inDamageTicks;

    private DamageSource damageSource;

    public DamageOverTimePowerType(Power power, LivingEntity entity, int beginDamageIn, int damageInterval, float damageAmountEasy, float damageAmount, DamageSourceDescription damageSourceDescription, RegistryKey<DamageType> damageType, RegistryKey<Enchantment> protectingEnchantmentKey, float protectionEffectiveness) {
        super(power, entity);
        this.damageSourceDescription = damageSourceDescription;
        this.damageType = damageType;
        this.beginDamageIn = beginDamageIn;
        this.damageAmount = damageAmount;
        this.damageAmountEasy = damageAmountEasy;
        this.damageTickInterval = damageInterval;
        this.protectingEnchantmentKey = protectingEnchantmentKey;
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

        if (inDamageTicks - getDamageBegin() >= 0) {
            if((inDamageTicks - getDamageBegin()) % damageTickInterval == 0) {
                DamageSource source = getDamageSource(entity.getDamageSources());
                entity.damage(source, entity.getWorld().getDifficulty() == Difficulty.EASY ? damageAmountEasy : damageAmount);
            }
        }

        inDamageTicks++;

    }

    private DamageSource getDamageSource(DamageSources damageSources) {

        if (damageSource == null) {
            damageSource = MiscUtil.createDamageSource(damageSources, damageSourceDescription, damageType);
        }

        return damageSource;

    }

    public void resetDamage() {

        if(outOfDamageTicks >= 20) {
            inDamageTicks = 0;
        }

        else {
            outOfDamageTicks++;
        }

    }

    @Override
    public void onRespawn() {
        inDamageTicks = 0;
        outOfDamageTicks = 0;
    }

    protected int getProtection() {

        if (protectingEnchantmentKey == null) {
            return 0;
        }

        Registry<Enchantment> enchantmentRegistry = entity.getRegistryManager().get(RegistryKeys.ENCHANTMENT);

        Enchantment protectingEnchantment = enchantmentRegistry.getOrThrow(protectingEnchantmentKey);
        RegistryEntry<Enchantment> protectingEnchantmentEntry = enchantmentRegistry.getEntry(protectingEnchantment);

        Map<EquipmentSlot, ItemStack> potentialItems = protectingEnchantment.getEquipment(entity);

        int accumLevel = 0;
        int items = 0;

        for (ItemStack potentialItem : potentialItems.values()) {

            int level = EnchantmentHelper.getLevel(protectingEnchantmentEntry, potentialItem);
            accumLevel += level;

            if (level > 0) {
                items++;
            }

        }

        return accumLevel + items;

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

        if (tag instanceof NbtCompound nbt) {
            inDamageTicks = nbt.getInt("InDamage");
            outOfDamageTicks = nbt.getInt("OutDamage");
        }

    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("damage_over_time"),
            new SerializableData()
                .add("interval", SerializableDataTypes.POSITIVE_INT, 20)
                .addFunctionedDefault("onset_delay", SerializableDataTypes.INT, data -> data.getInt("interval"))
                .add("damage", SerializableDataTypes.FLOAT)
                .addFunctionedDefault("damage_easy", SerializableDataTypes.FLOAT, data -> data.getFloat("damage"))
                .add("damage_source", ApoliDataTypes.DAMAGE_SOURCE_DESCRIPTION, null)
                .add("damage_type", SerializableDataTypes.DAMAGE_TYPE, GENERIC_DAMAGE)
                .add("protection_enchantment", SerializableDataTypes.ENCHANTMENT, null)
                .add("protection_effectiveness", SerializableDataTypes.FLOAT, 1.0F),
            data -> (power, entity) -> new DamageOverTimePowerType(power, entity,
                data.getInt("onset_delay"),
                data.getInt("interval"),
                data.getFloat("damage_easy"),
                data.getFloat("damage"),
                data.get("damage_source"),
                data.get("damage_type"),
                data.get("protection_enchantment"),
                data.getFloat("protection_effectiveness")
            )
        ).allowCondition();
    }
}
