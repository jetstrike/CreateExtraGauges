package net.liukrast.eg.mixin;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSupportBehaviour;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.liukrast.eg.ExtraGauges;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.liukrast.eg.content.logistics.link.RelativeNBTUtils;

import java.util.Iterator;
import java.util.List;

@Mixin(value = FactoryPanelSupportBehaviour.class, remap = false)
public abstract class FactoryPanelSupportBehaviourMixin {

    @Shadow
    private List<FactoryPanelPosition> linkedPanels;

    @Redirect(
        method = "notifyPanels",
        at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V")
    )
    private void redirectRemoveNotify(Iterator<FactoryPanelPosition> iterator, @Local FactoryPanelPosition panelPos) {
        BlockState state = ((FactoryPanelSupportBehaviour) (Object) this).getWorld().getBlockState(panelPos.pos());
        boolean isGauge = AllBlocks.FACTORY_GAUGE.has(state);
        ExtraGauges.CONSTANTS.getLogger().info("notifyPanels: behaviour is null at " + panelPos.pos() + ", blockState=" + state.getBlock() + ", isGauge=" + isGauge);
        if (isGauge) {
            return;
        }
        ExtraGauges.CONSTANTS.getLogger().warn("notifyPanels: REMOVING link to " + panelPos.pos() + " because blockState=" + state.getBlock() + " is not a Factory Gauge!");
        iterator.remove();
    }

    @Redirect(
        method = "shouldBePoweredTristate",
        at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V")
    )
    private void redirectRemoveTristate(Iterator<FactoryPanelPosition> iterator, @Local FactoryPanelPosition panelPos) {
        BlockState state = ((FactoryPanelSupportBehaviour) (Object) this).getWorld().getBlockState(panelPos.pos());
        boolean isGauge = AllBlocks.FACTORY_GAUGE.has(state);
        ExtraGauges.CONSTANTS.getLogger().info("shouldBePoweredTristate: behaviour is null at " + panelPos.pos() + ", blockState=" + state.getBlock() + ", isGauge=" + isGauge);
        if (isGauge) {
            return;
        }
        ExtraGauges.CONSTANTS.getLogger().warn("shouldBePoweredTristate: REMOVING link to " + panelPos.pos() + " because blockState=" + state.getBlock() + " is not a Factory Gauge!");
        iterator.remove();
    }

    @Inject(method = "write", at = @At("HEAD"))
    private void onWrite(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        var support = (FactoryPanelSupportBehaviour) (Object) this;
        if (support.blockEntity != null) {
            RelativeNBTUtils.writeLinkedPanelsRelative(nbt, registries, support.blockEntity.getBlockPos(), linkedPanels);
            ExtraGauges.CONSTANTS.getLogger().info("FactoryPanelSupport write (relative): parent=" + support.blockEntity.getBlockPos() + ", class=" + support.blockEntity.getClass().getSimpleName() + ", gauges=" + linkedPanels);
        }
    }

    @Inject(method = "read", at = @At("HEAD"), cancellable = true)
    private void onRead(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        var support = (FactoryPanelSupportBehaviour) (Object) this;
        if (support.blockEntity != null) {
            if (RelativeNBTUtils.readLinkedPanelsRelative(nbt, registries, support.blockEntity.getBlockPos(), linkedPanels)) {
                ExtraGauges.CONSTANTS.getLogger().info("FactoryPanelSupport read (relative): parent=" + support.blockEntity.getBlockPos() + ", class=" + support.blockEntity.getClass().getSimpleName() + ", gauges=" + linkedPanels);
                ci.cancel();
            }
        }
    }
}
