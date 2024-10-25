package party.elias.awakeneditems;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

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
}
