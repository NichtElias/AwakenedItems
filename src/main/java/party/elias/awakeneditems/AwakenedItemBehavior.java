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
            data = data.withXp(data.xp() + amount);

            while (data.xp() >= getRequiredXp(data.level())) {
                data = data.withXp(data.xp() - getRequiredXp(data.level())).withLevel(data.level() + 1);

                stack.set(AwakenedItems.AWAKENED_ITEM_COMPONENT, data);
                onItemLevelUp(stack, data, world);
            }

            stack.set(AwakenedItems.AWAKENED_ITEM_COMPONENT, data);
        }
    }

    public static void onItemLevelUp(ItemStack stack, AwakenedItemData aiData, Level world) {
        if (!world.isClientSide()) {
            Player owner = world.getPlayerByUUID(aiData.owner());
            if (owner != null) {
                owner.sendSystemMessage(formattedItemChatMessage(stack, Component.translatable("chat.awakeneditems.aimsg.levelup", aiData.level())));
            }
        }
    }

    public static Component formattedItemChatMessage(ItemStack itemStack, Component message) {
        return Component.literal("<").append(itemStack.getDisplayName()).append(Component.literal("> ")).append(message);
    }
}
