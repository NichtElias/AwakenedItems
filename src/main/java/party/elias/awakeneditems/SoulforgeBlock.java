package party.elias.awakeneditems;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SoulforgeBlock extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = Shapes.or(
            Shapes.box(0, 12.0/16.0, 0, 1, 1, 1),
            Shapes.box(2.0/16.0, 10.0/16.0, 2.0/16.0, 14.0/16.0, 12.0/16.0, 14.0/16.0),
            Shapes.box(4.0/16.0, 2.0/16.0, 4.0/16.0, 12.0/16.0, 10.0/16.0, 12.0/16.0),
            Shapes.box(2.0/16.0, 0, 2.0/16.0, 14.0/16.0, 2.0/16.0, 14.0/16.0));

    public SoulforgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoulforgeBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Optional<SoulforgeBlockEntity> be = level.getBlockEntity(pos, AwakenedItems.SOULFORGE_BLOCK_ENTITY.get());

        if (be.isPresent() && !stack.isEmpty()) {
            int rest = be.get().tryInsertItem(stack);

            if (rest != stack.getCount()) {
                stack.setCount(rest);

                Utils.soulPuff(level, pos.getCenter().add(0, 0.6, 0));
                level.playLocalSound(pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1, 1, false);
            }

            return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        Optional<SoulforgeBlockEntity> be = level.getBlockEntity(pos, AwakenedItems.SOULFORGE_BLOCK_ENTITY.get());

        if (be.isPresent()) {
            ItemStack extracted = be.get().tryExtractItem();

            if (!extracted.isEmpty()) {

                Utils.dropAt(level, extracted, pos.getCenter().add(0, 1, 0));

                level.playLocalSound(pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1, 1, false);

                return InteractionResult.SUCCESS.withoutItem();
            }
            return InteractionResult.PASS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {

        Optional<SoulforgeBlockEntity> be = level.getBlockEntity(pos, AwakenedItems.SOULFORGE_BLOCK_ENTITY.get());

        for (ItemStack itemStack: be.get().getItems()) {
            Utils.dropAt(level, itemStack, pos.getCenter());
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
