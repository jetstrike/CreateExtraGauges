package net.liukrast.eg.content.logistics.link;

import com.mojang.serialization.Codec;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.createmod.catnip.codecs.CatnipCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.*;
import java.util.stream.Collectors;

public class RelativeNBTUtils {

    public static void writeLinkedPanelsRelative(CompoundTag nbt, HolderLookup.Provider registries, BlockPos origin, List<FactoryPanelPosition> linkedPanels) {
        List<FactoryPanelPosition> relative = linkedPanels.stream()
                .map(pos -> new FactoryPanelPosition(pos.pos().subtract(origin), pos.slot()))
                .collect(Collectors.toList());
        nbt.put("LinkedGaugesRelative", CatnipCodecUtils.encode(Codec.list(FactoryPanelPosition.CODEC), registries, relative).orElseThrow());
    }

    public static boolean readLinkedPanelsRelative(CompoundTag nbt, HolderLookup.Provider registries, BlockPos origin, List<FactoryPanelPosition> linkedPanels) {
        if (!nbt.contains("LinkedGaugesRelative")) return false;
        linkedPanels.clear();
        CatnipCodecUtils.decode(Codec.list(FactoryPanelPosition.CODEC), registries, nbt.get("LinkedGaugesRelative"))
                .ifPresent(list -> list.forEach(pos -> linkedPanels.add(new FactoryPanelPosition(origin.offset(pos.pos()), pos.slot()))));
        return true;
    }

    public static void writeTargetingRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Set<FactoryPanelPosition> targeting) {
        Set<FactoryPanelPosition> relative = targeting.stream()
                .map(pos -> new FactoryPanelPosition(pos.pos().subtract(origin), pos.slot()))
                .collect(Collectors.toSet());
        panelTag.put("TargetingRelative", CatnipCodecUtils.encode(CatnipCodecs.set(FactoryPanelPosition.CODEC), registries, relative).orElseThrow());
    }

    public static boolean readTargetingRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Set<FactoryPanelPosition> targeting) {
        if (!panelTag.contains("TargetingRelative")) return false;
        targeting.clear();
        CatnipCodecUtils.decode(CatnipCodecs.set(FactoryPanelPosition.CODEC), registries, panelTag.get("TargetingRelative"))
                .ifPresent(set -> set.forEach(pos -> targeting.add(new FactoryPanelPosition(origin.offset(pos.pos()), pos.slot()))));
        return true;
    }

    public static void writeTargetedByRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Collection<FactoryPanelConnection> targetedBy) {
        List<FactoryPanelConnection> relative = targetedBy.stream()
                .map(conn -> {
                    var relPos = new FactoryPanelPosition(conn.from.pos().subtract(origin), conn.from.slot());
                    return new FactoryPanelConnection(relPos, conn.amount, conn.arrowBendMode);
                })
                .collect(Collectors.toList());
        panelTag.put("TargetedByRelative", CatnipCodecUtils.encode(Codec.list(FactoryPanelConnection.CODEC), registries, relative).orElseThrow());
    }

    public static boolean readTargetedByRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Map<FactoryPanelPosition, FactoryPanelConnection> targetedBy) {
        if (!panelTag.contains("TargetedByRelative")) return false;
        targetedBy.clear();
        CatnipCodecUtils.decode(Codec.list(FactoryPanelConnection.CODEC), registries, panelTag.get("TargetedByRelative"))
                .ifPresent(list -> list.forEach(conn -> {
                    var absPos = new FactoryPanelPosition(origin.offset(conn.from.pos()), conn.from.slot());
                    var absConn = new FactoryPanelConnection(absPos, conn.amount, conn.arrowBendMode);
                    targetedBy.put(absPos, absConn);
                }));
        return true;
    }

    public static void writeTargetedByLinksRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Collection<FactoryPanelConnection> targetedByLinks) {
        List<FactoryPanelConnection> relative = targetedByLinks.stream()
                .map(conn -> {
                    var relPos = new FactoryPanelPosition(conn.from.pos().subtract(origin), conn.from.slot());
                    return new FactoryPanelConnection(relPos, conn.amount, conn.arrowBendMode);
                })
                .collect(Collectors.toList());
        panelTag.put("TargetedByLinksRelative", CatnipCodecUtils.encode(Codec.list(FactoryPanelConnection.CODEC), registries, relative).orElseThrow());
    }

    public static boolean readTargetedByLinksRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Map<BlockPos, FactoryPanelConnection> targetedByLinks) {
        if (!panelTag.contains("TargetedByLinksRelative")) return false;
        targetedByLinks.clear();
        CatnipCodecUtils.decode(Codec.list(FactoryPanelConnection.CODEC), registries, panelTag.get("TargetedByLinksRelative"))
                .ifPresent(list -> list.forEach(conn -> {
                    var absPos = new FactoryPanelPosition(origin.offset(conn.from.pos()), conn.from.slot());
                    var absConn = new FactoryPanelConnection(absPos, conn.amount, conn.arrowBendMode);
                    targetedByLinks.put(absPos.pos(), absConn);
                }));
        return true;
    }

    public static void writeTargetedByExtraRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Collection<FactoryPanelConnection> targetedByExtra) {
        List<FactoryPanelConnection> relative = targetedByExtra.stream()
                .map(conn -> {
                    var relPos = new FactoryPanelPosition(conn.from.pos().subtract(origin), conn.from.slot());
                    return new FactoryPanelConnection(relPos, conn.amount, conn.arrowBendMode);
                })
                .collect(Collectors.toList());
        panelTag.put("TargetedByExtraRelative", CatnipCodecUtils.encode(Codec.list(FactoryPanelConnection.CODEC), registries, relative).orElseThrow());
    }

    public static boolean readTargetedByExtraRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Map<BlockPos, FactoryPanelConnection> targetedByExtra) {
        if (!panelTag.contains("TargetedByExtraRelative")) return false;
        targetedByExtra.clear();
        CatnipCodecUtils.decode(Codec.list(FactoryPanelConnection.CODEC), registries, panelTag.get("TargetedByExtraRelative"))
                .ifPresent(list -> list.forEach(conn -> {
                    var absPos = new FactoryPanelPosition(origin.offset(conn.from.pos()), conn.from.slot());
                    var absConn = new FactoryPanelConnection(absPos, conn.amount, conn.arrowBendMode);
                    targetedByExtra.put(absPos.pos(), absConn);
                }));
        return true;
    }
}
