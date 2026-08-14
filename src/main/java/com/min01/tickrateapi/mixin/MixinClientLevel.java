package com.min01.tickrateapi.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTickList;

@Mixin(ClientLevel.class)
public class MixinClientLevel
{
	@WrapOperation(method = "tickEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/EntityTickList;forEach(Ljava/util/function/Consumer;)V"))
	private void tickrateapi$tickEntities(EntityTickList instance, Consumer<Entity> pEntity, Operation<Void> original)
	{
		original.call(instance, (Consumer<Entity>) entity -> 
		{
	        if(!TickrateUtil.hasTimer(entity) || entity instanceof Player)
	        {
	            pEntity.accept(entity); 
	        }
	    });
	}
}
