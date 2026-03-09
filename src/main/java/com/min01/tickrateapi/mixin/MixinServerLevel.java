package com.min01.tickrateapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.min01.tickrateapi.misc.TickrateSecurityManager;
import com.min01.tickrateapi.util.TickrateUtil;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

@Mixin(ServerLevel.class)
public class MixinServerLevel
{
	private final TickrateSecurityManager manager = new TickrateSecurityManager();
	
	@Inject(at = @At("HEAD"), method = "addFreshEntity")
	private void addFreshEntity(Entity pEntity, CallbackInfoReturnable<Boolean> ci)
	{
		Class<?>[] ctx = this.manager.getContext();
		for(Class<?> clazz : ctx)
		{
			if(TickrateUtil.ENTITY_MAP.containsKey(clazz.hashCode()))
			{
				Entity entity = TickrateUtil.ENTITY_MAP.get(clazz.hashCode());
				if(entity != null)
				{
					if(TickrateUtil.hasTimer(entity) && TickrateUtil.shouldChangeSubEntities(entity))
					{
						TickrateUtil.setBaseTickrate(pEntity, TickrateUtil.getTickrate(entity));
					}
					if(TickrateUtil.isExcluded(entity) && TickrateUtil.shouldExcludeSubEntities(entity))
					{
						TickrateUtil.excludeEntity(pEntity);
					}
				}
			}
			else if(TickrateUtil.ENTITY_MAP2.containsKey(clazz.hashCode()))
			{
				Entity entity = TickrateUtil.ENTITY_MAP2.get(clazz.hashCode());
				if(entity != null)
				{
					if(TickrateUtil.hasTimer(entity) && TickrateUtil.shouldChangeSubEntities(entity))
					{
						TickrateUtil.setBaseTickrate(pEntity, TickrateUtil.getTickrate(entity));
					}
					if(TickrateUtil.isExcluded(entity) && TickrateUtil.shouldExcludeSubEntities(entity))
					{
						TickrateUtil.excludeEntity(pEntity);
					}
				}
			}
		}
	}
}
