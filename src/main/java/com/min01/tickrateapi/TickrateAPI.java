package com.min01.tickrateapi;

import com.min01.tickrateapi.capabilities.TickrateCapabilities;
import com.min01.tickrateapi.capabilities.TickrateCapabilityImpl;
import com.min01.tickrateapi.config.TimerConfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(TickrateAPI.MODID)
public class TickrateAPI 
{
	public static final String MODID = "tickrateapi";
	
	public TickrateAPI(IEventBus bus, ModContainer container) 
	{
		container.registerConfig(Type.COMMON, TimerConfig.CONFIG_SPEC, "tickrate-api.toml");
		bus.addListener(RegisterCapabilitiesEvent.class, event -> 
		{
			for(EntityType<?> type : BuiltInRegistries.ENTITY_TYPE)
			{
				event.registerEntity(TickrateCapabilities.TICKRATE, type, (entity, ctx) -> new TickrateCapabilityImpl());
			}
		});
	}
}
