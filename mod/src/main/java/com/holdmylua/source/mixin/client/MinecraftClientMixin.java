package com.holdmylua.source.mixin.client;

import com.holdmylua.source.access.LivingEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Minecraft.class})
public class MinecraftClientMixin {
   @Shadow
   @Nullable
   public LocalPlayer player;
   @Shadow
   @Nullable
   public ClientLevel level;
   @Shadow
   @Final
   public GameRenderer gameRenderer;
   @Shadow
   @Nullable
   public MultiPlayerGameMode gameMode;
   @Shadow
   private int rightClickDelay;
   @Shadow
   @Nullable
   public HitResult hitResult;
   @Shadow
   @Final
   private static Logger LOGGER;

   @Inject(
      method = {"startAttack"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z"
      )}
   )
   public void doAttackMix(CallbackInfoReturnable<Boolean> cir) {
      if (this.player instanceof LivingEntityAccessor mixin) {
         mixin.hMI5_0$resetMainHandSwing(false);
      }
   }

   @Redirect(
      method = {"startUseItem"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z"
      )
   )
   // PORT-26.3: LocalPlayer.swing(InteractionHand)V became swing(InteractionHand, SwingAnimation, boolean)Z
   // and startUseItem now calls it from three places; all of them are redirected here.
   private boolean doItemUse(LocalPlayer instance, InteractionHand hand, SwingAnimation animation, boolean fromServer) {
      if (instance instanceof LivingEntityAccessor accessor) {
         if (hand == InteractionHand.MAIN_HAND) {
            accessor.hMI5_0$resetMainHandSwing(true);
         } else {
            accessor.hMI5_0$resetOffHandSwing(true);
         }
      }

      return instance.swing(hand, animation, fromServer);
   }
}
