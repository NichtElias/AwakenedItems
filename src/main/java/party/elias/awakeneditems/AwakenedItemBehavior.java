package party.elias.awakeneditems;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
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

    public static void speakToOwner(ItemStack item, Level world, Component msg, int priority) {
        AwakenedItemData aiData = item.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        if (aiData != null) {
            if (!world.isClientSide()) {
                Player owner = world.getPlayerByUUID(aiData.owner());
                if (owner != null) {
                    if (owner.getData(AwakenedItems.AWAKENED_ITEM_PLAYER_DATA_ATTACHMENT).timeSinceLastItemMsg() >= priority) {
                        owner.sendSystemMessage(formattedItemChatMessage(item, msg));
                        owner.setData(AwakenedItems.AWAKENED_ITEM_PLAYER_DATA_ATTACHMENT,
                                owner.getData(AwakenedItems.AWAKENED_ITEM_PLAYER_DATA_ATTACHMENT).withTimeSinceLastItemMsg(0));
                    }
                }
            }
        }
    }

    public static void onItemLevelUp(ItemStack stack, AwakenedItemData aiData, Level world) {
        speakToOwner(stack, world, Component.translatable("chat.awakeneditems.aimsg.levelup", aiData.level()), 0);
    }

    public static void onItemWouldDespawn(ItemStack item, ItemEntity entity) {
        speakToOwner(item, entity.level(), Component.translatable("chat.awakeneditems.aimsg.despawn"), 0);
    }

    public static Component formattedItemChatMessage(ItemStack itemStack, Component message) {
        return Component.literal("<").append(itemStack.getDisplayName()).append(Component.literal("> ")).append(message);
    }

    public static void inventoryTick(ItemStack itemStack, LivingEntity entity) {
        AwakenedItemData awakenedItemData = itemStack.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        itemStack.set(AwakenedItems.AWAKENED_ITEM_COMPONENT, awakenedItemData.withHeldByOwner(entity.getUUID().equals(awakenedItemData.owner())));

        if (!entity.getUUID().equals(awakenedItemData.owner())) {
            entity.hurt(new DamageSource(entity.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)),
                    1 + ((float)awakenedItemData.level() / 2));
        }
    }
}
