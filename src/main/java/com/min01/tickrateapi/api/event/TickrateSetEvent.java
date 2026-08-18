package com.min01.tickrateapi.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

//use this for TickrateUtil#setTickrate
public class TickrateSetEvent extends EntityEvent
{
	public TickrateSetEvent(Entity entity) 
	{
		super(entity);
	}
}
