package com.min01.tickrateapi.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.tickrateapi.util.CustomTimer;
import com.min01.tickrateapi.util.TickrateUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer
{
    @Shadow
    private ClientLevel level;
    
    @Final
    @Shadow
    private Minecraft minecraft;
    
    @Final
    @Shadow
    private EntityRenderDispatcher entityRenderDispatcher;
  
    @Inject(at = @At("HEAD"), method = "renderEntity", cancellable = true)
    private void renderEntity(Entity p_109518_, double p_109519_, double p_109520_, double p_109521_, float p_109522_, PoseStack p_109523_, MultiBufferSource p_109524_, CallbackInfo ci)
    {
		if(TickrateUtil.isEntityTimeStopped(p_109518_))
		{
	    	ci.cancel();
			float partialTick = TickrateUtil.STOP.partialTick;
			double d0 = Mth.lerp((double)partialTick, p_109518_.xOld, p_109518_.getX());	
	    	double d1 = Mth.lerp((double)partialTick, p_109518_.yOld, p_109518_.getY());
	    	double d2 = Mth.lerp((double)partialTick, p_109518_.zOld, p_109518_.getZ());
	    	float f = Mth.lerp(partialTick, p_109518_.yRotO, p_109518_.getYRot());
	    	this.entityRenderDispatcher.render(p_109518_, d0 - p_109519_, d1 - p_109520_, d2 - p_109521_, f, partialTick, p_109523_, p_109524_, this.entityRenderDispatcher.getPackedLightCoords(p_109518_, partialTick));
		}
		if(TickrateUtil.hasClientTimer(p_109518_))
		{
	    	ci.cancel();
			CustomTimer timer = TickrateUtil.getClientTimer(p_109518_);
			float partialTick = timer.partialTick;
			double d0 = Mth.lerp((double)partialTick, p_109518_.xOld, p_109518_.getX());	
	    	double d1 = Mth.lerp((double)partialTick, p_109518_.yOld, p_109518_.getY());
	    	double d2 = Mth.lerp((double)partialTick, p_109518_.zOld, p_109518_.getZ());
	    	float f = Mth.lerp(partialTick, p_109518_.yRotO, p_109518_.getYRot());
	    	this.entityRenderDispatcher.render(p_109518_, d0 - p_109519_, d1 - p_109520_, d2 - p_109521_, f, partialTick, p_109523_, p_109524_, this.entityRenderDispatcher.getPackedLightCoords(p_109518_, partialTick));
		}
    }
}
