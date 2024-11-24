package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SoulforgeBlockEntity extends BlockEntity {

    private ItemHandler itemHandler;

    public SoulforgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(AwakenedItems.SOULFORGE_BLOCK_ENTITY.get(), pos, blockState);

        itemHandler = new ItemHandler();
    }

    public int tryInsertItem(ItemStack itemStack) {
        boolean isAwakenedItem = Utils.checkAwakenedItem(itemStack, awakenedItemData -> true);
        if (isAwakenedItem == itemHandler.items.isEmpty()) {
            if (isAwakenedItem) {
                boolean isReady = Boolean.TRUE.equals(Utils.withAwakenedItemDataDo(itemStack, awakenedItemData ->
                        MilestoneLevel.getFor(getLevel(), itemStack, awakenedItemData.level()) != null
                        && awakenedItemData.xp() >= AwakenedItemBehavior.getRequiredXp(awakenedItemData.level())
                        && awakenedItemData.isFlagSet(AwakenedItemData.Flags.Flag.MILESTONE_REQUIREMENTS)
                ));

                if (!isReady) {
                    return itemStack.getCount();
                }
            }

            itemHandler.items.add(itemStack.copyWithCount(1));

            if (!isAwakenedItem) {
                ItemStack awakened = itemHandler.items.getFirst();

                MilestoneLevel milestoneLevel = MilestoneLevel.getFor(getLevel(), awakened);

                if (milestoneLevel.reforgingFinisher().test(itemStack)) {
                    reforge();
                }
            }

            setChanged();

            return itemStack.getCount() - 1;
        }

        return itemStack.getCount();
    }

    public ItemStack tryExtractItem() {
        if (!itemHandler.items.isEmpty()) {
            setChanged();
            return itemHandler.items.removeLast();
        } else {
            return ItemStack.EMPTY;
        }
    }

    public void reforge() {
        ItemStack awakened = itemHandler.items.getFirst();

        itemHandler.items.clear();

        AwakenedItemBehavior.milestoneLevelUp(awakened, getLevel(), MilestoneLevel.getFor(getLevel(), awakened));

        Utils.dropAt(getLevel(), awakened, getBlockPos().getCenter().add(0, 1, 0));
        Utils.soulPuff(getLevel(), getBlockPos().getCenter().add(0, 1, 0));
        getLevel().playLocalSound(getBlockPos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.75f, 1, false);
        getLevel().playLocalSound(getBlockPos(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1, false);
    }

    public List<ItemStack> getItems() {
        return itemHandler.items;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        ListTag itemList = tag.getList("items", CompoundTag.TAG_COMPOUND);

        for (int i = 0; i < itemList.size(); i++) {
            itemHandler.items.add(ItemStack.parse(registries, itemList.get(i)).orElseThrow());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        ListTag listTag = new ListTag();

        for (ItemStack itemStack: itemHandler.items) {
            listTag.add(itemStack.save(registries));
        }

        tag.put("items", listTag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static class ItemHandler implements IItemHandler {

        private List<ItemStack> items;

        public ItemHandler() {
            items = new ArrayList<>();
        }

        @Override
        public int getSlots() {
            return items.size() + 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot >= items.size() ? ItemStack.EMPTY : items.get(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack; // don't allow inserting for now
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY; // don't allow extracting for now
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
    }
}
