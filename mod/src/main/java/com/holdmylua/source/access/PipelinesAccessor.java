package com.holdmylua.source.access;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

public interface PipelinesAccessor {
   RenderPipeline customP();

   RenderPipeline custom_solidP();
}
