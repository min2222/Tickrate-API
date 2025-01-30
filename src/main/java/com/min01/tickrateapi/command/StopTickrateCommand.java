package com.min01.tickrateapi.command;

import java.util.Collection;

import com.min01.tickrateapi.util.TickrateUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class StopTickrateCommand 
{
	public static void register(CommandDispatcher<CommandSourceStack> p_214446_)
	{
		p_214446_.register(Commands.literal("stopTickrate").requires((p_137777_) -> 
		{
			return p_137777_.hasPermission(2);
		}).then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("excludeEntities", EntityArgument.entities()).then(Commands.argument("stop", BoolArgumentType.bool()).executes((p_137810_) ->
		{
			return stopTickrate(p_137810_.getSource(), EntityArgument.getPlayer(p_137810_, "player"), EntityArgument.getEntities(p_137810_, "excludeEntities"), BoolArgumentType.getBool(p_137810_, "stop"));
		})))));
	}
	
	private static int stopTickrate(CommandSourceStack source, ServerPlayer player, Collection<? extends Entity> entities, boolean stop) 
	{
		for(Entity entity : entities) 
		{
			TickrateUtil.stopOrUnstopTime(stop, player.level);
			TickrateUtil.excludeOrIncludeEntity(stop, entity);
			source.sendSuccess(Component.literal("Stopped Tickrate of entire world in " + player.level.dimension().location().toString()), true);
		}
		return entities.size();
	}
}
