package net.liukrast.eg.mixin;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DisplayLinkContext.class)
public class DisplayLinkContextMixin {
    @Inject(method = "getSourceBlockEntity", at = @At("HEAD"), cancellable = true)
    private void getSourceBlockEntityCrossLevel(CallbackInfoReturnable<BlockEntity> cir) {
        DisplayLinkContext context = (DisplayLinkContext) (Object) this;
        var level = context.level();
        var sourcePos = context.getSourcePos();
        var server = level == null ? null : level.getServer();
        if (server != null) {
            for (var serverLevel : server.getAllLevels()) {
                var be = serverLevel.getBlockEntity(sourcePos);
                if (be != null) {
                    if (be instanceof NavTableBlockEntity navBE) {
                        if (navBE.subLevel == null) {
                            navBE.subLevel = (SubLevel) Sable.HELPER.getContaining(serverLevel, sourcePos);
                        }
                    }
                    cir.setReturnValue(be);
                    return;
                }
            }
        }
    }
}
