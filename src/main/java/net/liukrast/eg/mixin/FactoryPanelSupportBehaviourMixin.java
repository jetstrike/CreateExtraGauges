package net.liukrast.eg.mixin;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSupportBehaviour;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.liukrast.eg.ExtraGauges;

import java.util.Iterator;

@Mixin(value = FactoryPanelSupportBehaviour.class, remap = false)
public abstract class FactoryPanelSupportBehaviourMixin {

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
}
