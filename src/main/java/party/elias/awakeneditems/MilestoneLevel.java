package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Optional;

public record MilestoneLevel (int level, AwakenedItemType itemType, String name, ResourceLocation trigger, Optional<EquipmentSlotGroup> inSlot, Ingredient reforgingFinisher, int priority) {
    public static final Codec<MilestoneLevel> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("level").forGetter(MilestoneLevel::level),
                    StringRepresentable.fromEnum(AwakenedItemType::values).fieldOf("item_type").forGetter(MilestoneLevel::itemType),
                    Codec.STRING.fieldOf("name").forGetter(MilestoneLevel::name),
                    ResourceLocation.CODEC.fieldOf("trigger").forGetter(MilestoneLevel::trigger),
                    EquipmentSlotGroup.CODEC.optionalFieldOf("in_slot").forGetter(MilestoneLevel::inSlot),
                    Ingredient.CODEC.fieldOf("reforging_finisher").forGetter(MilestoneLevel::reforgingFinisher),
                    Codec.INT.optionalFieldOf("priority", 0).forGetter(MilestoneLevel::priority)
            ).apply(instance, MilestoneLevel::new)
    );

}
