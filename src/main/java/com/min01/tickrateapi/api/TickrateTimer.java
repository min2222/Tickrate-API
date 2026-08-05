package com.min01.tickrateapi.api;

import net.minecraft.Util;

public class TickrateTimer
{
	public float partialTick;
	public float tickDelta;
	public long lastMs;
	public float msPerTick;
	public float tickrate = 20.0F;
    
    //server side;
    public long nextTickTime = Util.getMillis();
    public long lastOverloadWarning;
    public long delayedTasksMaxNextTickTime;
    public boolean mayHaveDelayedTasks;

	public TickrateTimer(float pTicksPerSecond, long pLastMs)
	{
		this.tickrate = pTicksPerSecond;
		this.msPerTick = 1000.0F / pTicksPerSecond;
		this.lastMs = pLastMs;
	}
	
	public static TickrateTimer createDefault()
	{
		return createWithTickrate(20.0F);
	}
	
	public static TickrateTimer createWithTickrate(float tickrate)
	{
		return new TickrateTimer(tickrate, 0L);
	}

	public int advanceTime(long pGameTime) 
	{
		this.tickDelta = (float)(pGameTime - this.lastMs) / this.msPerTick;
		this.lastMs = pGameTime;
		this.partialTick += this.tickDelta;
		int i = (int)this.partialTick;
		this.partialTick -= (float)i;
		return i;
	}
	
	public void tickServer(Runnable run)
	{
	    long currentTime = Util.getMillis();
		long tickrate = (long) (1000L / this.tickrate);
		tickrate = Math.min(tickrate, 1000L);

        long i = currentTime - this.nextTickTime;
        if(i > 2000L && this.nextTickTime - this.lastOverloadWarning >= 15000L)
        {
        	long j = i / tickrate;
        	this.nextTickTime += j * tickrate;
        	this.lastOverloadWarning = this.nextTickTime;
        }
        
        while(currentTime >= this.nextTickTime)
        {
        	this.nextTickTime += tickrate;
        	run.run();
        }
        
        this.mayHaveDelayedTasks = true;
        this.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + tickrate, this.nextTickTime);
	}
	
	public void resetTickrate()
	{
		this.setTickrate(20.0F);
	}
	
	public void setTickrate(float tickrate)
	{
		this.tickrate = tickrate;
		this.msPerTick = 1000.0F / tickrate;
	}
	
	public boolean haveTime()
	{
		return Util.getMillis() < (this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTime : this.nextTickTime);
	}
}