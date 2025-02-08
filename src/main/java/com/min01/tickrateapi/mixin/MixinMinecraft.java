package com.min01.tickrateapi.mixin;

import com.min01.tickrateapi.util.TickrateUtil;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MixinMinecraft
{
    @Final
    @Shadow
    private DeltaTracker.Timer timer;

    @Nullable
    @Shadow
    public LocalPlayer player;

    @Inject(at = @At("HEAD"), method = "runTick", cancellable = true)
    private void runTick(boolean flag, CallbackInfo ci)
    {
        if(flag && this.player != null)
        {
            if(TickrateUtil.isEntityTimeStopped(this.player))
            {
                int j = this.timer.advanceTime(Util.getMillis(), flag);
                for(int k = 0; k < Math.min(10, j); ++k)
                {
                    this.tick();
                }
            }
        }
    }

    @Shadow
    public void tick()
    {

    }
}
