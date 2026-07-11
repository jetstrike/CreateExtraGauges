package net.liukrast.eg.mixin;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;

@Mixin(value = DisplayLinkBlockEntity.class, priority = 1500)
public class DisplayLinkBlockEntityMixin {

    @Inject(method = "updateGatheredData", at = @At("HEAD"), cancellable = true)
    private void onUpdateGatheredData(CallbackInfo ci) {
        DisplayLinkBlockEntity link = (DisplayLinkBlockEntity) (Object) this;
        if (link.getLevel() == null || link.getLevel().isClientSide) return;

        BlockPos sourcePosition = link.getSourcePosition();
        BlockPos targetPosition = link.getTargetPosition();
        Level level = link.getLevel();
        var server = level.getServer();
        if (server == null) return;

        // 1. Resolve target cross-level
        Level targetLevel = null;
        DisplayTarget targetObj = null;
        for (var serverLevel : server.getAllLevels()) {
            if (serverLevel.isLoaded(targetPosition)) {
                var tgt = DisplayTarget.get(serverLevel, targetPosition);
                if (tgt != null) {
                    targetObj = tgt;
                    targetLevel = serverLevel;
                    break;
                }
            }
        }

        if (targetObj == null) {
            ci.cancel();
            return;
        }

        // 2. Resolve source cross-level
        Level sourceLevel = null;
        BlockEntity sourceBE = null;
        for (var serverLevel : server.getAllLevels()) {
            if (serverLevel.isLoaded(sourcePosition)) {
                var be = serverLevel.getBlockEntity(sourcePosition);
                if (be != null) {
                    sourceBE = be;
                    sourceLevel = serverLevel;
                    break;
                }
            }
        }

        if (sourceBE == null) {
            ci.cancel();
            return;
        }

        // 3. Update activeTarget
        if (link.activeTarget != targetObj) {
            link.activeTarget = targetObj;
            link.notifyUpdate();
        }

        // 4. Resolve Display Source
        var sources = DisplaySource.getAll(sourceLevel, sourcePosition);
        if (sources.isEmpty()) {
            ci.cancel();
            return;
        }

        var sourceObj = sources.get(0);
        if (link.activeSource != sourceObj) {
            link.activeSource = sourceObj;
            link.notifyUpdate();
        }

        if (link.activeSource == null || link.activeTarget == null) {
            ci.cancel();
            return;
        }

        // 5. Populate sublevel dynamically
        if (sourceBE instanceof NavTableBlockEntity navBE) {
            navBE.subLevel = (SubLevel) Sable.HELPER.getContaining(sourceLevel, sourcePosition);
        }

        // 6. Transfer the data using the targetLevel context
        DisplayLinkContext context = new DisplayLinkContext(targetLevel, link);
        link.activeSource.transferData(context, link.activeTarget, link.targetLine);
        link.sendPulseNextSync();
        link.sendData();

        ci.cancel();
    }
}
