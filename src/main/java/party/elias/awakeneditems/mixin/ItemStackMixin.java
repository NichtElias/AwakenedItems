package party.elias.awakeneditems.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import party.elias.awakeneditems.AwakenedItemBehavior;
import party.elias.awakeneditems.AwakenedItemData;
import party.elias.awakeneditems.AwakenedItemType;
import party.elias.awakeneditems.AwakenedItems;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @WrapMethod(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V")
    private void hurtAndBreak(int damage, ServerLevel level, LivingEntity entity, Consumer<Item> onBreak, Operation<Void> original) {
        ItemStack itemStack = (ItemStack)(Object)this;
        AwakenedItemData awakenedItemData = itemStack.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        if (awakenedItemData != null) {
            if (entity.getRandom().nextDouble() < awakenedItemData.level() / (AwakenedItemType.SHIELD.checkItem(itemStack) ? 20.0 : 40.0)) {
                damage = 0;
            }
        }

        original.call(damage, level, entity, onBreak);
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"))
    private void applyDamage(int damage, ServerLevel level, LivingEntity entity, Consumer<Item> onBreak, CallbackInfo ci) {
        ItemStack itemStack = (ItemStack)(Object)this;
        if (itemStack.isDamageableItem() && damage > 0) {
            AwakenedItemBehavior.damaged(itemStack, level);
        }
    }
}
