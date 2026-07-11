package net.liukrast.eg.mixin;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSupportBehaviour;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;

@Mixin(value = FactoryPanelSupportBehaviour.class, remap = false)
public abstract class FactoryPanelSupportBehaviourMixin {

    @Redirect(
        method = "notifyPanels",
        at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V")
    )
    private void redirectRemoveNotify(Iterator<FactoryPanelPosition> iterator, @Local FactoryPanelPosition panelPos) {
        BlockState state = ((FactoryPanelSupportBehaviour) (Object) this).getWorld().getBlockState(panelPos.pos());
        if (AllBlocks.FACTORY_GAUGE.has(state)) {
            return;
        }
        iterator.remove();
    }

    @Redirect(
        method = "shouldBePoweredTristate",
        at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V")
    )
    private void redirectRemoveTristate(Iterator<FactoryPanelPosition> iterator, @Local FactoryPanelPosition panelPos) {
        BlockState state = ((FactoryPanelSupportBehaviour) (Object) this).getWorld().getBlockState(panelPos.pos());
        if (AllBlocks.FACTORY_GAUGE.has(state)) {
            return;
        }
        iterator.remove();
    }
}
