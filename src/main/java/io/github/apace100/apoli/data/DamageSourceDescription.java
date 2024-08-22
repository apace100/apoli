package io.github.apace100.apoli.data;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.NameMutableDamageSource;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

public class DamageSourceDescription {

    public static final SerializableData DATA = new SerializableData()
            .add("name", SerializableDataTypes.STRING);

    private static final Multimap<String, TagKey<DamageType>> STRING_TO_TAGS = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
    private static final Map<TagKey<DamageType>, String> TAG_TO_STRING = new HashMap<>();

    private static final int TAG_COUNT;

    static {

        registerDamageTypeTagMapping("bypasses_armor", DamageTypeTags.BYPASSES_ARMOR);
        registerDamageTypeTagMapping("fire", DamageTypeTags.IS_FIRE);
        registerDamageTypeTagMapping("unblockable", DamageTypeTags.BYPASSES_SHIELD);
        registerDamageTypeTagMapping("magic", DamageTypeTags.WITCH_RESISTANT_TO);
        registerDamageTypeTagMapping("magic", DamageTypeTags.AVOIDS_GUARDIAN_THORNS);
        registerDamageTypeTagMapping("out_of_world", DamageTypeTags.BYPASSES_INVULNERABILITY);
        registerDamageTypeTagMapping("projectile", DamageTypeTags.IS_PROJECTILE);
        registerDamageTypeTagMapping("explosive", DamageTypeTags.IS_EXPLOSION);

        for (String jsonKey : STRING_TO_TAGS.keySet()) {
            DATA.add(jsonKey, SerializableDataTypes.BOOLEAN, false);
        }

        TAG_COUNT = STRING_TO_TAGS.values().size();

    }

    private static void registerDamageTypeTagMapping(String jsonKey, TagKey<DamageType> tag) {
        STRING_TO_TAGS.put(jsonKey, tag);
        TAG_TO_STRING.put(tag, jsonKey);
    }

    private final String name;
    private final Set<TagKey<DamageType>> desiredDamageTypeTags = new HashSet<>();

    private RegistryKey<DamageType> damageType;

    public DamageSourceDescription(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<TagKey<DamageType>> getDamageTypeTags() {
        return Set.copyOf(desiredDamageTypeTags);
    }

    public void addDamageTypeTag(TagKey<DamageType> damageTypeTag) {
        desiredDamageTypeTags.add(damageTypeTag);
    }

    public RegistryKey<DamageType> getDamageType(DamageSources damageSources) {
        if(damageType == null) {
            findBestMatchingDamageType(damageSources);
        }
        return damageType;
    }

    public DamageSource create(DamageSources damageSources) {
        DamageSource damageSource = damageSources.create(getDamageType(damageSources));
        overwriteDamageSourceMessageKey(damageSource);
        return damageSource;
    }

    public DamageSource create(DamageSources damageSources, Entity attacker) {
        DamageSource damageSource = damageSources.create(getDamageType(damageSources), attacker);
        overwriteDamageSourceMessageKey(damageSource);
        return damageSource;
    }

    public DamageSource create(DamageSources damageSources, Entity source, Entity attacker) {
        DamageSource damageSource = damageSources.create(getDamageType(damageSources), source, attacker);
        overwriteDamageSourceMessageKey(damageSource);
        return damageSource;
    }

    private void overwriteDamageSourceMessageKey(DamageSource source) {
        ((NameMutableDamageSource)source).apoli$setName(name);
    }

    private void findBestMatchingDamageType(DamageSources damageSources) {

        var damageTypeRegistryKey = damageSources.registry.getKey();
        Triple<? extends RegistryEntry<DamageType>, Integer, Integer> bestMatch = damageSources.registry
            .streamEntries()
            .map(ref -> Triple.of(ref, this.getTagMatches(ref), this.getNameMatches(ref, damageSources)))
            .max(Comparator.comparingInt(tri -> tri.getMiddle() + tri.getRight()))
            .orElseThrow(() -> new NoSuchElementException("Registry \"" + damageTypeRegistryKey.getValue() + "\" was empty or not loaded yet!"));

        int bestMatchTagCount = bestMatch.getMiddle();
        int bestMatchNameCount = bestMatch.getRight();

        RegistryKey<DamageType> bestMatchDamageType = bestMatch.getLeft()
            .getKey()
            .orElseThrow();

        if (bestMatchTagCount < TAG_COUNT || bestMatchNameCount == 0) {
            Apoli.LOGGER.warn("Couldn't find the perfect damage type for legacy damage source \"{}\". Best match: {} out of {} tags with damage type \"{}\". Consider creating your own custom damage type.",
                this.getName(),
                bestMatchTagCount,
                TAG_COUNT,
                bestMatchDamageType.getValue());
        }

        this.damageType = bestMatchDamageType;

    }

    private int getNameMatches(RegistryEntry<DamageType> damageTypeEntry, DamageSources damageSources) {
        return damageTypeEntry.getKeyOrValue()
            .map(damageSources.registry::getOrEmpty, Optional::of)
            .filter(damageType -> this.getName().equals(damageType.msgId()))
            .map(damageType -> 100)
            .orElse(0);
    }

    private int getTagMatches(RegistryEntry<DamageType> damageType) {
        return (int) STRING_TO_TAGS.values()
            .stream()
            .filter(tag -> damageType.isIn(tag) == desiredDamageTypeTags.contains(tag))
            .count();
    }

    public static DamageSourceDescription fromData(SerializableData.Instance dataInstance) {
        DamageSourceDescription damageSourceDescription = new DamageSourceDescription(dataInstance.getString("name"));
        for(String jsonKey : STRING_TO_TAGS.keySet()) {
            if(dataInstance.getBoolean(jsonKey)) {
                STRING_TO_TAGS.get(jsonKey).forEach(damageSourceDescription::addDamageTypeTag);
            }
        }
        return damageSourceDescription;
    }

    public static void toData(DamageSourceDescription damageSourceDescription, SerializableData.Instance data) {

        data.set("name", damageSourceDescription.getName());
        Set<TagKey<DamageType>> tags = damageSourceDescription.getDamageTypeTags();

        for(Map.Entry<TagKey<DamageType>, String> damageTagPair : TAG_TO_STRING.entrySet()) {

            TagKey<DamageType> tag = damageTagPair.getKey();
            String jsonKey = damageTagPair.getValue();

            boolean isTrueAlready = data.isPresent(jsonKey) && data.getBoolean(jsonKey);
            data.set(jsonKey, isTrueAlready || tags.contains(tag));

        }

    }
}
