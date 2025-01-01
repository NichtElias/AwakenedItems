package party.elias.awakeneditems.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import party.elias.awakeneditems.AwakenedItems;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @WrapOperation(method = "updateFallFlyingMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;", ordinal = 1))
    private Vec3 modify(Vec3 instance, double x, double y, double z, Operation<Vec3> original) {
        LivingEntity living = (LivingEntity)(Object)this;

        double efficiency = living.getAttributeValue(AwakenedItems.GLIDER_EFFICIENCY_ATTRIBUTE);

        return original.call(instance, x * efficiency, y * efficiency, z * efficiency);
    }

}
