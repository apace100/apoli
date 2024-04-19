package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.TagKey;

import java.util.List;
import java.util.Optional;

public class ModifyTypeTagPower extends Power implements Prioritized<ModifyTypeTagPower> {

    private final TagKey<EntityType<?>> tag;

    private final boolean replace;
    private final int priority;

    public ModifyTypeTagPower(PowerType<?> type, LivingEntity entity, TagKey<EntityType<?>> tag, boolean replace, int priority) {
        super(type, entity);
        this.tag = tag;
        this.replace = replace;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public TagKey<EntityType<?>> getTag() {
        return tag;
    }

    public boolean matches(TagKey<EntityType<?>> otherTag) {
        return this.getTag().equals(otherTag);
    }

    public boolean shouldReplace() {
        return replace;
    }

    public static boolean doesApply(Entity entity, TagKey<EntityType<?>> entityTypeTag, boolean original) {

        CallInstance<ModifyTypeTagPower> mttpci = new CallInstance<>();
        mttpci.add(entity, ModifyTypeTagPower.class, p -> true);

        for (int i = mttpci.getMaxPriority(); i >= mttpci.getMinPriority(); i--) {

            List<ModifyTypeTagPower> mttps = mttpci.getPowers(i);
            if (mttps.isEmpty()) {
                continue;
            }

            Optional<Boolean> replaceResult = mttps
                .stream()
                .filter(ModifyTypeTagPower::shouldReplace)
                .findFirst()
                .map(replacingMttp -> replacingMttp.matches(entityTypeTag));

            if (replaceResult.isPresent()) {
                return replaceResult.get();
            }

            if (mttps.stream().anyMatch(mttp -> mttp.matches(entityTypeTag))) {
                return true;
            }

        }

        return original;

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("modify_type_tag"),
            new SerializableData()
                .add("tag", SerializableDataTypes.ENTITY_TAG)
                .add("replace", SerializableDataTypes.BOOLEAN, false)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (powerType, livingEntity) -> new ModifyTypeTagPower(
                powerType,
                livingEntity,
                data.get("tag"),
                data.get("replace"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
