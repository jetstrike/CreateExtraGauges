package net.liukrast.eg.mixin;

import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import dev.simulated_team.simulated.content.display_sources.NavigationTableDisplaySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = NavigationTableDisplaySource.class, remap = false)
public class NavigationTableDisplaySourceMixin {
    @Redirect(
        method = "provideLine",
        at = @At(
            value = "INVOKE",
            target = "Ldev/simulated_team/simulated/content/blocks/nav_table/navigation_target/NavigationTarget;distanceToTarget(Ldev/simulated_team/simulated/content/blocks/nav_table/NavTableBlockEntity;)D"
        )
    )
    private double useCachedDistance(NavigationTarget target, NavTableBlockEntity be) {
        return be.distanceToTarget();
    }
}
