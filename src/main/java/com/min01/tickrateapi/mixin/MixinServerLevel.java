package com.min01.tickrateapi.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;

@Mixin(ServerLevel.class)
public class MixinServerLevel 
{
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/EntityTickList;forEach(Ljava/util/function/Consumer;)V"))
	private void tickrateapi$tick(EntityTickList instance, Consumer<Entity> pEntity, Operation<Void> original)
	{
		original.call(instance, (Consumer<Entity>) entity -> 
		{
	        if(!TickrateUtil.hasTimer(entity))
	        {
	            pEntity.accept(entity); 
	        }
	    });
	}
}
