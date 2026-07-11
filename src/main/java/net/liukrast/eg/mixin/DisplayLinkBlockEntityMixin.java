package net.liukrast.eg.mixin;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import net.liukrast.eg.content.logistics.link.DisplayCollectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(DisplayLinkBlockEntity.class)
public class DisplayLinkBlockEntityMixin {
    @Redirect(
        method = "updateGatheredData",
        at = @At(value = "INVOKE", target = "Ljava/util/List;contains(Ljava/lang/Object;)Z")
    )
    private boolean ignoreMissingSourceForCollector(List<?> list, Object o) {
        // If this is a DisplayCollector, never consider the source missing (so activeSource is never set to null)
        if ((Object) this instanceof DisplayCollectorBlockEntity) {
            return true;
        }
        return list.contains(o);
    }

    @Redirect(
        method = "updateGatheredData",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isLoaded(Lnet/minecraft/core/BlockPos;)Z")
    )
    private boolean redirectIsLoaded(Level level, BlockPos pos) {
        if ((Object) this instanceof DisplayCollectorBlockEntity dc) {
            if (pos.equals(dc.getTargetPosition())) {
                return level.isLoaded(pos);
            }
            var server = level.getServer();
            if (server != null) {
                for (var serverLevel : server.getAllLevels()) {
                    if (serverLevel.isLoaded(pos)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return level.isLoaded(pos);
    }
}
