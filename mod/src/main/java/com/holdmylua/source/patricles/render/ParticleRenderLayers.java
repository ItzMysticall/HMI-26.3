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
import java.util.function.Function;
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

   public static RenderType additiveParticle(Identifier texture) {
      return ADDITIVE_PARTICLE_LAYER.apply(texture);
   }
}
