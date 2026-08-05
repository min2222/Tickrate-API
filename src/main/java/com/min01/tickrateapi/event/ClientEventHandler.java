package com.min01.tickrateapi.event;

import com.min01.tickrateapi.TickrateAPI;
import com.min01.tickrateapi.api.TickrateArea;
import com.min01.tickrateapi.world.TickrateClientData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TickrateAPI.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler 
{
	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
		Camera camera = event.getCamera();
		Stage stage = event.getStage();
		if(stage == Stage.AFTER_CUTOUT_BLOCKS)
		{
			if(dispatcher.shouldRenderHitBoxes())
			{
				PoseStack stack = event.getPoseStack();
				MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();
				VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
				for(TickrateArea area : TickrateClientData.AREAS)
				{
					if(area.dimension.equals(level.dimension()))
					{
						AABB aabb = area.aabb;
						Vec3 center = aabb.getCenter();
						Vec3 camPos = camera.getPosition();
						Vec3 relative = center.subtract(camPos);
						stack.pushPose();
						stack.translate(relative.x, relative.y, relative.z);
			            LevelRenderer.renderLineBox(stack, consumer, aabb.move(center.reverse()), 1.0F, 1.0F, 0.0F, 1.0F);
			            stack.popPose();
					}
				}
			}
		}
	}
	
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event)
    {
    	TickrateClientData.clear();
    }
}
