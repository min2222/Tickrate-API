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
	
	void setTimer(CustomTimer timer);
	
	CustomTimer getTimer();
	
	void resetTickrate();
	
	void exclude(boolean flag);
	
	boolean isExcluded();
	
	void excludeSubEntities(boolean flag);
	
	boolean shouldExcludeSubEntities();
	
	boolean hasTimer();
}
