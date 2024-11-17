package party.elias.awakeneditems.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import party.elias.awakeneditems.AwakenedItems;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.function.BiConsumer;

public class CuriosCompat {
    public static void forAllAwakenedItemsOnEntity(LivingEntity entity, BiConsumer<ItemStack, LivingEntity> consumer) {
        ICuriosItemHandler cap = entity.getCapability(CuriosCapability.INVENTORY);

        if (cap != null) {
            for (int i = 0; i < cap.getEquippedCurios().getSlots(); i++) {
                ItemStack stack = cap.getEquippedCurios().getStackInSlot(i);

                if (stack.has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
                    consumer.accept(stack, entity);
                }
            }
        }
    }
}
