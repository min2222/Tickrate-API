package com.min01.tickrateapi.capabilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.min01.tickrateapi.network.TickrateNetwork;
import com.min01.tickrateapi.network.UpdateTickratePacket;
import com.min01.tickrateapi.util.CustomTimer;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

public class TickrateCapabilityImpl implements ITickrateCapability
{
	public static final Capability<ITickrateCapability> TICKRATE = CapabilityManager.get(new CapabilityToken<>() {});
	
	private CustomTimer baseTimer = new CustomTimer(20.0F, 0L);
	private CustomTimer currentTimer = new CustomTimer(20.0F, 0L);
	
	private final Entity entity;
	private boolean excluded;
	private boolean excludeSubEntities;
	private boolean shouldChangeSubEntities = true;

	public TickrateCapabilityImpl(Entity entity)
	{
		this.entity = entity;
	}
	
	@Override
	public CompoundTag serializeNBT() 
	{
		CompoundTag nbt = new CompoundTag();
		nbt.putBoolean("ChangeSubEntities", this.shouldChangeSubEntities);
		nbt.putBoolean("ExcludeSubEntities", this.excludeSubEntities);
		nbt.putBoolean("Excluded", this.excluded);
		nbt.putFloat("BaseTickrate", this.baseTimer.tickrate);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		this.exclude(nbt.getBoolean("Excluded"));
		this.excludeSubEntities(nbt.getBoolean("ExcludeSubEntities"));
		this.changeSubEntities(nbt.getBoolean("ChangeSubEntities"));
		this.setBaseTickrate(nbt.getFloat("BaseTickrate"));
	}

	@Override
	public void setBaseTickrate(float tickrate) 
	{
		this.baseTimer.setTickrate(tickrate);
    	this.sendUpdatePacket();
	}
	
	@Override
	public void setTickrate(float tickrate) 
	{
		this.currentTimer.setTickrate(tickrate);
    	this.sendUpdatePacket();
	}
	
	@Override
	public float getTickrate() 
	{
		return this.currentTimer.tickrate;
	}
	
	@Override
	public boolean hasTimer() 
	{
		return this.getTickrate() != 20.0F;
	}
	
	@Override
	public void resetTickrate()
	{
		this.baseTimer.setTickrate(20.0F);
    	this.sendUpdatePacket();
	}

	@Override
	public void tick() 
	{
		this.setTickrate(this.baseTimer.tickrate);
	}

	@Override
	public CustomTimer getBaseTimer() 
	{
		return this.baseTimer;
	}
	
	@Override
	public CustomTimer getCurrentTimer() 
	{
		return this.currentTimer;
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
    	this.sendUpdatePacket();
	}
	
	@Override
	public void changeSubEntities(boolean flag)
	{
		this.shouldChangeSubEntities = flag;
    	this.sendUpdatePacket();
	}

	@Override
	public boolean shouldExcludeSubEntities()
	{
		return this.excludeSubEntities;
	}
	
	@Override
	public boolean shouldChangeSubEntities() 
	{
		return this.shouldChangeSubEntities;
	}
	
	private void sendUpdatePacket() 
	{
		if(!this.entity.level.isClientSide)
		{
			TickrateNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.entity), new UpdateTickratePacket(this.entity.getUUID(), this.excluded, this.excludeSubEntities, this.shouldChangeSubEntities, this.baseTimer.tickrate, this.getTickrate()));
		}
	}
	
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) 
	{
		return TICKRATE.orEmpty(cap, LazyOptional.of(() -> this));
	}
}