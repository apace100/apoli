package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.power.type.ModifyTypeTagPowerType;
import io.github.apace100.calio.mixin.TagEntryAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.DependencyTracker;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(TagGroupLoader.class)
public abstract class TagGroupLoaderMixin<T> {

    @Shadow
    @Final
    private String dataType;

    @Inject(method = "buildGroup", at = @At("RETURN"))
    private void apoli$rebuildTagsInTags(Map<Identifier, List<TagGroupLoader.TrackedEntry>> tags, CallbackInfoReturnable<Map<Identifier, Collection<T>>> cir, @Local TagEntry.ValueGetter<T> valueGetter, @Local DependencyTracker<Identifier, TagGroupLoader.TagDependencies> dependencyTracker) {

        String prefix = dataType + "/";

        dependencyTracker.traverse((id, dependencies) -> dependencies.entries()
            .stream()
            .map(TagGroupLoader.TrackedEntry::entry)
            .filter(entry -> entry.resolve(valueGetter, o -> {}))
            .map(TagEntryAccessor.class::cast)
            .filter(TagEntryAccessor::isTag)
            .forEach(entry -> ModifyTypeTagPowerType.TAGS_IN_TAGS
                .computeIfAbsent(id.withPrefixedPath(prefix), k -> new ObjectArraySet<>())
                .add(entry.getId().withPrefixedPath(prefix))));

    }

}
