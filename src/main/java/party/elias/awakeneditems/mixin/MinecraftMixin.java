package party.elias.awakeneditems.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @WrapMethod(method = "shouldEntityAppearGlowing")
    private boolean shouldEntityAppearGlowing(Entity entity, Operation<Boolean> original) {
        if (false) { // check if entity should be highlighted by item once that's ready
            return true;
        }
        return original.call(entity);
    }
}
