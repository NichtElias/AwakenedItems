package party.elias.awakeneditems;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.TriPredicate;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.function.BiPredicate;

public record OmniSlot (SlotType type, int index, String curioIdentifier) {

    public static OmniSlot equipment(EquipmentSlot slot) {
        return new OmniSlot(SlotType.EQUIPMENT, slot.ordinal(), null);
    }

    public static OmniSlot capability(int index) {
        return new OmniSlot(SlotType.CAPABILITY, index, null);
    }

    public static OmniSlot curio(String identifier, int index) {
        return new OmniSlot(SlotType.CURIO, index, identifier);
    }

    public boolean isForItem(LivingEntity entity, ItemStack itemStack) {
        return type.forItem.test(this, entity, itemStack);
    }

    public enum SlotType {
        EQUIPMENT((slot, entity, stack) -> {
            EquipmentSlot equipmentSlot = EquipmentSlot.values()[slot.index];
            return EquipmentSlotGroup.HAND.test(equipmentSlot) || stack.canEquip(equipmentSlot, entity);
        }),
        CAPABILITY((slot, entity, stack) -> {
            IItemHandler cap = entity.getCapability(Capabilities.ItemHandler.ENTITY);
            if (cap == null) return false;
            return cap.isItemValid(slot.index, stack);
        }),
        CURIO((slot, entity, stack) ->
                stack.is(TagKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath("curios", slot.curioIdentifier)))
        );

        private final TriPredicate<OmniSlot, LivingEntity, ItemStack> forItem;

        SlotType(TriPredicate<OmniSlot, LivingEntity, ItemStack> forItem) {
            this.forItem = forItem;
        }
    }
}
