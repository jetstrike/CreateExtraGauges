package net.liukrast.eg.mixin;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSupportBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;

@Mixin(value = FactoryPanelSupportBehaviour.class, remap = false)
public abstract class FactoryPanelSupportBehaviourMixin extends BlockEntityBehaviour {

    @Shadow
    private List<FactoryPanelPosition> linkedPanels;

    @Shadow
    private boolean changed;

    public FactoryPanelSupportBehaviourMixin() {
        super(null);
    }

    /**
     * @author Antigravity
     * @reason Prevent connection deletion during chunk loading / async world initialization.
     */
    @Overwrite
    public void notifyPanels() {
        if (getWorld().isClientSide())
            return;
        for (Iterator<FactoryPanelPosition> iterator = linkedPanels.iterator(); iterator.hasNext(); ) {
            FactoryPanelPosition panelPos = iterator.next();
            if (!getWorld().isLoaded(panelPos.pos()))
                continue;
            FactoryPanelBehaviour behaviour = FactoryPanelBehaviour.at(getWorld(), panelPos);
            if (behaviour == null) {
                BlockState state = getWorld().getBlockState(panelPos.pos());
                if (AllBlocks.FACTORY_GAUGE.has(state)) {
                    continue;
                }
                iterator.remove();
                changed = true;
                continue;
            }
            behaviour.checkForRedstoneInput();
        }
    }

    /**
     * @author Antigravity
     * @reason Prevent connection deletion during chunk loading / async world initialization.
     */
    @Overwrite
    @Nullable
    public Boolean shouldBePoweredTristate() {
        for (Iterator<FactoryPanelPosition> iterator = linkedPanels.iterator(); iterator.hasNext(); ) {
            FactoryPanelPosition panelPos = iterator.next();
            if (!getWorld().isLoaded(panelPos.pos()))
                return null;
            FactoryPanelBehaviour behaviour = FactoryPanelBehaviour.at(getWorld(), panelPos);
            if (behaviour == null) {
                BlockState state = getWorld().getBlockState(panelPos.pos());
                if (AllBlocks.FACTORY_GAUGE.has(state)) {
                    continue;
                }
                iterator.remove();
                changed = true;
                continue;
            }
            if (behaviour.isActive() && behaviour.satisfied && behaviour.count != 0)
                return true;
        }
        return false;
    }
}
