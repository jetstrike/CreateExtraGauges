package net.liukrast.eg.mixin;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import net.liukrast.eg.content.logistics.link.DisplayCollectorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = DisplayLinkBlockEntity.class, remap = false)
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
}
