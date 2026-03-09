package com.min01.tickrateapi.event;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TickrateAPI.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandlerForge
{
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		if(event.phase == Phase.START)
		{
			if(mc.player == null && mc.level == null)
			{
				TickrateUtil.AABB_LIST.clear();
				TickrateUtil.LEVEL_MAP.clear();
			}
		}
	}
}
