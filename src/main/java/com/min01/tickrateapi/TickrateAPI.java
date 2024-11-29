package com.min01.tickrateapi;

import com.min01.tickrateapi.config.TimerConfig;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;

@Mod(TickrateAPI.MODID)
public class TickrateAPI 
{
	public static final String MODID = "tickrateapi";
	
	public TickrateAPI(IEventBus bus, ModContainer container) 
	{
		container.registerConfig(Type.COMMON, TimerConfig.CONFIG_SPEC, "tickrate-api.toml");
	}
}
