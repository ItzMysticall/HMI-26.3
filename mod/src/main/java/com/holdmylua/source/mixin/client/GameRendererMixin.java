package com.holdmylua.source.mixin.client;

import com.holdmylua.source.LuaTestHMI;
import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class GameRendererMixin {
   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   // PORT-26.3: GameRenderer.render() no longer takes (DeltaTracker, boolean); GLFW is gone
   // (the client uses SDL now), so the clock comes from Blaze3D.getTime().
   private void deltaTime(CallbackInfo ci) {
      float currentTime = (float)Blaze3D.getTime();
      LuaTestHMI.deltaTime = currentTime - LuaTestHMI.prevTime;
      LuaTestHMI.prevTime = currentTime;
      if (Minecraft.getInstance().isPaused()) {
         LuaTestHMI.deltaTime = 0.0F;
      } else {
         LuaTestHMI.deltaTime = (float)Math.min(0.05, (double)LuaTestHMI.deltaTime);
      }
   }
}
