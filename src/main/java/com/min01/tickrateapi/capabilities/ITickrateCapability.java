package com.min01.tickrateapi.capabilities;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.util.CustomTimer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

@AutoRegisterCapability
public interface ITickrateCapability extends INBTSerializable<CompoundTag>
{
	ResourceLocation ID = new ResourceLocation(TickrateAPI.MODID, "entity_tickrate");

	void setEntity(Entity entity);
	
	void setBaseTickrate(float tickrate);
	
	void setTickrate(float tickrate);
	
	void resetTickrate();
	
	void tick();

	CustomTimer getBaseTimer();
	
	CustomTimer getCurrentTimer();
	
	void exclude(boolean flag);
	
	boolean isExcluded();
	
	void excludeSubEntities(boolean flag);
	
	boolean shouldExcludeSubEntities();
	
	boolean hasTimer();
	
	void sync(boolean excluded, boolean shouldExcludeSubEntities, float baseTickrate, float currentTickrate, boolean baseChangeSubEntities, boolean currentChangeSubEntities);
}
