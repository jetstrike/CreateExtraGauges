package net.liukrast.eg.mixin;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import net.liukrast.eg.ExtraGauges;
import net.liukrast.eg.content.logistics.link.DisplayCollectorBlockEntity;
import net.liukrast.eg.mixinExtension.DCFinder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(NavTableBlockEntity.class)
public abstract class NavTableBlockEntityMixin extends BlockEntity implements DCFinder {

    @Unique private final Set<BlockPos> extra_gauges$targetingDisplayCollectors = new HashSet<>();

    public NavTableBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public Set<BlockPos> extra_gauges$targetingDisplayCollectors() {
        return extra_gauges$targetingDisplayCollectors;
    }

    @Inject(method = "write", at = @At("HEAD"))
    private void writeSafe(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        var level = getLevel();
        if (level != null) {
            extra_gauges$targetingDisplayCollectors.removeIf(pos -> level.isLoaded(pos) && !(level.getBlockEntity(pos) instanceof DisplayCollectorBlockEntity));
        }
        ListTag list = new ListTag();
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        for(BlockPos pos : extra_gauges$targetingDisplayCollectors) {
            BlockPos.CODEC.encodeStart(ops, pos)
                    .resultOrPartial(ExtraGauges.CONSTANTS.getLogger()::error)
                    .ifPresent(list::add);
        }
        tag.put("extra_gauges$targetingDisplayCollectors", list);
    }

    @Inject(method = "read", at = @At("HEAD"))
    private void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (tag.get("extra_gauges$targetingDisplayCollectors") instanceof ListTag list) {
            extra_gauges$targetingDisplayCollectors.clear();
            var ops = registries.createSerializationContext(NbtOps.INSTANCE);
            for(Tag tag1 : list) {
                BlockPos.CODEC
                        .parse(ops, tag1)
                        .resultOrPartial(ExtraGauges.CONSTANTS.getLogger()::error)
                        .ifPresent(extra_gauges$targetingDisplayCollectors::add);
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (this.level == null || this.level.isClientSide) return;
        DisplayLinkBlock.notifyGatherers(this.level, this.worldPosition);
    }
}
