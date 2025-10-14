package com.min01.tickrateapi.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.event.ForgeEventFactory;

@Mixin(Minecraft.class)
public class MixinMinecraft
{
	@Shadow
	private volatile boolean pause;

	@Shadow
	private float pausePartialTick;

	@Nullable
	@Shadow
	public LocalPlayer player;
	
	@Final
	@Shadow
	private Timer timer;

	@Inject(at = @At("HEAD"), method = "getFrameTime", cancellable = true)
	private void getFrameTime(CallbackInfoReturnable<Float> cir) 
	{
		if(this.player != null)
		{
			if(TickrateUtil.hasTimer(this.player))
			{
				cir.setReturnValue(TickrateUtil.getTimer(this.player).partialTick);
			}
			else if(TickrateUtil.hasDimensionTimer(this.player.level.dimension()) && !TickrateUtil.isExcluded(this.player))
			{
				cir.setReturnValue(TickrateUtil.getDimensionTimer(this.player.level.dimension()).partialTick);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "getDeltaFrameTime", cancellable = true)
	private void getDeltaFrameTime(CallbackInfoReturnable<Float> cir) 
	{
		if(this.player != null)
		{
			if(TickrateUtil.hasTimer(this.player))
			{
				cir.setReturnValue(TickrateUtil.getTimer(this.player).tickDelta);
			}
			else if(TickrateUtil.hasDimensionTimer(this.player.level.dimension()) && !TickrateUtil.isExcluded(this.player))
			{
				cir.setReturnValue(TickrateUtil.getDimensionTimer(this.player.level.dimension()).tickDelta);
			}
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Timer;advanceTime(J)I"), method = "runTick")
	private int advanceTime(Timer instance, long p_92526_)
	{
		if(this.player != null)
		{
			if(TickrateUtil.hasTimer(this.player))
			{
				return TickrateUtil.getTimer(this.player).advanceTime(p_92526_);
			}
			else if(TickrateUtil.hasDimensionTimer(this.player.level.dimension()) && !TickrateUtil.isExcluded(this.player))
			{
				int j = TickrateUtil.getDimensionTimer(this.player.level.dimension()).advanceTime(p_92526_);
				return j;
			}
			else
			{
				return instance.advanceTime(p_92526_);
			}
		}
		else
		{
			return instance.advanceTime(p_92526_);
		}
	}

	@ModifyArg(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"), index = 0)
	private float render(float f1)
	{
		if(this.player != null)
		{
			if(TickrateUtil.hasTimer(this.player))
			{
				return this.pause ? this.pausePartialTick : TickrateUtil.getTimer(this.player).partialTick;
			}
			else if(TickrateUtil.hasDimensionTimer(this.player.level.dimension()) && !TickrateUtil.isExcluded(this.player))
			{
				return this.pause ? this.pausePartialTick : TickrateUtil.getDimensionTimer(this.player.level.dimension()).partialTick;
			}
		}
		return f1;
	}
	
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onRenderTickStart(F)V"), method = "runTick", remap = false)
	private void onRenderTickStart(float timer)
	{
		if(this.player != null)
		{
			if(TickrateUtil.hasTimer(this.player))
			{
				ForgeEventFactory.onRenderTickStart(this.pause ? this.pausePartialTick : TickrateUtil.getTimer(this.player).partialTick);
			}
			else if(TickrateUtil.hasDimensionTimer(this.player.level.dimension()) && !TickrateUtil.isExcluded(this.player))
			{
				ForgeEventFactory.onRenderTickStart(this.pause ? this.pausePartialTick : TickrateUtil.getDimensionTimer(this.player.level.dimension()).partialTick);
			}
			else
			{
				ForgeEventFactory.onRenderTickStart(timer);
			}
		}
		else
		{
			ForgeEventFactory.onRenderTickStart(timer);
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onRenderTickEnd(F)V"), method = "runTick", remap = false)
	private void onRenderTickEnd(float timer)
	{
		if(this.player != null)
		{
			if(TickrateUtil.hasTimer(this.player))
			{
				ForgeEventFactory.onRenderTickEnd(this.pause ? this.pausePartialTick : TickrateUtil.getTimer(this.player).partialTick);
			}
			else if(TickrateUtil.hasDimensionTimer(this.player.level.dimension()) && !TickrateUtil.isExcluded(this.player))
			{
				ForgeEventFactory.onRenderTickEnd(this.pause ? this.pausePartialTick : TickrateUtil.getDimensionTimer(this.player.level.dimension()).partialTick);
			}
			else
			{
				ForgeEventFactory.onRenderTickEnd(timer);
			}
		}
		else
		{
			ForgeEventFactory.onRenderTickEnd(timer);
		}
	}
}
