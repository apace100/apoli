package io.github.apace100.apoli.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 *  A helper class used for substituting {@link ItemStack#EMPTY} for custom logic handling.
 */
public final class WorkableEmptyStack {

	private static final Cache<UUID, ItemStack> CACHE = CacheBuilder.newBuilder()
	    .weakKeys()
	    .build();

	public static ItemStack getOrCreate(Entity entity) {

		try {
			return CACHE.get(entity.getUuid(), () -> {

			    ItemStack workableStack = new ItemStack((Void) null);
			    ((EntityLinkedItemStack) workableStack).apoli$setEntity(entity);

			    return workableStack;

			});
		}

	    catch (ExecutionException e) {
	        //  This shouldn't happen as there aren't any exceptions thrown when loading new cache values
	        return ItemStack.EMPTY;
		}

	}

	public static boolean isOf(StackReference stackReference) {
		return isOf(stackReference.get());
	}

	public static boolean isOf(ItemStack stack) {
		Entity holdingEntity = ((EntityLinkedItemStack) stack).apoli$getEntity();
		return holdingEntity != null
			&& isOf(holdingEntity, stack);
	}

	public static boolean isOf(Entity entity, ItemStack stack) {

		UUID uuid = entity.getUuid();
		ItemStack workableStack = CACHE.getIfPresent(uuid);

		return stack.isEmpty()
			&& stack == workableStack;

	}

	public static void remove(UUID uuid) {
		CACHE.invalidate(uuid);
	}

}
