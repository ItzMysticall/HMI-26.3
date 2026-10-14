package com.holdmylua.source.scripting.custom_api;

import com.holdmylua.source.annotation.Safe;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class KeyBindManager {
   KeyMapping key;

   @Safe
   public boolean isKeyPressed(int keyCode) {
      if (keyCode != 0) {
         // PORT-26.3: GLFW is no longer on the classpath (SDL replaced it); vanilla's own
         // InputConstants.isKeyDown(int) reads the key state now.
         return InputConstants.isKeyDown(keyCode);
      } else {
         return false;
      }
   }
}
