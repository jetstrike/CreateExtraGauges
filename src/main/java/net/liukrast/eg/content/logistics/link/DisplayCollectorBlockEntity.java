package net.liukrast.eg.content.logistics.link;

import com.mojang.serialization.DynamicOps;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.liukrast.deployer.lib.logistics.board.connection.AbstractPanelSupportBehaviour;
import net.liukrast.deployer.lib.logistics.board.connection.PanelConnectionBuilder;
import net.liukrast.deployer.lib.registry.DeployerPanelConnections;
import net.liukrast.eg.ExtraGauges;
import net.liukrast.eg.mixinExtension.DCFinder;
import net.liukrast.eg.registry.EGBlockEntityTypes;
import net.liukrast.eg.registry.EGBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;

import java.util.List;

public class DisplayCollectorBlockEntity extends DisplayLinkBlockEntity {
    private Component component;
    private BlockPos registeredSource;
    public DisplayCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(EGBlockEntityTypes.DISPLAY_COLLECTOR.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(computerBehaviour = com.simibubi.create.compat.computercraft.ComputerCraftProxy.behaviour(this));
        behaviours.add(factoryPanelSupport = new AbstractPanelSupportBehaviour(this, () -> true, () -> {}) {
            @Override
            public void addConnections(PanelConnectionBuilder builder) {
                builder.registerOutput(DeployerPanelConnections.STRING, () -> component == null ? null : component.getString());
            }
        });
        registerAwardables(behaviours, com.simibubi.create.foundation.advancement.AllAdvancements.DISPLAY_LINK, com.simibubi.create.foundation.advancement.AllAdvancements.DISPLAY_BOARD);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
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
        sendData();
    }

    @Override
    public BlockPos getSourcePosition() {
        return targetOffset == null ? null : worldPosition.offset(targetOffset);
    }

    @Override
    public BlockPos getTargetPosition() {
        for (FactoryPanelPosition position : factoryPanelSupport.getLinkedPanels())
            return position.pos();
        if (targetOffset != null && !targetOffset.equals(BlockPos.ZERO)) {
            return worldPosition.offset(targetOffset);
        }
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

    @Override
    public void sendData() {
        if (this.level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 4 | 16);
        } else {
            super.sendData();
        }
    }

    @Override
    public void tick() {
        try {
            super.tick();
            if (level == null || level.isClientSide) return;

            refreshTicks++;
            if (refreshTicks % 20 == 0) {
                ExtraGauges.CONSTANTS.getLogger().info("DC tick: pos=" + worldPosition + ", refreshTicks=" + refreshTicks + ", removed=" + isRemoved() + ", targetOffset=" + targetOffset);
            }
            if (refreshTicks >= 10) {
                refreshTicks = 0;
                updateGatheredData();
            }
        } catch (Throwable t) {
            ExtraGauges.CONSTANTS.getLogger().error("Error in DC tick", t);
        }
    }

    @Override
    public void updateGatheredData() {
        try {
            if (level == null || level.isClientSide) return;

            BlockPos sourcePosition = getSourcePosition();
            BlockPos targetPosition = getTargetPosition();

            ExtraGauges.CONSTANTS.getLogger().info("DC update: source=" + sourcePosition + ", target=" + targetPosition + ", level=" + level.dimension().location() + ", panels=" + (factoryPanelSupport == null ? "null" : factoryPanelSupport.getLinkedPanels().size()));

            if (sourcePosition == null || targetPosition == null) {
                ExtraGauges.CONSTANTS.getLogger().warn("DC update aborted: sourcePosition or targetPosition is null");
                return;
            }

            if (!level.isLoaded(targetPosition) || !level.isLoaded(sourcePosition)) {
                ExtraGauges.CONSTANTS.getLogger().warn("DC update aborted: targetLoaded=" + level.isLoaded(targetPosition) + ", sourceLoaded=" + level.isLoaded(sourcePosition));
                return;
            }

            DisplayTarget target = DisplayTarget.get(level, targetPosition);
            if (target == null) {
                ExtraGauges.CONSTANTS.getLogger().warn("DC update aborted: target is null");
                return;
            }

            if (activeTarget != target) {
                activeTarget = target;
                notifyUpdate();
            }

            var sources = DisplaySource.getAll(level, sourcePosition);
            if (sources.isEmpty()) {
                ExtraGauges.CONSTANTS.getLogger().warn("DC update aborted: sources is empty");
                return;
            }

            var sourceObj = sources.get(0);
            if (activeSource != sourceObj) {
                activeSource = sourceObj;
                notifyUpdate();
            }

            if (activeSource == null || activeTarget == null) {
                ExtraGauges.CONSTANTS.getLogger().warn("DC update aborted: activeSource=" + activeSource + ", activeTarget=" + activeTarget);
                return;
            }

            var sourceBE = level.getBlockEntity(sourcePosition);
            if (sourceBE instanceof NavTableBlockEntity navBE) {
                navBE.subLevel = (SubLevel) Sable.HELPER.getContaining(level, sourcePosition);
            }

            ExtraGauges.CONSTANTS.getLogger().info("DC update transferring text... activeSource=" + activeSource.getClass().getSimpleName() + ", activeTarget=" + activeTarget.getClass().getSimpleName());

            DisplayLinkContext context = new DisplayLinkContext(level, this);
            activeSource.transferData(context, activeTarget, targetLine);
            sendPulseNextSync();
            sendData();
        } catch (Throwable t) {
            ExtraGauges.CONSTANTS.getLogger().error("Error in DC updateGatheredData", t);
        }
    }

    private void registerAtSource() {
        if(level == null) return;

        BlockPos source = getSourcePosition();
        if (source == null) return;
        
        if (!source.equals(registeredSource)) {
            if (registeredSource != null) {
                DisplayCollectorIndex.remove(level, registeredSource, worldPosition);
            }
            DisplayCollectorIndex.add(level, source, worldPosition);
            registeredSource = source;
        }

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
                    var sources = DisplaySource.getAll(targetLevel, source);
                    if (!sources.isEmpty()) {
                        activeSource = sources.get(0);
                        updateGatheredData();
                    }
                }
            }
        }
    }
}
