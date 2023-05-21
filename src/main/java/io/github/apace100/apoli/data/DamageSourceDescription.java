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
import net.minecraft.util.Identifier;

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

        for(String jsonKey : STRING_TO_TAGS.keySet()) {
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
        ((NameMutableDamageSource)source).setName(name);
    }

    private void findBestMatchingDamageType(DamageSources damageSources) {
        Optional<? extends RegistryEntry<DamageType>> bestMatchingDamageType = damageSources.registry.streamEntries()
                .max(Comparator.comparingInt(this::getTagMatches));
        if(bestMatchingDamageType.isPresent()) {
            RegistryEntry<DamageType> bestMatch = bestMatchingDamageType.get();
            int bestMatchTagCount = getTagMatches(bestMatch);
            if(bestMatchTagCount < TAG_COUNT) {
                Apoli.LOGGER.warn("Could not find a perfect damage type for legacy damage source field, best match: {} out of {} tags with damage type \"{}\". Consider creating your own custom damage type.",
                        bestMatchTagCount, TAG_COUNT, bestMatch.getKey().map(RegistryKey::getValue).map(Identifier::toString).orElse("<unknown>"));
            }
            damageType = bestMatch.getKey().orElseThrow();
        } else {
            throw new NoSuchElementException("Damage type registry was empty or not loaded yet");
        }
    }

    private int getTagMatches(RegistryEntry<DamageType> damageType) {
        int count = 0;
        for(TagKey<DamageType> tag : STRING_TO_TAGS.values()) {
            if(damageType.isIn(tag) == desiredDamageTypeTags.contains(tag)) {
                count++;
            }
        }
        return count;
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

    public static SerializableData.Instance toData(SerializableData data, DamageSourceDescription damageSourceDescription) {
        SerializableData.Instance instance = data.new Instance();
        instance.set("name", damageSourceDescription.getName());
        Set<TagKey<DamageType>> tags = damageSourceDescription.getDamageTypeTags();
        for(Map.Entry<TagKey<DamageType>, String> damageTagPair : TAG_TO_STRING.entrySet()) {
            TagKey<DamageType> tag = damageTagPair.getKey();
            String jsonKey = damageTagPair.getValue();
            boolean isTrueAlready = instance.isPresent(jsonKey) && instance.getBoolean(jsonKey);
            instance.set(jsonKey, isTrueAlready || tags.contains(tag));
        }
        return instance;
    }
}
