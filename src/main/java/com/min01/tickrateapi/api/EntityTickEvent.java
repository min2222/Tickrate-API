package com.min01.tickrateapi.api;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

//unlike LivingTickEvent, this event is fired for every entities.
//and still fired even if tickrate of entity is 0.
//fired on both side.
public class EntityTickEvent extends EntityEvent
{
	public EntityTickEvent(Entity entity) 
	{
		super(entity);
	}
}
