package com.min01.tickrateapi.capabilities;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.api.TickrateTimer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

@AutoRegisterCapability
public interface ITickrateCapability extends ICapabilitySerializable<CompoundTag>
{
	ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TickrateAPI.MODID, "tickrate");

	void tick(Entity entity);
	
	void setBaseTickrate(float tickrate);
	
	void setTickrate(float tickrate);
	
	void setPriority(int priority);
	
	void resetTickrate();

	void sync(int priority, float baseTickrate, float currentTickrate);

	boolean hasTimer();
	
	int getPriority();

	float getBaseTickrate();
	
	float getTickrate();

	TickrateTimer getBaseTimer();
	
	TickrateTimer getTimer();
	
}
