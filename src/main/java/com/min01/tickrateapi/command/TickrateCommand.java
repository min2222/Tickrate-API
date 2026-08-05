package com.min01.tickrateapi.command;

import java.util.Collection;

import com.min01.tickrateapi.api.TickrateArea;
import com.min01.tickrateapi.api.TickrateTimer;
import com.min01.tickrateapi.util.TickrateUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TickrateCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> pDispatcher)
	{
		pDispatcher.register(Commands.literal("tickrate").requires(ctx -> 
		{
			return ctx.hasPermission(2);
		}).then(Commands.literal("entity").then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("tickrate", FloatArgumentType.floatArg()).executes(ctx ->
		{
			return setEntityTickrate(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), FloatArgumentType.getFloat(ctx, "tickrate"), 1000);
		}).then(Commands.argument("priority", IntegerArgumentType.integer()).executes(ctx ->
		{
			return setEntityTickrate(ctx.getSource(), EntityArgument.getEntities(ctx, "targets"), FloatArgumentType.getFloat(ctx, "tickrate"), IntegerArgumentType.getInteger(ctx, "priority"));
		}))))).then(Commands.literal("dimension").then(Commands.argument("dimension", DimensionArgument.dimension()).then(Commands.argument("tickrate", FloatArgumentType.floatArg()).executes(ctx -> 
		{
			return setDimensionTickrate(ctx.getSource(), DimensionArgument.getDimension(ctx, "dimension"), FloatArgumentType.getFloat(ctx, "tickrate"), 1000);
		}).then(Commands.argument("priority", IntegerArgumentType.integer()).executes(ctx -> 
		{
			return setDimensionTickrate(ctx.getSource(), DimensionArgument.getDimension(ctx, "dimension"), FloatArgumentType.getFloat(ctx, "tickrate"), IntegerArgumentType.getInteger(ctx, "priority"));
		}))))).then(Commands.literal("area").then(Commands.argument("dimension", DimensionArgument.dimension()).then(Commands.argument("pos1", Vec3Argument.vec3()).then(Commands.argument("pos2", Vec3Argument.vec3()).then(Commands.argument("tickrate", FloatArgumentType.floatArg()).executes(ctx -> 
		{
			return addTickrateArea(ctx.getSource(), DimensionArgument.getDimension(ctx, "dimension"), Vec3Argument.getVec3(ctx, "pos1"), Vec3Argument.getVec3(ctx, "pos2"), FloatArgumentType.getFloat(ctx, "tickrate"), 1000);
		}).then(Commands.argument("priority", IntegerArgumentType.integer()).executes(ctx -> 
		{
			return addTickrateArea(ctx.getSource(), DimensionArgument.getDimension(ctx, "dimension"), Vec3Argument.getVec3(ctx, "pos1"), Vec3Argument.getVec3(ctx, "pos2"), FloatArgumentType.getFloat(ctx, "tickrate"), IntegerArgumentType.getInteger(ctx, "priority"));
		}))))))));
	}
	
	private static int addTickrateArea(CommandSourceStack source, ServerLevel serverLevel, Vec3 pos1, Vec3 pos2, float tickrate, int priority)
	{
		AABB aabb = new AABB(pos1, pos2);
		TickrateArea area = new TickrateArea(serverLevel.dimension(), TickrateTimer.createWithTickrate(tickrate), aabb, priority);
		if(tickrate == -1)
		{
			TickrateUtil.removeTickrateArea(serverLevel, area);
			source.sendSuccess(() -> Component.literal("Removed tickrate area in " + serverLevel.dimension().location().toString()), true);
		}
		else
		{
			TickrateUtil.addTickrateArea(serverLevel, area);
			source.sendSuccess(() -> Component.literal("Added new tickrate area in " + serverLevel.dimension().location().toString() + " with " + tickrate + " tickrate"), true);
		}
		return 0;
	}
	
	private static int setDimensionTickrate(CommandSourceStack source, ServerLevel serverLevel, float tickrate, int priority) 
	{
		TickrateUtil.setDimensionTickrate(serverLevel, tickrate, priority);
		source.sendSuccess(() -> Component.literal("Changed Tickrate of " + serverLevel.dimension().location().toString() + " to " + tickrate), true);
		return 0;
	}
	
	private static int setEntityTickrate(CommandSourceStack source, Collection<? extends Entity> entities, float tickrate, int priority) 
	{
		for(Entity entity : entities) 
		{
			TickrateUtil.setBaseTickrate(entity, tickrate);
			TickrateUtil.setPriority(entity, priority);
			source.sendSuccess(() -> Component.literal("Changed Tickrate of " + entity.getDisplayName().getString() + " to " + tickrate), true);
		}
		return entities.size();
	}
}
