package com.min01.tickrateapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilityImpl;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public class MixinEntity 
{
	@Inject(method = "tick", at = @At("TAIL"))
	private void tick(CallbackInfo ci) 
	{
		Entity entity = Entity.class.cast(this);
		entity.getCapability(TickrateCapabilityImpl.TICKRATE).ifPresent(ITickrateCapability::tick);
		
		float tickrate = TickrateUtil.getArea(entity.level.dimension(), entity.getBoundingBox(), entity.position());
		if(tickrate != 20.0F || TickrateUtil.hasDimensionTimer(entity.level.dimension()))
		{
			TickrateUtil.setTickrate(entity, tickrate);
		}
	}
}
