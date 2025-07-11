package com.min01.tickrateapi.mixin;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.min01.tickrateapi.config.TimerConfig;
import com.min01.tickrateapi.util.CustomTimer;
import com.min01.tickrateapi.util.ITime;
import com.min01.tickrateapi.util.TickrateUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TrackingEmitter;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(ParticleEngine.class)
public class MixinParticleEngine
{
	@Shadow
	protected ClientLevel level;
	
	@Shadow
	@Final
	private Map<ParticleRenderType, Queue<Particle>> particles;
	
	@Shadow
	@Final
	private Queue<TrackingEmitter> trackingEmitters;
	
	@Shadow
	@Final
	private Queue<Particle> particlesToAdd;
	
	@Unique
	private final CustomTimer timer = new CustomTimer(20.0F, 0L);
	
	@Inject(at = @At(value = "HEAD"), method = "tick", cancellable = true)
	private void tick(CallbackInfo ci)
	{
		if(TickrateUtil.hasDimensionTimer(this.level.dimension()) && TickrateUtil.isExcluded(Minecraft.getInstance().player))
		{
			ci.cancel();
			int j = ((ITime)this.level).getTime();
			for(int k = 0; k < Math.min(TimerConfig.disableTickrateLimit.get() ? 500 : 10, j); ++k)
			{
				this.particles.forEach((p_288249_, p_288250_) ->
				{
					this.level.getProfiler().push(p_288249_.toString());
					this.tickParticleList(p_288250_);
					this.level.getProfiler().pop();
				});
				if(!this.trackingEmitters.isEmpty()) 
				{
					List<TrackingEmitter> list = Lists.newArrayList();
					for(TrackingEmitter trackingemitter : this.trackingEmitters)
					{
						trackingemitter.tick();
						if(!trackingemitter.isAlive()) 
						{
							list.add(trackingemitter);
						}
					}
					this.trackingEmitters.removeAll(list);
				}
				Particle particle;
				if(!this.particlesToAdd.isEmpty()) 
				{
					while((particle = this.particlesToAdd.poll()) != null)
					{
						this.particles.computeIfAbsent(particle.getRenderType(), (p_107347_) -> 
						{
							return EvictingQueue.create(16384);
						}).add(particle);
					}
				}
			}
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "tickParticleList", cancellable = true)
	private void tickParticleList(Collection<Particle> particles, CallbackInfo ci)
	{
		if(!particles.isEmpty()) 
		{
			Iterator<Particle> iterator = particles.iterator();
			while(iterator.hasNext())
			{
				Particle particle = iterator.next();
				Vec3 pos = new Vec3(particle.x, particle.y, particle.x);
				AABB aabb = new AABB(pos, pos).inflate(1.0F);
				Pair<Boolean, Float> pair = TickrateUtil.getArea(this.level.dimension(), aabb);
				if(pair.getLeft())
				{
					ci.cancel();
					this.timer.setTickrate(pair.getRight());
					int j = this.timer.advanceTime(Util.getMillis());
					for(int k = 0; k < Math.min(TimerConfig.disableTickrateLimit.get() ? 500 : 10, j); ++k)
					{
						this.tickParticle(particle);
					}
					if(!particle.isAlive())
					{
						particle.getParticleGroup().ifPresent((p_172289_) ->
						{
							this.updateCount(p_172289_, -1);
						});
						iterator.remove();
					}
				}
			}
		}
	}
	
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V")
	private void render(Particle instance, VertexConsumer p_107261_, Camera p_107262_, float p_107263_)
	{
		if(TickrateUtil.hasDimensionTimer(this.level.dimension()) && TickrateUtil.isExcluded(Minecraft.getInstance().player))
		{
			instance.render(p_107261_, p_107262_, TickrateUtil.getDimensionTimer(this.level.dimension()).partialTick);
		}
		else
		{
			instance.render(p_107261_, p_107262_, p_107263_);
		}
	}
	
	@Shadow
	private void tickParticleList(Collection<Particle> particles)
	{
		
	}
	
	@Shadow
	private void updateCount(ParticleGroup p_172282_, int p_172283_) 
	{
		
	}

	@Shadow
	private void tickParticle(Particle p_107394_)
	{
			   
	}
}
