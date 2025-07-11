package com.min01.tickrateapi.capabilities;

import com.min01.tickrateapi.network.TickrateNetwork;
import com.min01.tickrateapi.network.UpdateTickratePacket;
import com.min01.tickrateapi.util.CustomTimer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;

public class TickrateCapabilityImpl implements ITickrateCapability
{
	private CustomTimer baseTimer = new CustomTimer(20.0F, 0L);
	private CustomTimer prevTimer = new CustomTimer(20.0F, 0L);
	private CustomTimer currentTimer = new CustomTimer(20.0F, 0L);
	private Entity entity;
	private boolean excluded;
	private boolean excludeSubEntities;
	
	@Override
	public CompoundTag serializeNBT() 
	{
		CompoundTag nbt = new CompoundTag();
		nbt.putFloat("Tickrate", this.currentTimer.tickrate);
		nbt.putBoolean("ChangeSubEntities", this.currentTimer.shouldChangeSubEntities);
		nbt.putBoolean("Excluded", this.excluded);
		nbt.putBoolean("ExcludeSubEntities", this.excludeSubEntities);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		this.currentTimer.setTickrate(nbt.getFloat("Tickrate"));
		this.currentTimer.shouldChangeSubEntities = nbt.getBoolean("ChangeSubEntities");
		this.excluded = nbt.getBoolean("Excluded");
		this.excludeSubEntities = nbt.getBoolean("ExcludeSubEntities");
	}

	@Override
	public void setEntity(Entity entity) 
	{
		this.entity = entity;
	}

	@Override
	public void setBaseTickrate(float tickrate) 
	{
		this.baseTimer.setTickrate(tickrate);
		this.sendUpdatePacket(false);
	}
	
	@Override
	public void setTickrate(float tickrate) 
	{
		this.currentTimer.setTickrate(tickrate);
		this.sendUpdatePacket(false);
	}
	
	@Override
	public void resetTickrate()
	{
		this.currentTimer.setTickrate(20.0F);
		this.baseTimer.setTickrate(20.0F);
		this.sendUpdatePacket(true);
	}

	@Override
	public void tick() 
	{
		this.currentTimer.setTickrate(this.baseTimer.tickrate);
		
		if(!this.prevTimer.equals(this.currentTimer))
		{
			this.prevTimer.setTickrate(this.currentTimer.tickrate);
		}
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
		this.sendUpdatePacket(false);
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
		this.sendUpdatePacket(false);
	}

	@Override
	public boolean shouldExcludeSubEntities()
	{
		return this.excludeSubEntities;
	}
	
	@Override
	public boolean hasTimer() 
	{
		return true;
	}
	
	@Override
	public void sync(boolean excluded, boolean shouldExcludeSubEntities, float baseTickrate, float currentTickrate, boolean baseChangeSubEntities, boolean currentChangeSubEntities)
	{
		this.excluded = excluded;
		this.excludeSubEntities = shouldExcludeSubEntities;
		this.baseTimer.setTickrate(baseTickrate);
		this.baseTimer.shouldChangeSubEntities = baseChangeSubEntities;
		this.currentTimer.setTickrate(currentTickrate);
		this.currentTimer.shouldChangeSubEntities = currentChangeSubEntities;
	}
	
	private void sendUpdatePacket(boolean reset) 
	{
		if(!this.entity.level.isClientSide)
		{
			TickrateNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.entity), new UpdateTickratePacket(this.entity.getUUID(), this, reset));
		}
	}
}