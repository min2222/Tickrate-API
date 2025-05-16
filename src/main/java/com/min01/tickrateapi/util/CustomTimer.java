package com.min01.tickrateapi.util;

import net.minecraft.client.Timer;

public class CustomTimer extends Timer
{
	public float tickrate;
	public boolean shouldChangeSubEntities = true;

	public CustomTimer(float p_92523_, long p_92524_)
	{
		super(p_92523_, p_92524_);
		this.tickrate = p_92523_;
	}
}
