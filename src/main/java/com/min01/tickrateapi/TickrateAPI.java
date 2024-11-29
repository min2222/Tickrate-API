package com.min01.tickrateapi;

import com.min01.tickrateapi.config.TimerConfig;
import com.min01.tickrateapi.network.TickrateNetwork;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;

@Mod(TickrateAPI.MODID)
public class TickrateAPI 
{
	public static final String MODID = "tickrateapi";
	
	public TickrateAPI() 
	{
		ModLoadingContext ctx = ModLoadingContext.get();
		TickrateNetwork.registerMessages();
		ctx.registerConfig(Type.COMMON, TimerConfig.CONFIG_SPEC, "tickrate-api.toml");
	}
}
