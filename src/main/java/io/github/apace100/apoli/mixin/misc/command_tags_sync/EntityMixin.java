package io.github.apace100.apoli.mixin.misc.command_tags_sync;

import io.github.apace100.apoli.networking.packet.s2c.SyncCommandTagsS2CPacket;
import io.github.apace100.apoli.util.MiscUtil;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	public abstract World getWorld();

	@Unique
	private boolean apoli$syncCommandTags = false;

	@Inject(method = "addCommandTag", at = @At("RETURN"))
	private void apoli$onCommandTagAdded(String tag, CallbackInfoReturnable<Boolean> cir) {
		this.apoli$syncCommandTags |= cir.getReturnValueZ();
	}

	@Inject(method = "removeCommandTag", at = @At("RETURN"))
	private void apoli$onCommandTagRemoved(String tag, CallbackInfoReturnable<Boolean> cir) {
		this.apoli$syncCommandTags |= cir.getReturnValueZ();
	}

	@Inject(method = "baseTick", at = @At("TAIL"))
	private void apoli$syncCommandTagsWhenDirty(CallbackInfo ci) {

		if (!this.getWorld().isClient() && apoli$syncCommandTags) {
			MiscUtil.sendToTrackers(thisAsEntity(), new SyncCommandTagsS2CPacket(thisAsEntity()));
		}

		this.apoli$syncCommandTags = false;

	}

	@Unique
	private Entity thisAsEntity() {
		return (Entity) (Object) this;
	}

}
