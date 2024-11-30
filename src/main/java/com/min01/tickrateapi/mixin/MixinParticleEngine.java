package com.min01.tickrateapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.tickrateapi.util.TickrateUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;

@Mixin(ParticleEngine.class)
public class MixinParticleEngine
{
	@Shadow
	protected ClientLevel level;
	
	@Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
	private void tick(CallbackInfo ci)
	{
		if(TickrateUtil.isDimensionTimeStopped(this.level.dimension()))
		{
			ci.cancel();
		}
	}
	
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V")
	private void render(Particle instance, VertexConsumer p_107261_, Camera p_107262_, float p_107263_)
	{
		if(TickrateUtil.isDimensionTimeStopped(this.level.dimension()))
		{
			instance.render(p_107261_, p_107262_, TickrateUtil.STOP.partialTick);
		}
		else
		{
			instance.render(p_107261_, p_107262_, p_107263_);
		}
	}
}
