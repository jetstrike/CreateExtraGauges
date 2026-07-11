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
import net.liukrast.eg.ExtraGauges;

import java.util.*;
import java.util.stream.Collectors;

public class RelativeNBTUtils {

    public static void writeLinkedPanelsRelative(CompoundTag nbt, HolderLookup.Provider registries, BlockPos origin, List<FactoryPanelPosition> linkedPanels) {
        List<FactoryPanelPosition> relative = linkedPanels.stream()
                .map(pos -> new FactoryPanelPosition(pos.pos().subtract(origin), pos.slot()))
                .collect(Collectors.toList());
        var tag = CatnipCodecUtils.encode(Codec.list(FactoryPanelPosition.CODEC), registries, relative).orElseThrow();
        nbt.put("LinkedGaugesRelative", tag);
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils writeLinkedPanelsRelative: origin=" + origin + ", size=" + linkedPanels.size() + ", relative=" + relative);
    }

    public static boolean readLinkedPanelsRelative(CompoundTag nbt, HolderLookup.Provider registries, BlockPos origin, List<FactoryPanelPosition> linkedPanels) {
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils readLinkedPanelsRelative: origin=" + origin + ", keys=" + nbt.getAllKeys());
        if (!nbt.contains("LinkedGaugesRelative")) {
            ExtraGauges.CONSTANTS.getLogger().warn("RelativeNBTUtils readLinkedPanelsRelative: LinkedGaugesRelative NOT found!");
            return false;
        }
        linkedPanels.clear();
        CatnipCodecUtils.decode(Codec.list(FactoryPanelPosition.CODEC), registries, nbt.get("LinkedGaugesRelative"))
                .ifPresent(list -> list.forEach(pos -> linkedPanels.add(new FactoryPanelPosition(origin.offset(pos.pos()), pos.slot()))));
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils readLinkedPanelsRelative: loaded size=" + linkedPanels.size() + ", values=" + linkedPanels);
        return true;
    }

