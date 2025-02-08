package com.min01.tickrateapi.command;

import java.util.Collection;

import com.min01.tickrateapi.util.TickrateUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class SetTickrateCommand 
{
	public static void register(CommandDispatcher<CommandSourceStack> p_214446_)
	{
		p_214446_.register(Commands.literal("setTickrate").requires((p_137777_) -> 
		{
			return p_137777_.hasPermission(2);
		}).then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("tickrate", FloatArgumentType.floatArg()).executes((p_137810_) ->
		{
			return setTickrate(p_137810_.getSource(), EntityArgument.getEntities(p_137810_, "targets"), FloatArgumentType.getFloat(p_137810_, "tickrate"));
		}))).then(Commands.argument("targets", EntityArgument.entities()).executes((p_137810_) ->
		{
			return setTickrate(p_137810_.getSource(), EntityArgument.getEntities(p_137810_, "targets"), 20);
		})));
	}
	
	private static int setTickrate(CommandSourceStack source, Collection<? extends Entity> entities, float tickrate) 
	{
		for(Entity entity : entities) 
		{
			if(tickrate == 20)
			{
				TickrateUtil.resetTickrate(entity);
				source.sendSuccess(Component.literal("Reseted Tickrate of " + entity.getDisplayName().getString()), true);
			}
			else
			{
				TickrateUtil.setTickrate(entity, tickrate);
				source.sendSuccess(Component.literal("Changed Tickrate of " + entity.getDisplayName().getString() + " to " + tickrate), true);
			}
		}
		return entities.size();
	}
}
