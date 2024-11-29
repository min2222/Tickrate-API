package com.min01.tickrateapi.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
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
	
	@Inject(at = @At("HEAD"), method = "runTick", cancellable = true)
	private void runTick(boolean flag, CallbackInfo ci) 
	{
		if(flag && this.player != null)
		{
			if(TickrateUtil.isEntityTimeStopped(this.player))
			{
				int j = this.timer.advanceTime(Util.getMillis());
				for(int k = 0; k < Math.min(10, j); ++k)
				{
					this.tick();
				}
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "getFrameTime", cancellable = true)
	private void getFrameTime(CallbackInfoReturnable<Float> cir) 
	{
		if(this.player != null && TickrateUtil.hasClientTimer(this.player))
		{
			cir.setReturnValue(TickrateUtil.getClientTimer(this.player).partialTick);
		}
		if(this.player != null && TickrateUtil.isEntityTimeStopped(this.player))
		{
			cir.setReturnValue(TickrateUtil.STOP.partialTick);
		}
	}
	
	@Inject(at = @At("HEAD"), method = "getDeltaFrameTime", cancellable = true)
	private void getDeltaFrameTime(CallbackInfoReturnable<Float> cir) 
	{
		if(this.player != null && TickrateUtil.hasClientTimer(this.player))
		{
			cir.setReturnValue(TickrateUtil.getClientTimer(this.player).tickDelta);
		}
		if(this.player != null && TickrateUtil.isEntityTimeStopped(this.player))
		{
			cir.setReturnValue(TickrateUtil.STOP.tickDelta);
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Timer;advanceTime(J)I"), method = "runTick")
	private int advanceTime(Timer instance, long p_92526_)
	{
		if(this.player != null)
		{
			if(TickrateUtil.hasClientTimer(this.player))
			{
				return TickrateUtil.getClientTimer(this.player).advanceTime(p_92526_);
			}
			else if(!TickrateUtil.isEntityTimeStopped(this.player))
			{
				return instance.advanceTime(p_92526_);
			}
			else
			{
				return TickrateUtil.STOP.advanceTime(p_92526_);
			}
		}
		else
		{
			return instance.advanceTime(p_92526_);
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"), method = "runTick")
	private void render(GameRenderer instance, float f1, long crashreport, boolean crashreportcategory)
	{
		if(this.player != null)
		{
			if(TickrateUtil.hasClientTimer(this.player))
			{
				instance.render(this.pause ? this.pausePartialTick : TickrateUtil.getClientTimer(this.player).partialTick, crashreport, crashreportcategory);
			}
			else if(!TickrateUtil.isEntityTimeStopped(this.player))
			{
				instance.render(f1, crashreport, crashreportcategory);
			}
			else
			{
				instance.render(TickrateUtil.STOP.partialTick, crashreport, crashreportcategory);
			}
		}
		else
		{
			instance.render(f1, crashreport, crashreportcategory);
		}
	}
	
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onRenderTickStart(F)V"), method = "runTick", remap = false)
	private void onRenderTickStart(float timer)
	{
		if(this.player != null)
		{
			if(TickrateUtil.hasClientTimer(this.player))
			{
				ForgeEventFactory.onRenderTickStart(this.pause ? this.pausePartialTick : TickrateUtil.getClientTimer(this.player).partialTick);
			}
			else if(!TickrateUtil.isEntityTimeStopped(this.player))
			{
				ForgeEventFactory.onRenderTickStart(timer);
			}
			else
			{
				ForgeEventFactory.onRenderTickStart(TickrateUtil.STOP.partialTick);
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
			if(TickrateUtil.hasClientTimer(this.player))
			{
				ForgeEventFactory.onRenderTickEnd(this.pause ? this.pausePartialTick : TickrateUtil.getClientTimer(this.player).partialTick);
			}
			else if(!TickrateUtil.isEntityTimeStopped(this.player))
			{
				ForgeEventFactory.onRenderTickEnd(timer);
			}
			else
			{
				ForgeEventFactory.onRenderTickEnd(TickrateUtil.STOP.partialTick);
			}
		}
		else
		{
			ForgeEventFactory.onRenderTickEnd(timer);
		}
	}
	
	@Shadow
	public void tick() 
	{
		
	}
}
