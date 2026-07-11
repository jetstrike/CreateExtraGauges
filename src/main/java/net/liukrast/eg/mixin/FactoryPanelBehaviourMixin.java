package net.liukrast.eg.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.foundation.utility.CreateLang;
import net.liukrast.eg.mixinExtension.WidthModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.liukrast.eg.content.logistics.link.RelativeNBTUtils;
import net.liukrast.deployer.lib.mixinExtensions.FPBExtension;

import java.util.Map;
import java.util.Set;

@Mixin(FactoryPanelBehaviour.class)
public abstract class FactoryPanelBehaviourMixin implements WidthModifier {
    /* UNIQUE VALUES */
    @Unique private int extra_gauges$width = 3;
    /* IMPL METHODS */
    @Override public int extra_gauges$getWidth() {return extra_gauges$width;}
    @Override public void extra_gauges$setWidth(int width) {extra_gauges$width = width;}

    @Shadow public FactoryPanelBlock.PanelSlot slot;
    @Shadow public boolean active;
    @Shadow public Map<FactoryPanelPosition, FactoryPanelConnection> targetedBy;
    @Shadow public Map<BlockPos, FactoryPanelConnection> targetedByLinks;
    @Shadow public Set<FactoryPanelPosition> targeting;

    /* DATA */
    @Inject(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putUUID(Ljava/lang/String;Ljava/util/UUID;)V"))
    private void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci, @Local(ordinal = 1) CompoundTag panelTag) {
        if(extra_gauges$width != 3) panelTag.putInt("extra_gauges$CraftWidth", extra_gauges$width);
    }

    @Inject(method = "read", at = @At(value = "INVOKE", target = "Ljava/util/Map;clear()V"))
    private void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci, @Local(ordinal = 1) CompoundTag panelTag) {
        extra_gauges$width = panelTag.contains("extra_gauges$CraftWidth") ? panelTag.getInt("extra_gauges$CraftWidth") : 3;
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void onWrite(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (!active) return;
        CompoundTag panelTag = nbt.getCompound(CreateLang.asId(slot.name()));
        if (!panelTag.isEmpty()) {
            FactoryPanelBehaviour behavior = (FactoryPanelBehaviour) (Object) this;
            RelativeNBTUtils.writeTargetingRelative(panelTag, registries, behavior.getPos(), targeting);
            RelativeNBTUtils.writeTargetedByRelative(panelTag, registries, behavior.getPos(), targetedBy.values());
            RelativeNBTUtils.writeTargetedByLinksRelative(panelTag, registries, behavior.getPos(), targetedByLinks.values());
        }
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void onRead(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        CompoundTag panelTag = nbt.getCompound(CreateLang.asId(slot.name()));
        if (!panelTag.isEmpty()) {
            FactoryPanelBehaviour behavior = (FactoryPanelBehaviour) (Object) this;
            if (RelativeNBTUtils.readTargetingRelative(panelTag, registries, behavior.getPos(), targeting)) {
                RelativeNBTUtils.readTargetedByRelative(panelTag, registries, behavior.getPos(), targetedBy);
                RelativeNBTUtils.readTargetedByLinksRelative(panelTag, registries, behavior.getPos(), targetedByLinks);
                if ((Object) this instanceof FPBExtension fpb) {
                    RelativeNBTUtils.readTargetedByExtraRelative(panelTag, registries, behavior.getPos(), fpb.deployer$getExtra());
                }
            }
        }
    }
}
