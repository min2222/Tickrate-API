package com.min01.tickrateapi.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CustomTimer
{
	public float partialTick;
	public float tickDelta;
	public long lastMs;
	public float msPerTick;
	public float tickrate;
    public float accumulator = 0.0F;
    public int pendingTicks = 0;
    public int tick;
    public boolean canTick;

	public CustomTimer(float p_92523_, long p_92524_)
	{
		this.tickrate = p_92523_;
		this.msPerTick = 1000.0F / p_92523_;
		this.lastMs = p_92524_;
	}

	@OnlyIn(Dist.CLIENT)
	public int advanceTime(long p_92526_) 
	{
		this.tickDelta = (float)(p_92526_ - this.lastMs) / this.msPerTick;
		this.lastMs = p_92526_;
		this.partialTick += this.tickDelta;
		int i = (int)this.partialTick;
		this.partialTick -= (float)i;
		if(this.tick >= 15)
		{
			int time = (int) Mth.lerp(Minecraft.getInstance().getFrameTime(), i, Minecraft.getInstance().timer.advanceTime(p_92526_));
			return time;
		}
		return i;
	}
	
	public void setTick(int tick)
	{
		this.tick = tick;
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