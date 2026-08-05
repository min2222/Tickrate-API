package com.min01.tickrateapi.api;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class TickrateData
{
	public final ResourceKey<Level> dimension;
	public final TickrateTimer timer;
	
	protected int priority = 1000;
	
	public TickrateData(ResourceKey<Level> dimension, TickrateTimer timer, int priority) 
	{
		this.dimension = dimension;
		this.timer = timer;
		this.priority = priority;
	}
	
	public void setPriority(int priority)
	{
		this.priority = priority;
	}
	
	public void setTickrate(float tickrate)
	{
		this.timer.setTickrate(tickrate);
	}
	
	public int getPriority()
	{
		return this.priority;
	}
	
	public float getTickrate()
	{
		return this.timer.tickrate;
	}
}
