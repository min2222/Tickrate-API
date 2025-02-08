package com.min01.tickrateapi.capabilities;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.util.CustomTimer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

public interface ITickrateCapability extends INBTSerializable<CompoundTag>
{
	ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(TickrateAPI.MODID, "entity_tickrate");
	
	void setTimer(CustomTimer timer);
	
	CustomTimer getTimer();
	
	void resetTickrate();
	
	void exclude(boolean flag);
	
	boolean isExcluded();
	
	void excludeSubEntities(boolean flag);
	
	boolean shouldExcludeSubEntities();
	
	boolean hasTimer();
}
