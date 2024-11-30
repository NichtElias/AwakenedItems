package party.elias.awakeneditems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SoulforgeBlock extends Block implements EntityBlock {

    public SoulforgeBlock() {
        super(BlockBehaviour.Properties.of().destroyTime(10).explosionResistance(10).sound(SoundType.ANVIL).noOcclusion());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoulforgeBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Optional<SoulforgeBlockEntity> be = level.getBlockEntity(pos, AwakenedItems.SOULFORGE_BLOCK_ENTITY.get());

        if (be.isPresent() && !stack.isEmpty()) {
            int rest = be.get().tryInsertItem(stack);

            if (rest != stack.getCount()) {
                stack.setCount(rest);

                Utils.soulPuff(level, pos.getCenter().add(0, 0.6, 0));
                level.playLocalSound(pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1, 1, false);
            }

            return ItemInteractionResult.SUCCESS;
        } else {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
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

                return InteractionResult.SUCCESS;
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
}
