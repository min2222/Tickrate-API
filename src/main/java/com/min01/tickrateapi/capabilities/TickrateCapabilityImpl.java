package com.min01.tickrateapi.capabilities;

import org.jetbrains.annotations.UnknownNullability;

import com.min01.tickrateapi.util.CustomTimer;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class TickrateCapabilityImpl implements ITickrateCapability
{
	private CustomTimer timer = new CustomTimer(20.0F, 0);
	private boolean excluded;
	private boolean excludeSubEntities;
	private boolean hasTimer;
	
	@Override
	@UnknownNullability
	public CompoundTag serializeNBT(HolderLookup.Provider provider) 
	{
		CompoundTag nbt = new CompoundTag();
		nbt.putFloat("Tickrate", this.timer.tickrate);
		nbt.putBoolean("ChangeSubEntities", this.timer.shouldChangeSubEntities);
		nbt.putBoolean("Excluded", this.excluded);
		nbt.putBoolean("ExcludeSubEntities", this.excludeSubEntities);
		nbt.putBoolean("HasTimer", this.hasTimer);
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt)
	{
		this.timer = new CustomTimer(nbt.getFloat("Tickrate"), 0);
		this.timer.shouldChangeSubEntities = nbt.getBoolean("ChangeSubEntities");
		this.excluded = nbt.getBoolean("Excluded");
		this.excludeSubEntities = nbt.getBoolean("ExcludeSubEntities");
		this.hasTimer = nbt.getBoolean("HasTimer");
	}

	@Override
	public void setTimer(CustomTimer timer) 
	{
		this.timer = timer;
		this.hasTimer = true;
	}

	@Override
	public CustomTimer getTimer() 
	{
		return this.timer;
	}
	
	@Override
	public void resetTickrate()
	{
		this.timer = new CustomTimer(20.0F, 0);
		this.hasTimer = false;
	}

	@Override
	public void exclude(boolean flag) 
	{
		this.excluded = flag;
	}

	@Override
	public boolean isExcluded() 
	{
		return this.excluded;
	}

	@Override
	public void excludeSubEntities(boolean flag)
	{
		this.excludeSubEntities = flag;
	}

	@Override
	public boolean shouldExcludeSubEntities()
	{
		return this.excludeSubEntities;
	}
	
	@Override
	public boolean hasTimer() 
	{
		return this.hasTimer;
	}
}
