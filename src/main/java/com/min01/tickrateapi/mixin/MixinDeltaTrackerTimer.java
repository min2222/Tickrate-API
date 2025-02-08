package com.min01.tickrateapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;

@Mixin(DeltaTracker.Timer.class)
public class MixinDeltaTrackerTimer
{
	@Shadow
	private boolean paused;

	@Shadow
	private boolean frozen;

	@Shadow
	private float pausedDeltaTickResidual;

	@Inject(at = @At("HEAD"), method = "getGameTimeDeltaTicks", cancellable = true)
	private void getGameTimeDeltaTicks(CallbackInfoReturnable<Float> cir)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft.player != null && TickrateUtil.hasTimer(minecraft.player))
		{
			cir.setReturnValue(TickrateUtil.getTimer(minecraft.player).tickDelta);
		}
		if(minecraft.player != null && TickrateUtil.isEntityTimeStopped(minecraft.player))
		{
			cir.setReturnValue(TickrateUtil.STOP.tickDelta);
		}
	}

	@Inject(at = @At("HEAD"), method = "advanceGameTime", cancellable = true)
	private void advanceGameTime(long pTime, CallbackInfoReturnable<Integer> cir)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft.player != null && TickrateUtil.hasTimer(minecraft.player))
		{
			cir.setReturnValue(TickrateUtil.getTimer(minecraft.player).advanceTime(pTime));
		}
		if(minecraft.player != null && TickrateUtil.isEntityTimeStopped(minecraft.player))
		{
			cir.setReturnValue(TickrateUtil.STOP.advanceTime(pTime));
		}
	}

	@Inject(at = @At("HEAD"), method = "getGameTimeDeltaPartialTick", cancellable = true)
	private void getGameTimeDeltaPartialTick(boolean pRunsNormally, CallbackInfoReturnable<Float> cir)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft.player != null && TickrateUtil.hasTimer(minecraft.player))
		{
			cir.setReturnValue(this.getGameTimeDeltaPartialTickCustom(pRunsNormally, TickrateUtil.getTimer(minecraft.player).partialTick));
		}
		if(minecraft.player != null && TickrateUtil.isEntityTimeStopped(minecraft.player))
		{
			cir.setReturnValue(this.getGameTimeDeltaPartialTickCustom(pRunsNormally, TickrateUtil.STOP.partialTick));
		}
	}

	@Inject(at = @At("HEAD"), method = "pause", cancellable = true)
	private void pause(CallbackInfo ci)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft.player != null && TickrateUtil.hasTimer(minecraft.player))
		{
			ci.cancel();
			if(!this.paused)
			{
				this.pausedDeltaTickResidual = TickrateUtil.getTimer(minecraft.player).partialTick;
			}
			this.paused = true;
		}
		if(minecraft.player != null && TickrateUtil.isEntityTimeStopped(minecraft.player))
		{
			ci.cancel();
			if(!this.paused)
			{
				this.pausedDeltaTickResidual = TickrateUtil.STOP.partialTick;
			}
			this.paused = true;
		}
	}

	@Unique
	private float getGameTimeDeltaPartialTickCustom(boolean pRunsNormally, float partialTicks)
	{
		if(!pRunsNormally && this.frozen)
		{
			return 1.0F;
		}
		else
		{
			return this.paused ? this.pausedDeltaTickResidual : partialTicks;
		}
	}
}