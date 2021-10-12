package io.github.apace100.apoli.registry;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.data.ClassDataRegistry;
import net.minecraft.client.render.entity.feature.*;

public class ApoliClassData {

    public static void registerAll() {

        ClassDataRegistry<Power> power = ClassDataRegistry.getOrCreate(Power.class, "Power");
        power.addPackage("io.github.apace100.apoli.power");

        ClassDataRegistry<FeatureRenderer<?, ?>> featureRenderer =
            ClassDataRegistry.getOrCreate(ClassUtil.castClass(FeatureRenderer.class), "FeatureRenderer");
        featureRenderer.addMapping("slime_overlay", SlimeOverlayFeatureRenderer.class);
        featureRenderer.addMapping("snowman_pumpkin", SnowmanPumpkinFeatureRenderer.class);
        featureRenderer.addMapping("fox_held_item", FoxHeldItemFeatureRenderer.class);
        featureRenderer.addMapping("llama_decor", LlamaDecorFeatureRenderer.class);
        featureRenderer.addMapping("elytra", ElytraFeatureRenderer.class);
        featureRenderer.addMapping("villager_clothing", VillagerClothingFeatureRenderer.class);
        featureRenderer.addMapping("panda_held_item", PandaHeldItemFeatureRenderer.class);
        featureRenderer.addMapping("drowned_overlay", DrownedOverlayFeatureRenderer.class);
        featureRenderer.addMapping("saddle", SaddleFeatureRenderer.class);
        featureRenderer.addMapping("shoulder_parrot", ShoulderParrotFeatureRenderer.class);
        featureRenderer.addMapping("horse_armor", HorseArmorFeatureRenderer.class);
        featureRenderer.addMapping("wolf_collar", WolfCollarFeatureRenderer.class);
        featureRenderer.addMapping("energy_swirl_overlay", EnergySwirlOverlayFeatureRenderer.class);
        featureRenderer.addMapping("held_item", HeldItemFeatureRenderer.class);
        featureRenderer.addMapping("sheep_wool", SheepWoolFeatureRenderer.class);
        featureRenderer.addMapping("iron_golem_flower", IronGolemFlowerFeatureRenderer.class);
        featureRenderer.addMapping("cape", CapeFeatureRenderer.class);
        featureRenderer.addMapping("eyes", EyesFeatureRenderer.class);
        featureRenderer.addMapping("dolphin_held_item", DolphinHeldItemFeatureRenderer.class);
        featureRenderer.addMapping("horse_marking", HorseMarkingFeatureRenderer.class);
        featureRenderer.addMapping("deadmau5", Deadmau5FeatureRenderer.class);
        featureRenderer.addMapping("armor", ArmorFeatureRenderer.class);
        featureRenderer.addMapping("stray_overlay", StrayOverlayFeatureRenderer.class);
        featureRenderer.addMapping("enderman_block", EndermanBlockFeatureRenderer.class);
        featureRenderer.addMapping("mooshroom_mushroom", MooshroomMushroomFeatureRenderer.class);
        featureRenderer.addMapping("iron_golem_crack", IronGolemCrackFeatureRenderer.class);
        featureRenderer.addMapping("villager_held_item", VillagerHeldItemFeatureRenderer.class);
        featureRenderer.addMapping("trident_riptide", TridentRiptideFeatureRenderer.class);
        featureRenderer.addMapping("head", HeadFeatureRenderer.class);
        featureRenderer.addMapping("cat_collar", CatCollarFeatureRenderer.class);
        featureRenderer.addMapping("tropical_fish_color", TropicalFishColorFeatureRenderer.class);
        featureRenderer.addMapping("shulker_head", ShulkerHeadFeatureRenderer.class);
        featureRenderer.addMapping("stuck_objects", StuckObjectsFeatureRenderer.class);
        featureRenderer.addMapping("stuck_stingers", StuckStingersFeatureRenderer.class);
        featureRenderer.addMapping("stuck_arrows", StuckArrowsFeatureRenderer.class);
    }
}
