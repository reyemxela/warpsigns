package net.reyemxela.warpsigns.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.reyemxela.warpsigns.utils.Pairing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlock.class)
public abstract class SignBlockMixin {
    @Inject(at = @At("HEAD"), method = "getStateForNeighborUpdate")
    private void neighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> info) {
        if (world.isClient()) { return; }
        if (!((SignBlock)(Object)this).canPlaceAt(state, world, pos)) {
            Pairing.breakSign((ServerWorld)world, null, pos);
        }
    }
}