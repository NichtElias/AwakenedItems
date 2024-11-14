package party.elias.awakeneditems;

import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class Utils {
    public static double getSummedAttributeModifiers(ItemAttributeModifiers modifiers, Holder<Attribute> attribute, AttributeModifier.Operation operation) {
        double sum = 0;
        for (ItemAttributeModifiers.Entry entry: modifiers.modifiers()) {
            if (entry.attribute().value().equals(attribute.value()) && entry.modifier().operation() == operation) {
                sum += entry.modifier().amount();
            }
        }
        return sum;
    }

    public static <T> T randomChoice(Collection<T> c) {
        List<T> l = List.copyOf(c);

        return l.get((int)((double)l.size() * Math.random()));
    }

    public static void forAllAwakenedItemsOnEntity(LivingEntity entity, BiConsumer<ItemStack, LivingEntity> consumer) {
        if (entity instanceof Player player) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);

                if (stack.has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
                    consumer.accept(stack, player);
                }
            }
        } else {
            for (EquipmentSlot slot: EquipmentSlot.values()) {
                ItemStack item = entity.getItemBySlot(slot);

                if (item.has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
                    consumer.accept(item, entity);
                }
            }
        }
    }

    public static ServerPlayer getPlayerByUUIDFromServer(MinecraftServer server, UUID uuid) {
        for (ServerPlayer player: server.getPlayerList().getPlayers()) {
            if (player.getUUID().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    public static double getOwnerPower(ItemStack itemStack) {
        AwakenedItemData awakenedItemData = itemStack.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        if (awakenedItemData != null) {
            Player player = null;

            if (CommonGameEvents.SERVER != null) {
                player = getPlayerByUUIDFromServer(CommonGameEvents.SERVER, awakenedItemData.owner());
            } else {
                Level level = ClientUtils.getLevel();

                if (level != null) {
                    player = level.getPlayerByUUID(awakenedItemData.owner());
                }
            }

            if (player != null) {
                return player.getAttributeValue(AwakenedItems.AI_POWER_ATTRIBUTE);
            }
        }

        return 1;
    }

    public static double getPower(Entity entity) {
        if (entity instanceof LivingEntity livingEntity)
            return livingEntity.getAttributeValue(AwakenedItems.AI_POWER_ATTRIBUTE);
        return 1;
    }
}
