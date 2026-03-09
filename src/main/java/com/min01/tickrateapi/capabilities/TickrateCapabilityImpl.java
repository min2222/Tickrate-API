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
	
	private float tickrate;
	
	private Entity entity;
	private boolean excluded;
	private boolean excludeSubEntities;
	private boolean shouldChangeSubEntities = true;
    private boolean isChangingTickrate = false;
	
	@Override
	public CompoundTag serializeNBT() 
	{
		CompoundTag nbt = new CompoundTag();
		nbt.putFloat("CurrentTickrate", this.tickrate);
		nbt.putFloat("BaseTickrate", this.baseTimer.tickrate);
		nbt.putBoolean("ChangeSubEntities", this.shouldChangeSubEntities);
		nbt.putBoolean("Excluded", this.excluded);
		nbt.putBoolean("ExcludeSubEntities", this.excludeSubEntities);
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		this.tickrate = nbt.getFloat("CurrentTickrate");
		this.baseTimer.setTickrate(nbt.getFloat("BaseTickrate"));
		this.shouldChangeSubEntities = nbt.getBoolean("ChangeSubEntities");
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
		this.tickrate = tickrate;
		this.baseTimer.setTickrate(tickrate);
	}
	
	@Override
	public void setTickrate(float tickrate) 
	{
		this.tickrate = tickrate;
	}
	
	@Override
	public float getTickrate() 
	{
		return this.tickrate;
	}
	
	@Override
	public void resetTickrate()
	{
		this.tickrate = 20.0F;
		this.baseTimer.setTickrate(20.0F);
	}

	@Override
	public void tick() 
	{
		this.currentTimer.setTickrate(this.baseTimer.tickrate);
        this.isChangingTickrate = true;
        
        try 
        {
            if(this.isChangingTickrate) 
            {
            	this.currentTimer.setTickrate(this.tickrate);
            }
        }
        finally
        {
        	if(this.baseTimer.tickrate == 20.0F)
        	{
        		this.resetTickrate();
        	}
        	this.isChangingTickrate = false;
        }
		this.sendUpdatePacket();
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
	}
	
	@Override
	public void changeSubEntities(boolean flag)
	{
		this.shouldChangeSubEntities = flag;
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
	
	@Override
	public boolean hasTimer() 
	{
		return this.tickrate != 20.0F;
	}
	
	@Override
	public void sync(boolean excluded, boolean excludeSubEntities, boolean changeSubEntities, float baseTickrate, float currentTickrate)
	{
		this.excluded = excluded;
		this.excludeSubEntities = excludeSubEntities;
		this.shouldChangeSubEntities = changeSubEntities;
		this.baseTimer.setTickrate(baseTickrate);
		this.tickrate = currentTickrate;
	}
	
	private void sendUpdatePacket() 
	{
		if(!this.entity.level.isClientSide)
		{
			TickrateNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.entity), new UpdateTickratePacket(this.entity.getUUID(), this));
		}
	}
	
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) 
	{
		return TICKRATE.orEmpty(cap, LazyOptional.of(() -> this));
	}
}