package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class ModifyItemCooldownItemActionType extends ItemActionType {

    public static final DataObjectFactory<ModifyItemCooldownItemActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("modifier", Modifier.DATA_TYPE.optional(), Optional.empty())
            .addFunctionedDefault("modifiers", Modifier.LIST_TYPE.optional(), data -> data.<Optional<Modifier>>get("modifier").map(List::of)),
        data -> new ModifyItemCooldownItemActionType(
            data.get("modifiers")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("modifiers", actionType.modifiers)
    );

    private final List<Modifier> modifiers;

    public ModifyItemCooldownItemActionType(List<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    @Override
	protected void execute(World world, StackReference stackReference) {

        ItemStack stack = stackReference.get();
        if (stack.isEmpty() || modifiers.isEmpty() || !(((EntityLinkedItemStack) stack).apoli$getEntity(true) instanceof PlayerEntity player)) {
            return;
        }

        ItemCooldownManager cooldownManager = player.getItemCooldownManager();
        ItemCooldownManager.Entry cooldownEntry = cooldownManager.entries.get(stack.getItem());

        int oldDuration = cooldownEntry != null
            ? cooldownEntry.endTick - cooldownEntry.startTick
            : 0;

        cooldownManager.set(stack.getItem(), (int) ModifierUtil.applyModifiers(player, modifiers, oldDuration));

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return ItemActionTypes.MODIFY_ITEM_COOLDOWN;
    }

}
