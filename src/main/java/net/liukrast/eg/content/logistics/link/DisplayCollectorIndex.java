package net.liukrast.eg.content.logistics.link;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Runtime lookup of loaded display collectors keyed by the position of the source
 * block they observe. Unlike {@link net.liukrast.eg.mixinExtension.DCFinder}, which
 * requires a mixin into each source's block entity, this works for any display
 * source (vanilla Create or third-party mods), so push updates via
 * {@code DisplayLinkBlock.notifyGatherers}/{@code sendToGatherers} reach collectors
 * that are not adjacent to the source.
 */
public final class DisplayCollectorIndex {
    private static final Map<LevelAccessor, Map<BlockPos, Set<BlockPos>>> COLLECTORS_BY_SOURCE = new WeakHashMap<>();

    private DisplayCollectorIndex() {}

    static synchronized void add(LevelAccessor level, BlockPos source, BlockPos collector) {
        COLLECTORS_BY_SOURCE.computeIfAbsent(level, $ -> new HashMap<>())
                .computeIfAbsent(source.immutable(), $ -> new HashSet<>())
                .add(collector.immutable());
    }

    static synchronized void remove(LevelAccessor level, BlockPos source, BlockPos collector) {
        var bySource = COLLECTORS_BY_SOURCE.get(level);
        if (bySource == null) return;
        var collectors = bySource.get(source);
        if (collectors == null) return;
        collectors.remove(collector);
        if (collectors.isEmpty()) bySource.remove(source);
        if (bySource.isEmpty()) COLLECTORS_BY_SOURCE.remove(level);
    }

    public static synchronized Set<BlockPos> get(LevelAccessor level, BlockPos source) {
        var bySource = COLLECTORS_BY_SOURCE.get(level);
        if (bySource == null) return Set.of();
        var collectors = bySource.get(source);
        return collectors == null ? Set.of() : Set.copyOf(collectors);
    }
}
