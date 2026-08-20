package com.min01.tickrateapi.capabilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.min01.tickrateapi.api.TickrateData;
import com.min01.tickrateapi.api.TickrateTimer;
import com.min01.tickrateapi.network.TickrateNetwork;
import com.min01.tickrateapi.network.UpdateTickratePacket;
import com.min01.tickrateapi.util.TickrateUtil;

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
	
	private final TickrateTimer baseTimer = TickrateTimer.createDefault();
	private final TickrateTimer currentTimer = TickrateTimer.createDefault();
	
	private boolean isUpdating;
	private int priority = 1000;
	private float delayedTickrate = -1;
	
	@Override
	public CompoundTag serializeNBT() 
	{
		CompoundTag nbt = new CompoundTag();
		nbt.putFloat("BaseTickrate", this.getBaseTickrate());
		nbt.putInt("Priority", this.getPriority());
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		this.setBaseTickrate(nbt.getFloat("BaseTickrate"));
		this.setPriority(nbt.getInt("Priority"));
	}

	@Override
	public void tick(Entity entity) 
	{
		int prevPriority = this.getPriority();
		float prevBaseTickrate = this.getBaseTickrate();
		float prevTickrate = this.getTickrate();
		
		this.setTickrate(this.getBaseTickrate());
		this.isUpdating = true;
		
		try
		{
			TickrateData data = TickrateUtil.findHighestPriorityTickrate(entity);
			if(data != null && data.getPriority() > this.getPriority())
			{
			    this.setTickrate(data.getTickrate());
			}
			if(this.delayedTickrate != -1.0F)
			{
				this.setTickrate(this.delayedTickrate);
				this.delayedTickrate = -1.0F;
			}
		}
        finally
        {
        	this.isUpdating = false;
        }

		if(this.getPriority() != prevPriority || this.getBaseTickrate() != prevBaseTickrate || this.getTickrate() != prevTickrate || entity.tickCount <= 4)
		{
			if(!entity.level.isClientSide)
			{
				TickrateNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), new UpdateTickratePacket(entity.getUUID(), this.getPriority(), this.getBaseTickrate(), this.getTickrate()));
			}
		}
	}

	@Override
	public void setBaseTickrate(float tickrate) 
	{
		this.baseTimer.setTickrate(tickrate);
	}
	
	@Override
	public void setTickrate(float tickrate) 
	{
		if(this.isUpdating)
		{
			this.currentTimer.setTickrate(tickrate);
		}
		else if(this.delayedTickrate == -1.0F)
		{
			this.delayedTickrate = tickrate;
		}
	}
	
	@Override
	public void setPriority(int priority) 
	{
		this.priority = priority;
	}
	
	@Override
	public void resetTickrate()
	{
		this.baseTimer.resetTickrate();
	}
	
	@Override
	public void sync(int priority, float baseTickrate, float currentTickrate)
	{
		this.setPriority(priority);
		this.setBaseTickrate(baseTickrate);
		this.setTickrate(currentTickrate);
	}
	
	@Override
	public boolean hasTimer() 
	{
		return this.getTickrate() != 20.0F;
	}
	
	@Override
	public int getPriority()
	{
		return this.priority;
	}
	
	@Override
	public float getBaseTickrate()
	{
		return this.baseTimer.tickrate;
	}
	
	@Override
	public float getTickrate() 
	{
		return this.currentTimer.tickrate;
	}
	
	@Override
	public TickrateTimer getBaseTimer() 
	{
		return this.baseTimer;
	}
	
	@Override
	public TickrateTimer getTimer() 
	{
		return this.currentTimer;
	}
	
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) 
	{
		return TICKRATE.orEmpty(cap, LazyOptional.of(() -> this));
	}
}
