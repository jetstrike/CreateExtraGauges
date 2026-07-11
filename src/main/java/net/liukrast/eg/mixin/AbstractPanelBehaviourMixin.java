package net.liukrast.eg.mixin;

import net.liukrast.deployer.lib.logistics.board.AbstractPanelBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.liukrast.eg.content.logistics.link.RelativeNBTUtils;

@Mixin(value = AbstractPanelBehaviour.class, remap = false)
public abstract class AbstractPanelBehaviourMixin {

    @Inject(method = "write", at = @At("RETURN"))
    private void onWrite(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        var behavior = (AbstractPanelBehaviour) (Object) this;
        if (behavior.active) {
            CompoundTag panelTag = nbt.getCompound(CreateLang.asId(behavior.slot.name()));
            if (!panelTag.isEmpty()) {
                RelativeNBTUtils.writeTargetingRelative(panelTag, registries, behavior.getPos(), behavior.targeting);
                RelativeNBTUtils.writeTargetedByRelative(panelTag, registries, behavior.getPos(), behavior.targetedBy.values());
                RelativeNBTUtils.writeTargetedByLinksRelative(panelTag, registries, behavior.getPos(), behavior.targetedByLinks.values());
                RelativeNBTUtils.writeTargetedByExtraRelative(panelTag, registries, behavior.getPos(), behavior.getTargetedByExtra().values());
            }
        }
    }
}
