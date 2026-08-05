package com.min01.tickrateapi;

import com.min01.tickrateapi.capabilities.TickrateCapabilities;
import com.min01.tickrateapi.network.TickrateNetwork;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TickrateAPI.MODID)
public class TickrateAPI
{
	public static final String MODID = "tickrateapi";
	
	public TickrateAPI(FMLJavaModLoadingContext ctx) 
	{
		TickrateNetwork.registerMessages();
		MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, TickrateCapabilities::onAttachEntityCapabilities);
	}
}
