package party.elias.awakeneditems.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;
import party.elias.awakeneditems.AwakenedItems;
import party.elias.awakeneditems.OmniSlot;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;
import java.util.function.BiConsumer;

public class CuriosCompat {
    public static void forEquippedCurios(LivingEntity entity, TriConsumer<ItemStack, LivingEntity, OmniSlot> consumer) {
        ICuriosItemHandler cap = entity.getCapability(CuriosCapability.INVENTORY);

        if (cap != null) {
            for (Map.Entry<String, ICurioStacksHandler> entry: cap.getCurios().entrySet()) {
                IDynamicStackHandler stackHandler = entry.getValue().getStacks();
                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);

                    if (stack.has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
                        consumer.accept(stack, entity, OmniSlot.curio(entry.getKey(), i));
                    }
                }
            }
        }
    }
}