    public static void writeTargetingRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Set<FactoryPanelPosition> targeting) {
        Set<FactoryPanelPosition> relative = targeting.stream()
                .map(pos -> new FactoryPanelPosition(pos.pos().subtract(origin), pos.slot()))
                .collect(Collectors.toSet());
        var tag = CatnipCodecUtils.encode(CatnipCodecs.set(FactoryPanelPosition.CODEC), registries, relative).orElseThrow();
        panelTag.put("TargetingRelative", tag);
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils writeTargetingRelative: origin=" + origin + ", size=" + targeting.size() + ", relative=" + relative);
    }

    public static boolean readTargetingRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Set<FactoryPanelPosition> targeting) {
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils readTargetingRelative: origin=" + origin + ", keys=" + panelTag.getAllKeys());
        if (!panelTag.contains("TargetingRelative")) {
            ExtraGauges.CONSTANTS.getLogger().warn("RelativeNBTUtils readTargetingRelative: TargetingRelative NOT found!");
            return false;
        }
        targeting.clear();
        CatnipCodecUtils.decode(CatnipCodecs.set(FactoryPanelPosition.CODEC), registries, panelTag.get("TargetingRelative"))
                .ifPresent(set -> set.forEach(pos -> targeting.add(new FactoryPanelPosition(origin.offset(pos.pos()), pos.slot()))));
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils readTargetingRelative: loaded size=" + targeting.size() + ", values=" + targeting);
        return true;
    }

    public static void writeTargetedByRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Collection<FactoryPanelConnection> targetedBy) {
        List<FactoryPanelConnection> relative = targetedBy.stream()
                .map(conn -> {
                    var relPos = new FactoryPanelPosition(conn.from.pos().subtract(origin), conn.from.slot());
                    return new FactoryPanelConnection(relPos, conn.amount, conn.arrowBendMode);
                })
                .collect(Collectors.toList());
        var tag = CatnipCodecUtils.encode(Codec.list(FactoryPanelConnection.CODEC), registries, relative).orElseThrow();
        panelTag.put("TargetedByRelative", tag);
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils writeTargetedByRelative: origin=" + origin + ", size=" + targetedBy.size() + ", relative=" + relative);
    }

    public static boolean readTargetedByRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Map<FactoryPanelPosition, FactoryPanelConnection> targetedBy) {
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils readTargetedByRelative: origin=" + origin);
        if (!panelTag.contains("TargetedByRelative")) {
            ExtraGauges.CONSTANTS.getLogger().warn("RelativeNBTUtils readTargetedByRelative: TargetedByRelative NOT found!");
            return false;
        }
        targetedBy.clear();
        CatnipCodecUtils.decode(Codec.list(FactoryPanelConnection.CODEC), registries, panelTag.get("TargetedByRelative"))
                .ifPresent(list -> list.forEach(conn -> {
                    var absPos = new FactoryPanelPosition(origin.offset(conn.from.pos()), conn.from.slot());
                    var absConn = new FactoryPanelConnection(absPos, conn.amount, conn.arrowBendMode);
                    targetedBy.put(absPos, absConn);
                }));
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils readTargetedByRelative: loaded size=" + targetedBy.size() + ", values=" + targetedBy.values());
        return true;
    }

    public static void writeTargetedByLinksRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Collection<FactoryPanelConnection> targetedByLinks) {
        List<FactoryPanelConnection> relative = targetedByLinks.stream()
                .map(conn -> {
                    var relPos = new FactoryPanelPosition(conn.from.pos().subtract(origin), conn.from.slot());
                    return new FactoryPanelConnection(relPos, conn.amount, conn.arrowBendMode);
                })
                .collect(Collectors.toList());
        var tag = CatnipCodecUtils.encode(Codec.list(FactoryPanelConnection.CODEC), registries, relative).orElseThrow();
        panelTag.put("TargetedByLinksRelative", tag);
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils writeTargetedByLinksRelative: origin=" + origin + ", size=" + targetedByLinks.size() + ", relative=" + relative);
    }

    public static boolean readTargetedByLinksRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Map<BlockPos, FactoryPanelConnection> targetedByLinks) {
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils readTargetedByLinksRelative: origin=" + origin);
        if (!panelTag.contains("TargetedByLinksRelative")) {
            ExtraGauges.CONSTANTS.getLogger().warn("RelativeNBTUtils readTargetedByLinksRelative: TargetedByLinksRelative NOT found!");
            return false;
        }
        targetedByLinks.clear();
        CatnipCodecUtils.decode(Codec.list(FactoryPanelConnection.CODEC), registries, panelTag.get("TargetedByLinksRelative"))
                .ifPresent(list -> list.forEach(conn -> {
                    var absPos = new FactoryPanelPosition(origin.offset(conn.from.pos()), conn.from.slot());
                    var absConn = new FactoryPanelConnection(absPos, conn.amount, conn.arrowBendMode);
                    targetedByLinks.put(absPos.pos(), absConn);
                }));
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils readTargetedByLinksRelative: loaded size=" + targetedByLinks.size() + ", values=" + targetedByLinks.values());
        return true;
    }

    public static void writeTargetedByExtraRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Collection<FactoryPanelConnection> targetedByExtra) {
        List<FactoryPanelConnection> relative = targetedByExtra.stream()
                .map(conn -> {
                    var relPos = new FactoryPanelPosition(conn.from.pos().subtract(origin), conn.from.slot());
                    return new FactoryPanelConnection(relPos, conn.amount, conn.arrowBendMode);
                })
                .collect(Collectors.toList());
        var tag = CatnipCodecUtils.encode(Codec.list(FactoryPanelConnection.CODEC), registries, relative).orElseThrow();
        panelTag.put("TargetedByExtraRelative", tag);
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils writeTargetedByExtraRelative: origin=" + origin + ", size=" + targetedByExtra.size() + ", relative=" + relative);
    }

    public static boolean readTargetedByExtraRelative(CompoundTag panelTag, HolderLookup.Provider registries, BlockPos origin, Map<BlockPos, FactoryPanelConnection> targetedByExtra) {
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils readTargetedByExtraRelative: origin=" + origin);
        if (!panelTag.contains("TargetedByExtraRelative")) {
            ExtraGauges.CONSTANTS.getLogger().warn("RelativeNBTUtils readTargetedByExtraRelative: TargetedByExtraRelative NOT found!");
            return false;
        }
        targetedByExtra.clear();
        CatnipCodecUtils.decode(Codec.list(FactoryPanelConnection.CODEC), registries, panelTag.get("TargetedByExtraRelative"))
                .ifPresent(list -> list.forEach(conn -> {
                    var absPos = new FactoryPanelPosition(origin.offset(conn.from.pos()), conn.from.slot());
                    var absConn = new FactoryPanelConnection(absPos, conn.amount, conn.arrowBendMode);
                    targetedByExtra.put(absPos.pos(), absConn);
                }));
        ExtraGauges.CONSTANTS.getLogger().info("RelativeNBTUtils readTargetedByExtraRelative: loaded size=" + targetedByExtra.size() + ", values=" + targetedByExtra.values());
        return true;
    }
}
