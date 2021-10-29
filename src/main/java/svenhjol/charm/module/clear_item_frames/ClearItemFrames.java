package svenhjol.charm.module.clear_item_frames;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public class ClearItemFrames {
	
    private ActionResult handleUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        return ActionResult.PASS;
    }

    public ActionResult handleAttackEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
    	return ActionResult.PASS;
    }
    
}
