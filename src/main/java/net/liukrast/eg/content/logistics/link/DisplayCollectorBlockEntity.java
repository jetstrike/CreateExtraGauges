package net.liukrast.eg.content.logistics.link;

import com.mojang.serialization.DynamicOps;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.liukrast.deployer.lib.logistics.board.connection.AbstractPanelSupportBehaviour;
import net.liukrast.deployer.lib.logistics.board.connection.PanelConnectionBuilder;
import net.liukrast.deployer.lib.registry.DeployerPanelConnections;
import net.liukrast.eg.ExtraGauges;
import net.liukrast.eg.mixinExtension.DCFinder;
import net.liukrast.eg.registry.EGBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.Level;
import java.util.List;

public class DisplayCollectorBlockEntity extends DisplayLinkBlockEntity {
    private Component component;
    private BlockPos registeredSource;
    public DisplayCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(EGBlockEntityTypes.DISPLAY_COLLECTOR.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(factoryPanelSupport = new AbstractPanelSupportBehaviour(this, () -> true, () -> {}) {
            @Override
            public void addConnections(PanelConnectionBuilder builder) {
                builder.registerOutput(DeployerPanelConnections.STRING, () -> component == null ? null : component.getString());
            }
        });
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        // At placement, ClickToLinkBlockItem applies "TargetOffset" through block
        // entity NBT after onLoad has already run, so re-register here as well
        if(level != null && !isRemoved()) registerAtSource();
        if(!tag.contains("text")) return;
        DynamicOps<Tag> dynamicops = registries.createSerializationContext(NbtOps.INSTANCE);
        ComponentSerialization.FLAT_CODEC
                .parse(dynamicops, tag.get("text"))
                .resultOrPartial(ExtraGauges.CONSTANTS.getLogger()::error)
                .ifPresent(text -> component = text);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if(component != null) {
            DynamicOps<Tag> dynamicops = registries.createSerializationContext(NbtOps.INSTANCE);
            ComponentSerialization.FLAT_CODEC
                    .encodeStart(dynamicops, component)
                    .resultOrPartial(ExtraGauges.CONSTANTS.getLogger()::error)
                    .ifPresent(tag1 -> tag.put("text", tag1));
        }
    }

    public Component getComponent() {
        return component == null ? Component.empty() : component;
    }

    public void setComponent(Component component) {
        this.component = component;
        factoryPanelSupport.notifyPanels();
    }

    @Override
    public BlockPos getSourcePosition() {
        return worldPosition.offset(targetOffset);
    }

    @Override
    public BlockPos getTargetPosition() {
        for (FactoryPanelPosition position : factoryPanelSupport.getLinkedPanels())
            return position.pos();
        return worldPosition.relative(getDirection());
    }

    @Override
    public void target(BlockPos targetPosition) {
        super.target(targetPosition);
        if(level != null && !isRemoved()) registerAtSource();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        registerAtSource();
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        super.transform(be, transform);
        registerAtSource();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (registeredSource != null) {
            var server = level == null ? null : level.getServer();
            if (server != null) {
                for (var serverLevel : server.getAllLevels()) {
                    DisplayCollectorIndex.remove(serverLevel, registeredSource, worldPosition);
                }
            } else if (level != null) {
                DisplayCollectorIndex.remove(level, registeredSource, worldPosition);
            }
            registeredSource = null;
        }
    }

    private void registerAtSource() {
        if(level == null) return;

        BlockPos source = getSourcePosition();
        var server = level.getServer();
        if (server != null) {
            Level targetLevel = null;
            DCFinder targetFinder = null;
            for (var serverLevel : server.getAllLevels()) {
                var beAtPos = serverLevel.getBlockEntity(source);
                if (beAtPos instanceof DCFinder finder) {
                    targetFinder = finder;
                    targetLevel = serverLevel;
                    break;
                }
            }

            if (targetFinder != null) {
                var set = targetFinder.extra_gauges$targetingDisplayCollectors();
                if (!set.contains(getBlockPos())) {
                    set.add(getBlockPos());
                    ((BlockEntity) targetFinder).setChanged();
                }

                if (activeSource == null) {
                    var sources = com.simibubi.create.api.behaviour.display.DisplaySource.getAll(targetLevel, source);
                    if (!sources.isEmpty()) {
                        activeSource = sources.get(0);
                        updateGatheredData();
                    }
                }

                if (!source.equals(registeredSource)) {
                    if (registeredSource != null) {
                        DisplayCollectorIndex.remove(targetLevel, registeredSource, worldPosition);
                    }
                    DisplayCollectorIndex.add(targetLevel, source, worldPosition);
                    registeredSource = source;
                }
            } else {
                if (!source.equals(registeredSource)) {
                    if (registeredSource != null) {
                        DisplayCollectorIndex.remove(level, registeredSource, worldPosition);
                    }
                    DisplayCollectorIndex.add(level, source, worldPosition);
                    registeredSource = source;
                }
            }
        } else {
            if (!source.equals(registeredSource)) {
                if (registeredSource != null) {
                    DisplayCollectorIndex.remove(level, registeredSource, worldPosition);
                }
                DisplayCollectorIndex.add(level, source, worldPosition);
                registeredSource = source;
            }
        }
    }
}
