package net.liukrast.eg.mixin;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import net.liukrast.eg.content.logistics.link.DisplayCollectorIndex;
import net.liukrast.eg.mixinExtension.DCFinder;
import net.liukrast.eg.registry.EGBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(DisplayLinkBlock.class)
public class DisplayLinkBlockMixin {

    @Inject(method = "forEachAttachedGatherer", at = @At("HEAD"))
    private static void forEachAttachedGatherer(LevelAccessor level, BlockPos pos, Consumer<DisplayLinkBlockEntity> callback, CallbackInfo ci) {
        Set<BlockPos> collectors = new LinkedHashSet<>();
        if (level instanceof Level l && l.getServer() != null) {
            for (var serverLevel : l.getServer().getAllLevels()) {
                collectors.addAll(DisplayCollectorIndex.get(serverLevel, pos));
            }
        } else {
            collectors.addAll(DisplayCollectorIndex.get(level, pos));
        }

        DCFinder finder = null;
        if (level.getBlockEntity(pos) instanceof DCFinder f) {
            finder = f;
        } else if (level instanceof Level l && l.getServer() != null) {
            for (var serverLevel : l.getServer().getAllLevels()) {
                if (serverLevel.getBlockEntity(pos) instanceof DCFinder f) {
                    finder = f;
                    break;
                }
            }
        }

        if (finder != null) {
            collectors.addAll(finder.extra_gauges$targetingDisplayCollectors());
        }

        for (BlockPos offsetPos : collectors) {
            BlockState blockState = null;
            BlockEntity blockEntity = null;

            if (level.getBlockState(offsetPos).is(EGBlocks.DISPLAY_COLLECTOR.get())) {
                blockState = level.getBlockState(offsetPos);
                blockEntity = level.getBlockEntity(offsetPos);
            } else if (level instanceof Level l && l.getServer() != null) {
                for (var serverLevel : l.getServer().getAllLevels()) {
                    if (serverLevel.getBlockState(offsetPos).is(EGBlocks.DISPLAY_COLLECTOR.get())) {
                        blockState = serverLevel.getBlockState(offsetPos);
                        blockEntity = serverLevel.getBlockEntity(offsetPos);
                        break;
                    }
                }
            }

            if (blockState == null || blockEntity == null)
                continue;
            if (!(blockEntity instanceof DisplayLinkBlockEntity dlbe))
                continue;
            if (dlbe.activeSource == null)
                continue;

            callback.accept(dlbe);
        }
    }
}
