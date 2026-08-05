package com.min01.tickrateapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilityImpl;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public class MixinEntity
{
	@Inject(method = "tick", at = @At("TAIL"))
	private void tickrateapi$tick(CallbackInfo ci) 
	{
		Entity entity = (Entity) (Object) this;
    	ITickrateCapability cap = entity.getCapability(TickrateCapabilityImpl.TICKRATE).orElse(new TickrateCapabilityImpl());
    	cap.tick(entity);
	}
}
