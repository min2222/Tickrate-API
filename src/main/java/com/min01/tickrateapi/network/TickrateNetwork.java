package com.min01.tickrateapi.network;

import com.min01.tickrateapi.TickrateAPI;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = TickrateAPI.MODID, bus = EventBusSubscriber.Bus.MOD)
public class TickrateNetwork
{
	@SubscribeEvent
	public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event)
	{
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToClient(TimerSyncPacket.TYPE, TimerSyncPacket.STREAM_CODEC, TimerSyncPacket::handle);
	}

	public static void sendToAll(CustomPacketPayload message)
	{
		PacketDistributor.sendToAllPlayers(message);
	}
}
