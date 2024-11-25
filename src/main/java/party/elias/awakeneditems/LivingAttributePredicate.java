package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record LivingAttributePredicate(Map<ResourceLocation, MinMaxBounds.Doubles> attributes) implements EntitySubPredicate {
    public static final MapCodec<LivingAttributePredicate> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.unboundedMap(ResourceLocation.CODEC, MinMaxBounds.Doubles.CODEC)
                            .optionalFieldOf("attributes", new HashMap<>()).forGetter(LivingAttributePredicate::attributes)
            ).apply(instance, LivingAttributePredicate::new)
    );

    @Override
    public MapCodec<? extends EntitySubPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        if (entity instanceof LivingEntity living) {
            for (ResourceLocation resourceLocation: attributes.keySet()) {
                Holder<Attribute> attribute = level.registryAccess().holderOrThrow(ResourceKey.create(Registries.ATTRIBUTE, resourceLocation));
                if (!attributes.get(resourceLocation).matches(living.getAttributeValue(attribute))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
