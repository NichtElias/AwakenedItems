package party.elias.awakeneditems;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AwakenedItemBehavior {

    public static int getRequiredXp(int currentLevel) {
        return (int) (Math.pow(1.25, currentLevel) * 10);
    }

    public static void addXp(ItemStack stack, int amount, Level world) {
        AwakenedItemData data = stack.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        if (data != null) {
            data = data.withXp(data.getXp() + amount);

            while (data.getXp() >= getRequiredXp(data.getLevel())) {
                data = data.withXp(data.getXp() - getRequiredXp(data.getLevel())).withLevel(data.getLevel() + 1);

                stack.set(AwakenedItems.AWAKENED_ITEM_COMPONENT, data);
                onItemLevelUp(stack, data, world);
            }

            stack.set(AwakenedItems.AWAKENED_ITEM_COMPONENT, data);
        }
    }

    public static void onItemLevelUp(ItemStack stack, AwakenedItemData aiData, Level world) {
        if (!world.isClientSide()) {
            Player owner = world.getPlayerByUUID(aiData.getOwner());
            if (owner != null) {
                owner.sendSystemMessage(Utils.formattedItemChatMessage(stack, Component.translatable("chat.awakeneditems.aimsg.levelup", aiData.getLevel())));
            }
        }
    }
}
