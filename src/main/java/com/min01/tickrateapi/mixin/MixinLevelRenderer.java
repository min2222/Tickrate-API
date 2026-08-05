package com.min01.tickrateapi.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.min01.tickrateapi.api.TickrateTimer;
import com.min01.tickrateapi.util.TickrateUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer 
{
	@WrapMethod(method = "renderEntity")
	private void tickrateapi$renderEntity(Entity pEntity, double pCamX, double pCamY, double pCamZ, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, Operation<Void> original)
	{
		if(TickrateUtil.hasTimer(pEntity))
		{
			TickrateTimer timer = TickrateUtil.getTimer(pEntity);
			original.call(pEntity, pCamX, pCamY, pCamZ, timer.partialTick, pPoseStack, pBufferSource);
			return;
		}
		original.call(pEntity, pCamX, pCamY, pCamZ, pPartialTick, pPoseStack, pBufferSource);
	}
}
