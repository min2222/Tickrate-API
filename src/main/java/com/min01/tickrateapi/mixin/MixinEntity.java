package com.min01.tickrateapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilities;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public class MixinEntity 
{
	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo ci) 
	{
		Entity entity = Entity.class.cast(this);
		entity.getCapability(TickrateCapabilities.TICKRATE).ifPresent(ITickrateCapability::tick);
	}
}
