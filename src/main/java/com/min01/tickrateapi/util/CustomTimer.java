package com.min01.tickrateapi.util;

public class CustomTimer
{
	public float partialTick;
	public float tickDelta;
	public long lastMs;
	public float msPerTick;
	public float tickrate;
	public boolean shouldChangeSubEntities = true;
    public float accumulator = 0.0F;
    public int pendingTicks = 0;

	public CustomTimer(float p_92523_, long p_92524_)
	{
		this.tickrate = p_92523_;
		this.msPerTick = 1000.0F / p_92523_;
		this.lastMs = p_92524_;
	}

	public int advanceTime(long p_92526_) 
	{
		this.tickDelta = (float)(p_92526_ - this.lastMs) / this.msPerTick;
		this.lastMs = p_92526_;
		this.partialTick += this.tickDelta;
		int i = (int)this.partialTick;
		this.partialTick -= (float)i;
		return i;
	}
	
	public void setTickrate(float p_92523_)
	{
		this.tickrate = p_92523_;
		this.msPerTick = 1000.0F / p_92523_;
	}
	
	@Override
	public boolean equals(Object obj)
	{
	    if(!(obj instanceof CustomTimer))
	    {
	    	return false;
	    }
	    CustomTimer other = (CustomTimer) obj;
	    return this.tickrate == other.tickrate;
	}
}