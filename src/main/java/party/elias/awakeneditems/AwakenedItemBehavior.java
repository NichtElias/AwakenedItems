package party.elias.awakeneditems;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class AwakenedItemBehavior {

    public static List<PersonalityTrait> getRandomPersonality() {
        List<PersonalityTrait> pickedTraits = new ArrayList<>();
        List<PersonalityTrait> possibleTraits = new ArrayList<>(PersonalityTrait.SET);

        for (int i = 0; i < 2 && !possibleTraits.isEmpty(); i++) {
            PersonalityTrait t = Utils.randomChoice(possibleTraits);
            possibleTraits.removeAll(t.getIncompatibleTraitsAsTraits());
            possibleTraits.remove(t);
            pickedTraits.add(t);
        }

        return pickedTraits;
    }

    public static int getRequiredXp(int currentLevel) {
        return (int) (Math.pow(Config.Level.xpMultiplier, currentLevel) * Config.Level.xpBase);
    }

    public static void addXp(ItemStack stack, int amount, Level world) {
        AwakenedItemData data = stack.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        if (data != null) {
            data = data.withXp(data.xp() + amount);

            while (data.xp() >= getRequiredXp(data.level())) {
                MilestoneLevel milestoneLevel = MilestoneLevelManager.getFor(stack, data.level());
                if (milestoneLevel == null) {
                    data = data.withXp(data.xp() - getRequiredXp(data.level())).withLevel(data.level() + 1);

                    stack.set(AwakenedItems.AWAKENED_ITEM_COMPONENT, data);
                    AwakenedItemBehavior.speakToOwner(stack, world, "levelup", 0, Component.literal(String.valueOf(data.level())));
                } else {
                    if (!data.isFlagSet(AwakenedItemData.Flags.Flag.MILESTONE_XP)) {
                        AwakenedItemBehavior.speakToOwner(stack, world, "ml_xp." + milestoneLevel.name(), 0);
                        data = data.withFlagSet(AwakenedItemData.Flags.Flag.MILESTONE_XP, true);
                    }
                    break;
                }
            }

            stack.set(AwakenedItems.AWAKENED_ITEM_COMPONENT, data);
        }
    }

    public static void speakToOwner(ItemStack item, Level world, String trigger, int priority, Component... args) {
        AwakenedItemData aiData = item.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        if (aiData != null) {
            if (!world.isClientSide()) {
                ServerPlayer owner = (ServerPlayer) world.getPlayerByUUID(aiData.owner());
                if (owner != null) {
                    if (owner.getData(AwakenedItems.AWAKENED_ITEM_PLAYER_DATA_ATTACHMENT).timeSinceLastItemMsg() >= priority) {

                        PacketDistributor.sendToPlayer(owner, new ItemChatMessage(item, trigger, Arrays.stream(args).toList()));

                        owner.setData(AwakenedItems.AWAKENED_ITEM_PLAYER_DATA_ATTACHMENT,
                                owner.getData(AwakenedItems.AWAKENED_ITEM_PLAYER_DATA_ATTACHMENT).withTimeSinceLastItemMsg(0));
                    }
                }
            }
        }
    }

    public static void maybeSpeakToOwner(double p, ItemStack item, Level world, String trigger, int priority, Component... args) {
        if (Math.random() < p) {
            speakToOwner(item, world, trigger, priority, args);
        }
    }

    public static void damaged(ItemStack itemStack, ServerLevel level) {
        if ((double)itemStack.getDamageValue() / itemStack.getMaxDamage() > 0.95) {
            speakToOwner(itemStack, level, "damaged", 2000);
        }

        if (itemStack.getDamageValue() == itemStack.getMaxDamage() - 2) {
            speakToOwner(itemStack, level, "willdie", 0);
        }
    }

    public static void fulfillMilestoneRequirements(ItemStack itemStack, Level world, MilestoneLevel milestoneLevel) {
        speakToOwner(itemStack, world, "ml_requirements." + milestoneLevel.name(), 0);

        Utils.withAwakenedItemData(itemStack, awakenedItemData -> {
            itemStack.set(AwakenedItems.AWAKENED_ITEM_COMPONENT, awakenedItemData.withFlagSet(AwakenedItemData.Flags.Flag.MILESTONE_REQUIREMENTS, true));
        });
    }

    public static void milestoneLevelUp(ItemStack itemStack, Level world, MilestoneLevel milestoneLevel) {
        speakToOwner(itemStack, world, "ml_reached." + milestoneLevel.name(), 0, Component.literal(String.valueOf(milestoneLevel.level())));

        Utils.withAwakenedItemData(itemStack, awakenedItemData ->
                itemStack.set(AwakenedItems.AWAKENED_ITEM_COMPONENT, awakenedItemData
                        .withXp(awakenedItemData.xp() - getRequiredXp(awakenedItemData.level()))
                        .withLevel(awakenedItemData.level() + 1)
                        .withFlagSet(AwakenedItemData.Flags.Flag.MILESTONE_XP, false)
                        .withFlagSet(AwakenedItemData.Flags.Flag.MILESTONE_REQUIREMENTS, false))
        );
        addXp(itemStack, 0, world);
    }

    public static void inventoryTick(ItemStack itemStack, LivingEntity entity) {
        AwakenedItemData awakenedItemData = itemStack.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        awakenedItemData = awakenedItemData.withHeldByOwner(entity.getUUID().equals(awakenedItemData.owner()));
        itemStack.set(AwakenedItems.AWAKENED_ITEM_COMPONENT, awakenedItemData);

        if (awakenedItemData.heldByOwner()) {
            IEnergyStorage energyStorage = itemStack.getCapability(Capabilities.EnergyStorage.ITEM);

            if (energyStorage != null) {
                energyStorage.receiveEnergy(Mth.square(awakenedItemData.level() + 1), false);
            }

            if (AwakenedItemType.CURIO.checkItemOnly(itemStack)) {
                if (entity.level().getGameTime() % 100 == 0) {
                    addXp(itemStack, Config.Level.xpPerCurioHectotick, entity.level());
                }
            }
        }

        if (entity.level() instanceof ServerLevel serverLevel) {
            maybeSpeakToOwner(0.0005, itemStack, entity.level(), "random", 5000);

            if (!entity.getUUID().equals(awakenedItemData.owner()) && Math.random() < 0.05) {
                entity.hurt(new DamageSource(entity.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)),
                        1 + ((float) awakenedItemData.level() / 2));
            }
        }
    }
}
