package net.louislourson.elytraautoflight.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import net.louislourson.elytraautoflight.ElytraAutoFlight;
import net.louislourson.elytraautoflight.GraphDataPoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Inject(at = @At(value = "HEAD"), method = "draw")
	private void drawPre(float partialTicks, CallbackInfo ci) {
		if (!ci.isCancelled()) {
			//System.out.println("drawPre mixin!");
			//InGameHudDrawCallback.Pre.EVENT.invoker().draw(partialTicks);
		}
	}

	@Inject(at = @At(value = "RETURN"), method = "draw")
	private void drawPost(float partialTicks, CallbackInfo ci) {
		if (!ci.isCancelled()) {
			//System.out.println("drawPost mixin!");
			//InGameHudDrawCallback.Post.EVENT.invoker().draw(partialTicks);

			ElytraAutoFlight elytraAutoFlight = ElytraAutoFlight.instance;

			if (elytraAutoFlight.showHud) {
				MinecraftClient.getInstance().textRenderer.drawWithShadow(
						elytraAutoFlight.hudString, 5, 100, 0xFFFFFF);

				DrawableHelper.fill(elytraAutoFlight.guiX, elytraAutoFlight.guiY, elytraAutoFlight.guiX + elytraAutoFlight.guiWidth, elytraAutoFlight.guiY + elytraAutoFlight.guiHeight, 0x55FFFFFF);

				double maxAltitude = 0;
				for (GraphDataPoint p : elytraAutoFlight.graph) {
					if (p.realPosition.y > maxAltitude) maxAltitude = p.realPosition.y;
				}


				if (maxAltitude > 0) {
					double currentXd = 0;
					double currentYd = 0;

					float float_1 = (float)(0xFFFF0000 >> 24 & 255) / 255.0F;
					float float_2 = (float)(0xFFFF0000 >> 16 & 255) / 255.0F;
					float float_3 = (float)(0xFFFF0000 >> 8 & 255) / 255.0F;
					float float_4 = (float)(0xFFFF0000 & 255) / 255.0F;

					Tessellator tessellator_1 = Tessellator.getInstance();BufferBuilder bufferBuilder_1 = tessellator_1.getBufferBuilder();
					GlStateManager.enableBlend();
					GlStateManager.disableTexture();
					GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					GlStateManager.color4f(float_2, float_3, float_4, float_1);
					bufferBuilder_1.begin(3, VertexFormats.POSITION);

					for (GraphDataPoint p : elytraAutoFlight.graph) {
						int currentX = (int)currentXd;
						int currentY = (int)(p.realPosition.y * elytraAutoFlight.guiHeight / maxAltitude);

						double screenX = elytraAutoFlight.guiX + currentX;
						double screenY = elytraAutoFlight.guiY + elytraAutoFlight.guiHeight - currentY;

						bufferBuilder_1.vertex(screenX, screenY, 0.0D).next();

						currentXd += p.horizontalDelta * elytraAutoFlight.guiWidth / elytraAutoFlight.guiGraphRealWidth;
					}

					tessellator_1.draw();
					GlStateManager.enableTexture();
					GlStateManager.disableBlend();




				}

			}
		}
	}

}


/*
						DrawableHelper.fill(
								elytraAutoFlight.guiX + currentX,
								elytraAutoFlight.guiY + elytraAutoFlight.guiHeight - currentY,
								elytraAutoFlight.guiX + currentX + elytraAutoFlight.guiGraphDotWidth,
								elytraAutoFlight.guiY + elytraAutoFlight.guiHeight - currentY + elytraAutoFlight.guiGraphDotWidth,
								0xFFFF0000);

 */