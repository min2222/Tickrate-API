package com.min01.tickrateapi.capabilities;

import com.min01.tickrateapi.network.TickrateNetwork;
import com.min01.tickrateapi.network.UpdateTickratePacket;
import com.min01.tickrateapi.util.CustomTimer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;

public class TickrateCapabilityImpl implements ITickrateCapability
{
	private CustomTimer timer = new CustomTimer(20.0F, 0);
	private Entity entity;
	private boolean excluded;
	private boolean excludeSubEntities;
	private boolean hasTimer;
	
	@Override
	public CompoundTag serializeNBT() 
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
	public void deserializeNBT(CompoundTag nbt)
	{
		this.timer = new CustomTimer(nbt.getFloat("Tickrate"), 0);
		this.timer.shouldChangeSubEntities = nbt.getBoolean("ChangeSubEntities");
		this.excluded = nbt.getBoolean("Excluded");
		this.excludeSubEntities = nbt.getBoolean("ExcludeSubEntities");
		this.hasTimer = nbt.getBoolean("HasTimer");
	}

	@Override
	public void setEntity(Entity entity) 
	{
		this.entity = entity;
	}

	@Override
	public void setTimer(CustomTimer timer) 
	{
		this.timer = timer;
		this.hasTimer = true;
		this.sendUpdatePacket(false);
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
		this.sendUpdatePacket(true);
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
		return this.hasTimer;
	}
	
	private void sendUpdatePacket(boolean reset) 
	{
		if(this.entity instanceof ServerPlayer)
		{
			TickrateNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.entity), new UpdateTickratePacket(this.entity.getUUID(), this, reset));
		}
	}
}
