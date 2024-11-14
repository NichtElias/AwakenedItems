package party.elias.awakeneditems.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import party.elias.awakeneditems.AwakenedItemData;
import party.elias.awakeneditems.AwakenedItems;
import party.elias.awakeneditems.Utils;

@Mixin(ThrownTrident.class)
public class ThrownTridentMixin {

    @ModifyVariable(method = "onHitEntity", ordinal = 0, at = @At(value = "STORE", ordinal = 0))
    private float modifyDamage(float value, EntityHitResult result) {
        ThrownTrident trident = ((ThrownTrident)(Object)this);
        Entity thrower = trident.getOwner();
        ItemStack itemStack = trident.getWeaponItem();

        AwakenedItemData awakenedItemData = itemStack.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        if (awakenedItemData != null && awakenedItemData.owner().equals(thrower.getUUID())) {
            value += (float) (awakenedItemData.level() / 20.0 * 12.0 * Utils.getPower(thrower));
        }

        return value;
    }
}
