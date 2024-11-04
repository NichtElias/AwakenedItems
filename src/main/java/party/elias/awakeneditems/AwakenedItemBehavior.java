package party.elias.awakeneditems;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
        return (int) (Math.pow(1.25, currentLevel) * 500);
    }

    public static void addXp(ItemStack stack, int amount, Level world) {
        AwakenedItemData data = stack.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        if (data != null) {
            data = data.withXp(data.xp() + amount);

            while (data.xp() >= getRequiredXp(data.level())) {
                data = data.withXp(data.xp() - getRequiredXp(data.level())).withLevel(data.level() + 1);

                stack.set(AwakenedItems.AWAKENED_ITEM_COMPONENT, data);
                AwakenedItemBehavior.speakToOwner(stack, world, "levelup", 0, Component.literal(String.valueOf(data.level())));
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

    public static void inventoryTick(ItemStack itemStack, LivingEntity entity) {
        AwakenedItemData awakenedItemData = itemStack.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        itemStack.set(AwakenedItems.AWAKENED_ITEM_COMPONENT, awakenedItemData.withHeldByOwner(entity.getUUID().equals(awakenedItemData.owner())));

        if (entity.level() instanceof ServerLevel serverLevel) {
            maybeSpeakToOwner(0.0005, itemStack, entity.level(), "random", 5000);

            if (!entity.getUUID().equals(awakenedItemData.owner()) && Math.random() < 0.05) {
                entity.hurt(new DamageSource(entity.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)),
                        1 + ((float) awakenedItemData.level() / 2));
            }
        }
    }
}
