package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;

public record MilestoneLevel (int level, AwakenedItemType itemType, String name, ResourceLocation trigger, boolean hasToBeHeld) {
    public static final Codec<MilestoneLevel> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("level").forGetter(MilestoneLevel::level),
                    StringRepresentable.fromEnum(AwakenedItemType::values).fieldOf("itemType").forGetter(MilestoneLevel::itemType),
                    Codec.STRING.fieldOf("name").forGetter(MilestoneLevel::name),
                    ResourceLocation.CODEC.fieldOf("trigger").forGetter(MilestoneLevel::trigger),
                    Codec.BOOL.optionalFieldOf("hasToBeHeld", false).forGetter(MilestoneLevel::hasToBeHeld)
            ).apply(instance, MilestoneLevel::new)
    );

    public static Registry<MilestoneLevel> getRegistry(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(AwakenedItems.MILESTONE_LEVEL_REGISTRY_KEY);
    }

    public static void triggerAll(AdvancementHolder advancement, Player player) {

        MilestoneLevel.getRegistry(player.level().registryAccess()).forEach(
                milestoneLevel -> {
                    if (milestoneLevel.trigger.equals(advancement.id())) {

                        if (milestoneLevel.hasToBeHeld()) {

                            ItemStack itemStack = player.getMainHandItem();
                            Utils.withAwakenedItemData(itemStack, awakenedItemData -> {
                                if (milestoneLevel.itemType.checkItem(itemStack) && awakenedItemData.level() + 1 == milestoneLevel.level
                                        && awakenedItemData.xp() >= AwakenedItemBehavior.getRequiredXp(awakenedItemData.level())) {
                                    AwakenedItemBehavior.fulfillMilestoneRequirements(itemStack, player.level(), milestoneLevel);
                                }
                            });
                        } else {

                            Utils.forAllAwakenedItemsOnEntity(player, (itemStack, livingEntity) -> {
                                Utils.withAwakenedItemData(itemStack, awakenedItemData -> {
                                    if (milestoneLevel.itemType.checkItem(itemStack) && awakenedItemData.level() + 1 == milestoneLevel.level
                                            && awakenedItemData.xp() >= AwakenedItemBehavior.getRequiredXp(awakenedItemData.level())) {
                                        AwakenedItemBehavior.fulfillMilestoneRequirements(itemStack, player.level(), milestoneLevel);
                                    }
                                });
                            });
                        }
                    }
                }
        );
    }

    public static MilestoneLevel getFor(Level world, ItemStack itemStack, int level) {
        for (Map.Entry<ResourceKey<MilestoneLevel>, MilestoneLevel> entry: MilestoneLevel.getRegistry(world.registryAccess()).entrySet()) {
            MilestoneLevel milestoneLevel = entry.getValue();

            if (milestoneLevel.itemType().checkItem(itemStack) && milestoneLevel.level() == level) {
                return milestoneLevel;
            }
        }
        return null;
    }
}
