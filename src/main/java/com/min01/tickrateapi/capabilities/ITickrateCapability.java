package com.min01.tickrateapi.capabilities;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.util.CustomTimer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

@AutoRegisterCapability
public interface ITickrateCapability extends ICapabilitySerializable<CompoundTag>
{
	ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TickrateAPI.MODID, "entity_tickrate");

	void setBaseTickrate(float tickrate);
	
	void setTickrate(float tickrate);
	
	float getTickrate();
	
	void resetTickrate();
	
	boolean hasTimer();
	
	void tick();

	CustomTimer getBaseTimer();
	
	CustomTimer getCurrentTimer();
	
	void exclude(boolean flag);
	
	boolean isExcluded();
	
	void excludeSubEntities(boolean flag);

	void changeSubEntities(boolean flag);
	
	boolean shouldExcludeSubEntities();
	
	boolean shouldChangeSubEntities();
}
