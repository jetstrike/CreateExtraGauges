package net.liukrast.eg.mixin;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import net.liukrast.deployer.lib.logistics.board.AbstractPanelBehaviour;
import net.liukrast.deployer.lib.logistics.board.connection.AbstractPanelSupportBehaviour;
import net.liukrast.deployer.lib.logistics.board.connection.PanelConnection;
import net.liukrast.deployer.lib.logistics.board.connection.ProvidesConnection;
import net.liukrast.deployer.lib.mixin.FactoryPanelSupportAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;

@Mixin(value = AbstractPanelSupportBehaviour.class, remap = false)
public abstract class AbstractPanelSupportBehaviourMixin extends AbstractPanelSupportBehaviour {

    public AbstractPanelSupportBehaviourMixin() {
        super(null, null, null);
    }

    /**
     * @author Antigravity
     * @reason Prevent connection deletion during chunk loading / async world initialization.
     */
    @Overwrite
    public @Nullable <T> List<AbstractPanelBehaviour.ConnectionValue<T>> getAllValuesWithSource(PanelConnection<T> connection) {
        List<AbstractPanelBehaviour.ConnectionValue<T>> out = new ArrayList<>();
        for (Iterator<FactoryPanelPosition> iterator = getLinkedPanels().iterator(); iterator.hasNext(); ) {
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
                ((FactoryPanelSupportAccessor)this).deployer$setChanged(true);
                continue;
            }
            if (!behaviour.isActive())
                continue;
            var conn = behaviour.targetedByLinks.get(getPos());
            if(conn == null) continue;
            var pc = ProvidesConnection.getCurrentConnection(conn, () -> ProvidesConnection.getPossibleConnections(behaviour, this).stream().findFirst().orElse(null));
            if(pc == null || pc != connection) continue;
            var opt = ((ProvidesConnection)behaviour).getConnectionValue(connection);
            opt.ifPresent(t -> out.add(new AbstractPanelBehaviour.ConnectionValue<>(conn, t)));
        }
        return out;
    }
}
