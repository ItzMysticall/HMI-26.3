package com.holdmylua.source.patricles.render;

import com.holdmylua.source.mixin.render.RenderTypeInvoker;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class ParticleRenderLayers {
   static BlendFunction ADDITIVE = new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE);
   // PORT-26.1: RenderPipelines.GUI_TEXTURED_SNIPPET and RenderPipelines.register are no
   // longer accessible; the pipeline is built standalone with the same shaders/format and
   // is compiled lazily on first use instead of being preload-registered.
   // PORT-26.2: SourceFactor/DestFactor merged into BlendFactor; the withUniform/withSampler
   // calls moved to BindGroupLayout; withVertexFormat split into withVertexBinding +
   // withPrimitiveTopology.
   static RenderPipeline ADDITIVE_PARTICLE = RenderPipeline.builder()
      .withBindGroupLayout(BindGroupLayouts.PROJECTION)
      .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
      .withVertexShader("core/position_tex_color")
      .withFragmentShader("core/position_tex_color")
      .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withLocation("pipeline/additive_particle_effect")
      .withColorTargetState(new ColorTargetState(ADDITIVE))
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .build();
   private static final Function<Identifier, RenderType> ADDITIVE_PARTICLE_LAYER = Util.memoize(
      texture -> RenderTypeInvoker.hmi$create(
         "fire_screen_effect", RenderSetup.builder(ADDITIVE_PARTICLE).withTexture("Sampler0", texture).createRenderSetup()
      )
   );

   private static final Logger LOGGER = LoggerFactory.getLogger("holdmyitems");
   private static long retryAtNanos;
   private static boolean loggedNotReady;

   /**
    * Returns the additive particle render type, or null while its pipeline is not compiled yet.
    * <p>
    * PORT-26.3: the pipeline is compiled lazily and PipelineCache.get returns null until it is
    * ready. Vanilla tolerates that, but Iris' RenderSystem.getCompiledPipelineNullable hook
    * dereferences the result without a null check and crashes the render thread (NPE in
    * redirectIrisProgram). Asking for the compiled pipeline here, inside a try/catch, both
    * starts the compile early and keeps the draw from ever reaching that hook unprepared.
    * It is checked on every call (not cached) because a resource reload rebuilds the cache.
    */
   public static RenderType additiveParticle(Identifier texture) {
      return additiveReady() ? ADDITIVE_PARTICLE_LAYER.apply(texture) : null;
   }

   private static boolean additiveReady() {
      long now = System.nanoTime();
      if (now < retryAtNanos) {
         return false;
      }
      try {
         if (RenderSystem.getCompiledPipelineNullable(ADDITIVE_PARTICLE) != null) {
            return true;
         }
      } catch (Throwable t) {
         if (!loggedNotReady) {
            loggedNotReady = true;
            LOGGER.warn("Additive particle pipeline is not usable yet ({}); skipping those particles until it is", String.valueOf(t));
         }
      }
      retryAtNanos = now + 100_000_000L; // retry in 100 ms
      return false;
   }
}
