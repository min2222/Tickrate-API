package com.min01.tickrateapi.util;

public class CustomTimer
{
	public float tickDelta;
	private float msPerTick;
	public float partialTick;
	private long lastMs;
	public float tickrate;
	public boolean shouldChangeSubEntities = true;

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
}
