package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;

public record MilestoneLevel (int level, AwakenedItemType itemType, String name, ResourceLocation trigger) {
    public static final Codec<MilestoneLevel> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("level").forGetter(MilestoneLevel::level),
                    StringRepresentable.fromEnum(AwakenedItemType::values).fieldOf("itemType").forGetter(MilestoneLevel::itemType),
                    Codec.STRING.fieldOf("name").forGetter(MilestoneLevel::name),
                    ResourceLocation.CODEC.fieldOf("trigger").forGetter(MilestoneLevel::trigger)
            ).apply(instance, MilestoneLevel::new)
    );

    private static Registry<MilestoneLevel> REGISTRY = null;

    public static Registry<MilestoneLevel> getRegistry() {
        if (REGISTRY == null)
            REGISTRY = (Registry<MilestoneLevel>) BuiltInRegistries.REGISTRY.get(AwakenedItems.MILESTONE_LEVEL_REGISTRY_KEY.location());
        return REGISTRY;
    }

    public static void triggerAll(AdvancementHolder advancement, Player player) {

        MilestoneLevel.getRegistry().forEach(
                milestoneLevel -> {
                    if (milestoneLevel.trigger.equals(advancement.id())) {
                        Utils.forAllAwakenedItemsOnEntity(player, (itemStack, livingEntity) -> {
                                if (milestoneLevel.itemType.checkItem(itemStack) && Utils.checkAwakenedItem(itemStack,
                                        aiData -> aiData.level() + 1 == milestoneLevel.level)) {

                                }
                        });
                    }
                }
        );
    }
}
