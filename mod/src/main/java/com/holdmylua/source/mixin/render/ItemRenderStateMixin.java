package com.holdmylua.source.mixin.render;

import com.holdmylua.source.global.DispatcherStorage;
import com.holdmylua.source.global.item_model.ItemModelStorage;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({FeatureRenderDispatcher.class})
public class ItemRenderStateMixin {
   // PORT-26.3: renderAllFeatures(...) is now a static execute-phase method that no longer sees
   // the item quads' scripts. Item quads are prepared (and the per-item Lua context queued in
   // DispatcherStorage / ItemModelStorage is consumed) inside prepareFrame(SubmitNodeStorage), so
   // the leftover entries are cleared once that has finished instead.
   @Inject(
      method = {"prepareFrame"},
      at = {@At("TAIL")}
   )
   private void tester(SubmitNodeStorage submitNodeStorage, CallbackInfoReturnable<?> cir) {
      DispatcherStorage.clear();
      ItemModelStorage.clear();
   }
}
