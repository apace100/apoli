package io.github.apace100.apoli.loot.context;

import com.google.common.collect.BiMap;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.mixin.LootContextTypesAccessor;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ApoliLootContextTypes {

    public static final LootContextType ANY = register(
        "any", builder -> builder
            .allow(LootContextParameters.THIS_ENTITY)
            .allow(LootContextParameters.LAST_DAMAGE_PLAYER)
            .allow(LootContextParameters.DAMAGE_SOURCE)
            .allow(LootContextParameters.KILLER_ENTITY)
            .allow(LootContextParameters.DIRECT_KILLER_ENTITY)
            .allow(LootContextParameters.ORIGIN)
            .allow(LootContextParameters.BLOCK_STATE)
            .allow(LootContextParameters.BLOCK_ENTITY)
            .allow(LootContextParameters.TOOL)
            .allow(LootContextParameters.EXPLOSION_RADIUS)
    );

    private ApoliLootContextTypes() {}

    private static LootContextType register(String name, Function<LootContextType.Builder, LootContextType.Builder> builderFunction) {

        Identifier id = Apoli.identifier(name);
        LootContextType lootContextType = builderFunction.apply(new LootContextType.Builder()).build();

        BiMap<Identifier, LootContextType> idAndLootContextTypeMap = LootContextTypesAccessor.getMap();
        if (idAndLootContextTypeMap.containsKey(id)) throw new IllegalStateException("Loot table parameter set \"" + id + "\" is already registered!");

        idAndLootContextTypeMap.put(id, lootContextType);
        return lootContextType;

    }

}
