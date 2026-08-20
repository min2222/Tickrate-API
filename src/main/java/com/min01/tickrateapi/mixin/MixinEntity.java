package com.min01.tickrateapi.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.min01.tickrateapi.api.event.EntityTickEvent;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;

@Mixin(Entity.class)
public class MixinEntity
{
	@WrapMethod(method = "tick")
	private void tickrateapi$tick(Operation<Void> original) 
	{
		//is there any meaningful difference between fire event before or after? idk;
		Entity entity = (Entity) (Object) this;
		MinecraftForge.EVENT_BUS.post(new EntityTickEvent(entity));
		original.call();
	}
}
