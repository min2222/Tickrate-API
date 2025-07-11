package com.min01.tickrateapi.mixin;

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.tickrateapi.capabilities.ITickrateCapability;
import com.min01.tickrateapi.capabilities.TickrateCapabilities;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public class MixinEntity 
{
	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo ci) 
	{
		Entity entity = Entity.class.cast(this);
		entity.getCapability(TickrateCapabilities.TICKRATE).ifPresent(ITickrateCapability::tick);
		
		Pair<Boolean, Float> pair = TickrateUtil.getArea(entity.level.dimension(), entity.getBoundingBox());
		if(pair.getLeft())
		{
			TickrateUtil.setTickrate(entity, pair.getRight());
		}
	}
}
