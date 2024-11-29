package com.min01.tickrateapi.network;

import com.min01.tickrateapi.TickrateAPI;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

public class TickrateNetwork
{
	public static int ID;
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = 
    		NetworkRegistry.newSimpleChannel(new ResourceLocation(TickrateAPI.MODID, "tickrateapi"), 
    				() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	
	public static void registerMessages()
	{
		CHANNEL.registerMessage(ID++, TimerSyncPacket.class, TimerSyncPacket::encode, TimerSyncPacket::new, TimerSyncPacket.Handler::onMessage);
		CHANNEL.registerMessage(ID++, TimeStopSyncPacket.class, TimeStopSyncPacket::encode, TimeStopSyncPacket::new, TimeStopSyncPacket.Handler::onMessage);
	}
	
    public static <MSG> void sendToAll(MSG message) 
    {
    	for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) 
    	{
    		CHANNEL.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    	}
    }
}
