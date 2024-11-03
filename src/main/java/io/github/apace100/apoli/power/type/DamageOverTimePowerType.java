package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class DamageOverTimePowerType extends PowerType {

    public static final RegistryKey<DamageType> GENERIC_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Apoli.identifier("damage_over_time"));

    public static final TypedDataObjectFactory<DamageOverTimePowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("damage_type", SerializableDataTypes.DAMAGE_TYPE, GENERIC_DAMAGE)
            .add("protection_enchantment", SerializableDataTypes.ENCHANTMENT.optional(), Optional.empty())
            .add("protection_effectiveness", SerializableDataTypes.FLOAT, 1.0F)
            .add("damage", SerializableDataTypes.FLOAT)
            .addFunctionedDefault("damage_easy", SerializableDataTypes.FLOAT, data -> data.get("damage"))
            .add("interval", SerializableDataTypes.POSITIVE_INT, 20)
            .addFunctionedDefault("onset_delay", SerializableDataTypes.POSITIVE_INT, data -> data.get("interval")),
        (data, condition) -> new DamageOverTimePowerType(
            data.get("damage_type"),
            data.get("protection_enchantment"),
            data.get("protection_effectiveness"),
            data.get("damage"),
            data.get("damage_easy"),
            data.get("interval"),
            data.get("onset_delay"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("damage_type", powerType.damageType)
            .set("protection_enchantment", powerType.protectionEnchantmentKey)
            .set("protection_effectiveness", powerType.protectionEffectiveness)
            .set("damage", powerType.damageAmount)
            .set("damage_easy", powerType.damageAmountEasy)
            .set("interval", powerType.damageInterval)
            .set("onset_delay", powerType.damageOnsetDelay)
    );

    private final RegistryKey<DamageType> damageType;
    private final Optional<RegistryKey<Enchantment>> protectionEnchantmentKey;

    private final float protectionEffectiveness;

    private final float damageAmount;
    private final float damageAmountEasy;

    private final int damageInterval;
    private final int damageOnsetDelay;

    private int outOfDamageTicks = 0;
    private int inDamageTicks = 0;

    public DamageOverTimePowerType(RegistryKey<DamageType> damageType, Optional<RegistryKey<Enchantment>> protectionEnchantmentKey, float protectionEffectiveness, float damageAmountEasy, float damageAmount, int damageInterval, int damageOnsetDelay, Optional<EntityCondition> condition) {
        super(condition);
        this.damageType = damageType;
        this.protectionEnchantmentKey = protectionEnchantmentKey;
        this.protectionEffectiveness = protectionEffectiveness;
        this.damageAmountEasy = damageAmountEasy;
        this.damageAmount = damageAmount;
        this.damageInterval = damageInterval;
        this.damageOnsetDelay = damageOnsetDelay;
        this.setTicking(true);
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.DAMAGE_OVER_TIME;
    }

    public int getDamageBegin() {

        int protection = getProtection();
        int delay = (int) (Math.pow(protection * 2, 1.3) * protectionEffectiveness);

        return damageOnsetDelay + delay * 20;

    }

    @Override
    public void serverTick() {

        if (this.isActive()) {
            doDamage();
        }

        else {
            resetDamage();
        }

    }

    public void doDamage() {

        LivingEntity holder = getHolder();
        this.outOfDamageTicks = 0;

        if (inDamageTicks++ - getDamageBegin() >= 0 && (inDamageTicks - getDamageBegin()) % damageInterval == 0) {

            DamageSource damageSource = holder.getDamageSources().create(damageType);
            float amount = holder.getWorld().getDifficulty() == Difficulty.EASY
                ? damageAmountEasy
                : damageAmount;

            holder.damage(damageSource, amount);

        }

    }

    public void resetDamage() {

        if (outOfDamageTicks >= 20) {
            this.inDamageTicks = 0;
        }

        else {
            this.outOfDamageTicks++;
        }

    }

    @Override
    public void onRespawn() {
        this.inDamageTicks = 0;
        this.outOfDamageTicks = 0;
    }

    protected int getProtection() {

        if (protectionEnchantmentKey.isEmpty()) {
            return 0;
        }

        LivingEntity holder = getHolder();
        Registry<Enchantment> enchantmentRegistry = holder.getRegistryManager().get(RegistryKeys.ENCHANTMENT);

        Enchantment protectingEnchantment = enchantmentRegistry.getOrThrow(protectionEnchantmentKey.get());
        RegistryEntry<Enchantment> protectingEnchantmentEntry = enchantmentRegistry.getEntry(protectingEnchantment);

        Map<EquipmentSlot, ItemStack> potentialItems = protectingEnchantment.getEquipment(holder);

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

}
