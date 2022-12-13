package de.felix.test.mixin;

import de.felix.test.Test;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(at = @At("HEAD"), method = "shouldDrawSide", cancellable = true)
    private static void shouldDrawSideMixin(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos otherPos, CallbackInfoReturnable<Boolean> cir) {
        if (Test.xRayEnabled) {
            if (Test.xRayBlocks.contains(state.getBlock())) {
                cir.setReturnValue(true);
                return;
            }
            cir.setReturnValue(false);
        }
    }
}
