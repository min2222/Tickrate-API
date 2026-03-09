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
    		NetworkRegistry.newSimpleChannel(ResourceLocation.fromNamespaceAndPath(TickrateAPI.MODID, TickrateAPI.MODID), 
    				() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	
	public static void registerMessages()
	{
		CHANNEL.registerMessage(ID++, UpdateTickratePacket.class, UpdateTickratePacket::write, UpdateTickratePacket::read, UpdateTickratePacket::handle);
		CHANNEL.registerMessage(ID++, UpdateDimensionTickratePacket.class, UpdateDimensionTickratePacket::write, UpdateDimensionTickratePacket::read, UpdateDimensionTickratePacket::handle);
		CHANNEL.registerMessage(ID++, UpdateAreaTickratePacket.class, UpdateAreaTickratePacket::write, UpdateAreaTickratePacket::read, UpdateAreaTickratePacket::handle);
	}
	
    public static <MSG> void sendToServer(MSG message) 
    {
    	CHANNEL.sendToServer(message);
    }
	
    public static <MSG> void sendToAll(MSG message) 
    {
    	for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) 
    	{
    		CHANNEL.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    	}
    }
}
