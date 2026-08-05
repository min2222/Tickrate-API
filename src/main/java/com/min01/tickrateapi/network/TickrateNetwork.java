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
	public static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath(TickrateAPI.MODID, TickrateAPI.MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	
	public static void registerMessages()
	{
		int id = 0;
		CHANNEL.registerMessage(id++, UpdateTickratePacket.class, UpdateTickratePacket::write, UpdateTickratePacket::read, UpdateTickratePacket::handle);
		CHANNEL.registerMessage(id++, AddTickrateAreaPacket.class, AddTickrateAreaPacket::write, AddTickrateAreaPacket::read, AddTickrateAreaPacket::handle);
		CHANNEL.registerMessage(id++, RemoveTickrateAreaPacket.class, RemoveTickrateAreaPacket::write, RemoveTickrateAreaPacket::read, RemoveTickrateAreaPacket::handle);
		CHANNEL.registerMessage(id++, UpdateDimensionTickratePacket.class, UpdateDimensionTickratePacket::write, UpdateDimensionTickratePacket::read, UpdateDimensionTickratePacket::handle);
	}
	
    public static <MSG> void sendToServer(MSG message) 
    {
    	CHANNEL.sendToServer(message);
    }
    
    public static <MSG> void sendNonLocal(MSG msg, ServerPlayer player) 
    {
        CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
    
    public static <MSG> void sendToAll(MSG message)
    {
    	for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) 
    	{
    		CHANNEL.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    	}
    }
}
