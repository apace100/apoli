package io.github.apace100.apoli.integration.emi;

import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.recipe.special.EmiArmorDyeRecipe;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ApoliEmiPlugin implements EmiPlugin {
    public static Identifier WIDGETS = Apoli.identifier("textures/gui/emi_widgets.png");
    public static EmiTexture REQUIRED_POWER_HEADING_BORDER_RECIPE_POWER = new EmiTexture(WIDGETS, 0, 0, 118, 16);
    public static EmiTexture REQUIRED_POWER_HEADING_BORDER_MODIFY_CRAFTING = new EmiTexture(WIDGETS, 0, 0, 90, 16);
    public static EmiTexture POWER_NAME_BORDER_MIDDLE = new EmiTexture(WIDGETS, 0, 0, 118, 10);
    public static EmiTexture POWER_NAME_BORDER_BOTTOM = new EmiTexture(WIDGETS, 0, 0, 118, 2);
    private static final List<Identifier> LOADED_SUBPOWERS = new ArrayList<>();
    public static EmiRecipeCategory MODIFY_GRINDSTONE = new EmiRecipeCategory(Apoli.identifier("modified_grindstone"), new EmiTexture(WIDGETS, 240, 240, 16, 16));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(MODIFY_GRINDSTONE);

        registry.addWorkstation(MODIFY_GRINDSTONE, EmiStack.of(Items.GRINDSTONE));

        for (PowerType<?> powerType : PowerTypeRegistry.valueStream().filter(identifierPowerTypeEntry -> identifierPowerTypeEntry instanceof MultiplePowerType<?>).toList()) {
            ((MultiplePowerType<?>) powerType).getSubPowers().forEach(subpowerId -> {
                PowerType<?> powerType2 = PowerTypeRegistry.get(subpowerId);
                Power power2 = powerType2.create(null);
                if (power2 instanceof RecipePower recipePower) {
                    addRecipePowerRecipes(registry, recipePower.getRecipe(), powerType2, powerType.getName());
                } else if (power2 instanceof ModifyCraftingPower modifyCraftingPower) {
                    addModifyCraftingPowerRecipes(registry, modifyCraftingPower, powerType2, powerType.getName());
                }
                LOADED_SUBPOWERS.add(subpowerId);
            });
        }

        for (PowerType<?> powerType : PowerTypeRegistry.valueStream().filter(identifierPowerTypeEntry -> !(identifierPowerTypeEntry instanceof MultiplePowerType<?>)).toList()) {
            if (LOADED_SUBPOWERS.contains(powerType.getIdentifier())) continue;
            Power power = powerType.create(null);
            if (power instanceof RecipePower recipePower) {
                addRecipePowerRecipes(registry, recipePower.getRecipe(), powerType, null);
            } else if (power instanceof ModifyCraftingPower modifyCraftingPower) {
                addModifyCraftingPowerRecipes(registry, modifyCraftingPower, powerType, null);
            }
        }
    }

    private void addModifyCraftingPowerRecipes(EmiRegistry registry, ModifyCraftingPower power, PowerType<?> powerType, @Nullable MutableText multiplePowerTypeName) {
        if (power.getNewStack() == null || power.getNewStack().isEmpty()) return;

        CraftingInventory inv = new CraftingInventory(new ScreenHandler(null, -1) {
            public boolean canUse(PlayerEntity player) {
                return false;
            }

            public ItemStack transferSlot(PlayerEntity player, int index) {
                return null;
            }
        }, 1, 1);

        registry.getRecipeManager().listAllOfType(RecipeType.CRAFTING).stream().filter(cr -> !(cr instanceof ModifyCraftingPower) && power.doesApply(inv, cr)).forEach(recipe -> {
            if (recipe instanceof ShapelessRecipe shapeless) {
                registry.addRecipe(new ModifiedEmiShapelessRecipe(shapeless, EmiStack.of(power.getNewStack()), powerType, multiplePowerTypeName));
            } else if (recipe instanceof ShapedRecipe shaped) {
                registry.addRecipe(new ModifiedEmiShapedRecipe(shaped, EmiStack.of(power.getNewStack()), powerType, multiplePowerTypeName));
            } else if (recipe instanceof ShulkerBoxColoringRecipe) {
                for (DyeColor dye : DyeColor.values()) {
                    DyeItem dyeItem = DyeItem.byColor(dye);
                    Identifier id = new Identifier("emi", "dye_shulker_box/" + EmiUtil.subId(dyeItem));
                    registry.addRecipe(new ModifiedEmiCraftingRecipe(
                            List.of(EmiStack.of(Items.SHULKER_BOX), EmiStack.of(dyeItem)), EmiStack.of(power.getNewStack()),
                            EmiStack.of(ShulkerBoxBlock.getItemStack(dye)), id, powerType, multiplePowerTypeName, true));
                }
            } else if (recipe instanceof ArmorDyeRecipe) {
                for (Item item : EmiArmorDyeRecipe.DYEABLE_ITEMS) {
                    registry.addRecipe(new ModifiedEmiArmorDyeRecipe(item, EmiStack.of(power.getNewStack()), powerType, multiplePowerTypeName));
                }
            } else if (recipe instanceof SuspiciousStewRecipe suspiciousStew) {
                registry.addRecipe(new ModifiedEmiSuspiciousStewRecipe(EmiStack.of(power.getNewStack()), suspiciousStew.getId(), powerType, multiplePowerTypeName));
            } else if (recipe instanceof ShieldDecorationRecipe shieldDecoration) {
                registry.addRecipe(new ModifiedEmiBannerShieldRecipe(EmiStack.of(power.getNewStack()), shieldDecoration.getId(), powerType, multiplePowerTypeName));
            } else if (recipe instanceof BookCloningRecipe book) {
                registry.addRecipe(new ModifiedEmiBookCloningRecipe(EmiStack.of(power.getNewStack()), book.getId(), powerType, multiplePowerTypeName));
            } else if (recipe instanceof TippedArrowRecipe) {
                Registry.POTION.streamEntries().forEach(entry -> {
                    if (entry.value() == Potions.EMPTY) {
                        return;
                    }
                    EmiStack arrow = EmiStack.of(Items.ARROW);
                    registry.addRecipe(new ModifiedEmiCraftingRecipe(List.of(
                                arrow, arrow, arrow, arrow,
                                EmiStack.of(PotionUtil.setPotion(new ItemStack(Items.LINGERING_POTION), entry.value())),
                                arrow, arrow, arrow, arrow
                            ),
                            EmiStack.of(power.getNewStack()),
                            EmiStack.of(PotionUtil.setPotion(new ItemStack(Items.TIPPED_ARROW, 8), entry.value())),
                            new Identifier("emi", "tipped_arrow/" + EmiUtil.subId(Registry.POTION.getId(entry.value()))),
                            powerType, multiplePowerTypeName, false));
                });
            } else {
                EmiModifyCraftingLoadCallback.EVENT.invoker();
            }
        });
    }

    private void addRecipePowerRecipes(EmiRegistry registry, Recipe<CraftingInventory> recipe, PowerType<?> powerType, @Nullable MutableText multiplePowerTypeName) {
        if (recipe instanceof ShapedRecipe sr) {
            registry.addRecipe(new RecipePowerShapedEmiRecipe(sr, powerType, multiplePowerTypeName));
        } else if (recipe instanceof ShapelessRecipe slr) {
            registry.addRecipe(new RecipePowerShapelessEmiRecipe(slr, powerType, multiplePowerTypeName));
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void showPowerRequirement(WidgetHolder widgets, PowerType<?> powerType, MutableText powerName) {
        int colorValue;
        if (!PowerHolderComponent.KEY.get(MinecraftClient.getInstance().player).hasPower(powerType)) {
            colorValue = Formatting.DARK_GRAY.getColorValue();
        } else if (!PowerHolderComponent.KEY.get(MinecraftClient.getInstance().player).getPower(powerType).isActive()) {
            colorValue = Formatting.RED.getColorValue();
        } else {
            colorValue = Formatting.DARK_GREEN.getColorValue();
        }
        widgets.addTexture(ApoliEmiPlugin.REQUIRED_POWER_HEADING_BORDER_MODIFY_CRAFTING, 0, 56);
        widgets.addText(Text.translatable("emi.apoli.required_power").asOrderedText(), 4, 60, colorValue, true);
        List<OrderedText> powerNameLines = MinecraftClient.getInstance().textRenderer.wrapLines(powerName, widgets.getWidth() - 8);
        int y = 72;
        for (OrderedText line : powerNameLines) {
            widgets.addTexture(ApoliEmiPlugin.POWER_NAME_BORDER_MIDDLE, 0, y);
            widgets.addText(line, 4, y + 2, Formatting.WHITE.getColorValue(), false);
            y += 10;
        }
        widgets.addTexture(ApoliEmiPlugin.POWER_NAME_BORDER_BOTTOM, 0, y);
    }
}
