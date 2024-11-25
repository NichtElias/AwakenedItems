package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.Ingredient;

public record MilestoneLevel (int level, AwakenedItemType itemType, String name, ResourceLocation trigger, boolean hasToBeHeld, Ingredient reforgingFinisher, int priority) {
    public static final Codec<MilestoneLevel> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("level").forGetter(MilestoneLevel::level),
                    StringRepresentable.fromEnum(AwakenedItemType::values).fieldOf("itemType").forGetter(MilestoneLevel::itemType),
                    Codec.STRING.fieldOf("name").forGetter(MilestoneLevel::name),
                    ResourceLocation.CODEC.fieldOf("trigger").forGetter(MilestoneLevel::trigger),
                    Codec.BOOL.optionalFieldOf("hasToBeHeld", false).forGetter(MilestoneLevel::hasToBeHeld),
                    Ingredient.CODEC.fieldOf("reforgingFinisher").forGetter(MilestoneLevel::reforgingFinisher),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(MilestoneLevel::priority)
            ).apply(instance, MilestoneLevel::new)
    );

}
