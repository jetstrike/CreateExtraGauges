package net.liukrast.eg.helper;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class EGPonderSceneHelpers {
    private EGPonderSceneHelpers() {}

    public static void displayText(SceneBuilder builder, BlockPos pos, int time, boolean keyframe) {
        net.liukrast.deployer.lib.helper.PonderSceneHelpers.displayText(builder, pos, time, keyframe);
    }

    public static void displayText(SceneBuilder builder, FactoryPanelPosition pos, int time, boolean keyframe) {
        net.liukrast.deployer.lib.helper.PonderSceneHelpers.displayText(builder, pos.pos(), time, keyframe);
    }

    public static void displayText(SceneBuilder builder, FactoryPanelPosition pos, Direction direction, int time, boolean keyframe) {
        net.liukrast.deployer.lib.helper.PonderSceneHelpers.displayText(builder, pos.pos(), time, keyframe);
    }

    public static void displayText(SceneBuilder builder, BlockPos pos, Direction direction, int time, boolean keyframe) {
        net.liukrast.deployer.lib.helper.PonderSceneHelpers.displayText(builder, pos, time, keyframe);
    }

    public static Vec3 getGaugeWorldCenter(FactoryPanelPosition gauge, Direction facing) {
        return gauge.pos().getCenter().add(new Vec3(facing.getStepX() * 0.5, facing.getStepY() * 0.5, facing.getStepZ() * 0.5));
    }
}
