package net.liukrast.eg.mixin;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import net.liukrast.deployer.lib.logistics.board.connection.AbstractPanelSupportBehaviour;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.liukrast.eg.ExtraGauges;

import java.util.Iterator;

@Mixin(value = AbstractPanelSupportBehaviour.class, remap = false)
public abstract class AbstractPanelSupportBehaviourMixin {

    @Redirect(
        method = "getAllValuesWithSource",
        at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V")
    )
    private void redirectRemoveAllValues(Iterator<FactoryPanelPosition> iterator, @Local FactoryPanelPosition panelPos) {
        BlockState state = ((AbstractPanelSupportBehaviour) (Object) this).getWorld().getBlockState(panelPos.pos());
        boolean isGauge = AllBlocks.FACTORY_GAUGE.has(state);
        ExtraGauges.CONSTANTS.getLogger().info("getAllValuesWithSource: behaviour is null at " + panelPos.pos() + ", blockState=" + state.getBlock() + ", isGauge=" + isGauge);
        if (isGauge) {
            return;
        }
        ExtraGauges.CONSTANTS.getLogger().warn("getAllValuesWithSource: REMOVING link to " + panelPos.pos() + " because blockState=" + state.getBlock() + " is not a Factory Gauge!");
        iterator.remove();
    }
}
