package com.min01.tickrateapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.tickrateapi.api.EntityTickEvent;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;

@Mixin(Entity.class)
public class MixinEntity
{
	@Inject(method = "tick", at = @At("TAIL"))
	private void tickrateapi$tick(CallbackInfo ci) 
	{
		Entity entity = (Entity) (Object) this;
		MinecraftForge.EVENT_BUS.post(new EntityTickEvent(entity));
	}
}
