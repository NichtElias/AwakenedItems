package party.elias.awakeneditems;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MilestoneLevelManager extends SimplePreparableReloadListener<Void> {

    private static MilestoneLevelManager INSTANCE = null;

    private final RegistryAccess registryAccess;

    private MilestoneLevelManager(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
    }

    public static MilestoneLevelManager init(RegistryAccess registryAccess) {
        if (INSTANCE == null) {
            INSTANCE = new MilestoneLevelManager(registryAccess);
        }
        return INSTANCE;
    }

    public static Registry<MilestoneLevel> getRegistry() {
        return INSTANCE.registryAccess.registryOrThrow(AwakenedItems.MILESTONE_LEVEL_REGISTRY_KEY);
    }

    public static void triggerAll(AdvancementHolder advancement, Player player) {

        getRegistry().forEach(
                milestoneLevel -> {
                    if (milestoneLevel.trigger().equals(advancement.id())) {

                        if (milestoneLevel.hasToBeHeld()) {

                            ItemStack itemStack = player.getMainHandItem();
                            Utils.withAwakenedItemData(itemStack, awakenedItemData -> {
                                if (milestoneLevel.itemType().checkItem(itemStack) && awakenedItemData.level() + 1 == milestoneLevel.level()
                                        && awakenedItemData.xp() >= AwakenedItemBehavior.getRequiredXp(awakenedItemData.level())) {
                                    AwakenedItemBehavior.fulfillMilestoneRequirements(itemStack, player.level(), milestoneLevel);
                                }
                            });
                        } else {

                            Utils.forAllAwakenedItemsOnEntity(player, (itemStack, livingEntity) -> {
                                Utils.withAwakenedItemData(itemStack, awakenedItemData -> {
                                    if (milestoneLevel.itemType().checkItem(itemStack) && awakenedItemData.level() + 1 == milestoneLevel.level()
                                            && awakenedItemData.xp() >= AwakenedItemBehavior.getRequiredXp(awakenedItemData.level())) {
                                        AwakenedItemBehavior.fulfillMilestoneRequirements(itemStack, player.level(), milestoneLevel);
                                    }
                                });
                            });
                        }

                        if (player instanceof ServerPlayer serverPlayer) {
                            Utils.revokeAdvancement(serverPlayer, advancement);
                        }
                    }
                }
        );
    }

    public static MilestoneLevel getFor(ItemStack itemStack, int level) {
        ArrayList<MilestoneLevel> matching = new ArrayList<>();

        for (Map.Entry<ResourceKey<MilestoneLevel>, MilestoneLevel> entry: getRegistry().entrySet()) {
            MilestoneLevel milestoneLevel = entry.getValue();

            if (milestoneLevel.itemType().checkItem(itemStack) && milestoneLevel.level() == level + 1) {
                matching.add(milestoneLevel);
            }
        }

        if (matching.isEmpty()) {
            return null;
        }

        return matching.stream().max((a, b) -> {
            int specificityDifference = a.itemType().getSpecificity() - b.itemType().getSpecificity();
            if (specificityDifference == 0) {
                return a.priority() - b.priority();
            }
            return specificityDifference;
        }).get();
    }

    public static MilestoneLevel getFor(ItemStack itemStack) {
        return Utils.withAwakenedItemDataDo(itemStack, awakenedItemData -> getFor(itemStack, awakenedItemData.level()));
    }

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {

    }
}
