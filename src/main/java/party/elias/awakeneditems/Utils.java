package party.elias.awakeneditems;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class Utils {
    public static Component formattedItemChatMessage(ItemStack itemStack, Component message) {
        return Component.literal("<").append(itemStack.getDisplayName()).append(Component.literal("> ")).append(message);
    }
}
