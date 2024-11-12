package party.elias.awakeneditems.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import party.elias.awakeneditems.AwakenedItemData;
import party.elias.awakeneditems.AwakenedItems;

@Mixin(Player.class)
public class PlayerMixin {
    @WrapOperation(method = "blockUsingShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;disableShield(Lnet/minecraft/world/item/ItemStack;)V"))
    private void blockUsingShield(Player instance, ItemStack itemStack, Operation<Void> original) {
        AwakenedItemData awakenedItemData = itemStack.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        if (awakenedItemData != null) {
            if (instance.getRandom().nextDouble() < awakenedItemData.level() / 20.0) {
                return;
            }
        }

        original.call(instance, itemStack);
    }
}
