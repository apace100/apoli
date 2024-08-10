package io.github.apace100.apoli.registry;

import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.data.ClassDataRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.*;

@Environment(EnvType.CLIENT)
public class ApoliClassDataClient {

    public static final ClassDataRegistry<FeatureRenderer<?, ?>> FEATURE_RENDERERS = ClassDataRegistry.getOrCreate(ClassUtil.castClass(FeatureRenderer.class), "FeatureRenderer");

    public static void registerAll() {
        
        FEATURE_RENDERERS.addMapping("slime_overlay", SlimeOverlayFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("snowman_pumpkin", SnowGolemPumpkinFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("fox_held_item", FoxHeldItemFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("llama_decor", LlamaDecorFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("elytra", ElytraFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("villager_clothing", VillagerClothingFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("panda_held_item", PandaHeldItemFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("drowned_overlay", DrownedOverlayFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("saddle", SaddleFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("shoulder_parrot", ShoulderParrotFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("horse_armor", HorseArmorFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("wolf_collar", WolfCollarFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("energy_swirl_overlay", EnergySwirlOverlayFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("held_item", HeldItemFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("sheep_wool", SheepWoolFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("iron_golem_flower", IronGolemFlowerFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("cape", CapeFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("eyes", EyesFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("dolphin_held_item", DolphinHeldItemFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("horse_marking", HorseMarkingFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("deadmau5", Deadmau5FeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("armor", ArmorFeatureRenderer.class);
        //  TODO: Remove this non-existent feature renderer -eggohito
//        FEATURE_RENDERERS.addMapping("stray_overlay", StrayOverlayFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("enderman_block", EndermanBlockFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("mooshroom_mushroom", MooshroomMushroomFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("iron_golem_crack", IronGolemCrackFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("villager_held_item", VillagerHeldItemFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("trident_riptide", TridentRiptideFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("head", HeadFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("cat_collar", CatCollarFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("tropical_fish_color", TropicalFishColorFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("shulker_head", ShulkerHeadFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("stuck_objects", StuckObjectsFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("stuck_stingers", StuckStingersFeatureRenderer.class);
        FEATURE_RENDERERS.addMapping("stuck_arrows", StuckArrowsFeatureRenderer.class);

    }

}
