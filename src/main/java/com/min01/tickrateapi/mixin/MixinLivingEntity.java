package com.min01.tickrateapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.min01.tickrateapi.util.CustomTimer;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public class MixinLivingEntity
{
	@Inject(at = @At(value = "HEAD"), method = "addAdditionalSaveData")
	private void addAdditionalSaveData(CompoundTag tag, CallbackInfo ci)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		if(TickrateUtil.hasTimer(living))
		{
			CustomTimer timer = TickrateUtil.getTimer(living);
			tag.putFloat(TickrateUtil.TICKRATE, timer.tickrate);
		}
		if(TickrateUtil.isExcluded(living))
		{
			tag.putBoolean(TickrateUtil.EXCLUDED, true);
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "readAdditionalSaveData")
	private void readAdditionalSaveData(CompoundTag tag, CallbackInfo ci)
	{
		LivingEntity living = LivingEntity.class.cast(this);
		if(tag.contains(TickrateUtil.TICKRATE))
		{
			TickrateUtil.setTickrate(living, tag.getFloat(TickrateUtil.TICKRATE));
		}
		if(tag.contains(TickrateUtil.EXCLUDED))
		{
			TickrateUtil.excludeEntity(living);
		}
	}
}