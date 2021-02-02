package com.unascribed.fabrication.compat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;

// stolen from Fabric and ported to MCP to make PrideLib work
public interface SimpleResourceReloadListener<T> extends IResourceManagerReloadListener {
	@Override
	default CompletableFuture<Void> reload(IStage stage,
			IResourceManager resourceManager, IProfiler preparationsProfiler,
			IProfiler reloadProfiler, Executor backgroundExecutor,
			Executor gameExecutor) {
		return load(resourceManager, preparationsProfiler, backgroundExecutor).thenCompose(stage::markCompleteAwaitingOthers).thenCompose(
				(o) -> apply(o, resourceManager, reloadProfiler, gameExecutor)
			);
	}

	/**
	 * Asynchronously process and load resource-based data. The code
	 * must be thread-safe and not modify game state!
	 *
	 * @param manager  The resource manager used during reloading.
	 * @param profiler The profiler which may be used for this stage.
	 * @param executor The executor which should be used for this stage.
	 * @return A CompletableFuture representing the "data loading" stage.
	 */
	CompletableFuture<T> load(IResourceManager manager, IProfiler profiler, Executor executor);

	/**
	 * Synchronously apply loaded data to the game state.
	 *
	 * @param manager  The resource manager used during reloading.
	 * @param profiler The profiler which may be used for this stage.
	 * @param executor The executor which should be used for this stage.
	 * @return A CompletableFuture representing the "data applying" stage.
	 */
	CompletableFuture<Void> apply(T data, IResourceManager manager, IProfiler profiler, Executor executor);
}
