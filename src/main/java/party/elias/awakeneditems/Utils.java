package party.elias.awakeneditems;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.apache.logging.log4j.util.TriConsumer;
import party.elias.awakeneditems.compat.CuriosCompat;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Utils {
    public static double getSummedAttributeModifiers(ItemAttributeModifiers modifiers, Holder<Attribute> attribute, AttributeModifier.Operation operation) {
        double sum = 0;
        for (ItemAttributeModifiers.Entry entry: modifiers.modifiers()) {
            if (entry.attribute().value().equals(attribute.value()) && entry.modifier().operation() == operation) {
                sum += entry.modifier().amount();
            }
        }
        return sum;
    }

    public static <T> T randomChoice(Collection<T> c) {
        List<T> l = List.copyOf(c);

        return l.get((int)((double)l.size() * Math.random()));
    }

    public static void forAllAwakenedItemsOnEntity(LivingEntity entity, TriConsumer<ItemStack, LivingEntity, OmniSlot> consumer) {
        IItemHandler cap = entity.getCapability(Capabilities.ItemHandler.ENTITY);

        if (cap != null) {
            for (int i = 0; i < cap.getSlots(); i++) {
                ItemStack stack = cap.getStackInSlot(i);

                if (stack.has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
                    consumer.accept(stack, entity, OmniSlot.capability(i));
                }
            }
        }

        if (ModList.get().isLoaded("curios")) {
            CuriosCompat.forEquippedCurios(entity, consumer);
        }
    }

    public static void forAllEquippedAwakenedItems(LivingEntity entity, TriConsumer<ItemStack, LivingEntity, OmniSlot> consumer) {
        for (EquipmentSlot slot: EquipmentSlot.values()) {
            ItemStack item = entity.getItemBySlot(slot);

            if (item.has(AwakenedItems.AWAKENED_ITEM_COMPONENT)) {
                consumer.accept(item, entity, OmniSlot.equipment(slot));
            }
        }

        if (ModList.get().isLoaded("curios")) {
            CuriosCompat.forEquippedCurios(entity, consumer);
        }
    }

    public static ServerPlayer getPlayerByUUIDFromServer(MinecraftServer server, UUID uuid) {
        for (ServerPlayer player: server.getPlayerList().getPlayers()) {
            if (player.getUUID().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    public static double getOwnerPower(ItemStack itemStack) {
        AwakenedItemData awakenedItemData = itemStack.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        if (awakenedItemData != null) {
            Player player = null;

            if (CommonGameEvents.SERVER != null) {
                player = getPlayerByUUIDFromServer(CommonGameEvents.SERVER, awakenedItemData.owner());
            } else {
                Level level = ClientUtils.getLevel();

                if (level != null) {
                    player = level.getPlayerByUUID(awakenedItemData.owner());
                }
            }

            if (player != null) {
                return player.getAttributeValue(AwakenedItems.AI_POWER_ATTRIBUTE);
            }
        }

        return 1;
    }

    public static double getPower(Entity entity) {
        if (entity instanceof LivingEntity livingEntity)
            return livingEntity.getAttributeValue(AwakenedItems.AI_POWER_ATTRIBUTE);
        return 1;
    }


    public static void withAwakenedItemData(ItemStack stack, Consumer<AwakenedItemData> consumer) {
        withAwakenedItemDataDo(stack, awakenedItemData -> {consumer.accept(awakenedItemData); return null;});
    }

    public static <T> T withAwakenedItemDataDo(ItemStack stack, Function<AwakenedItemData, T> function) {
        AwakenedItemData awakenedItemData = stack.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);
        if (awakenedItemData != null) {
            return function.apply(awakenedItemData);
        }
        return null;
    }

    public static boolean checkAwakenedItem(ItemStack itemStack, Predicate<AwakenedItemData> predicate) {
        AwakenedItemData awakenedItemData = itemStack.get(AwakenedItems.AWAKENED_ITEM_COMPONENT);
        if (awakenedItemData != null) {
            return predicate.test(awakenedItemData);
        }
        return false;
    }

    public static void soulPuff(Level level, Vec3 pos) {
        for (int i = 0; i < 20; i++) {
            level.addParticle(ParticleTypes.SOUL, pos.x, pos.y, pos.z, Math.random() / 10 - 0.05, Math.random() / 10 - 0.05, Math.random() / 10 - 0.05);
        }
    }

    public static void dropAt(Level level, ItemStack itemStack, Vec3 pos) {
        ItemEntity entity = new ItemEntity(level, pos.x, pos.y, pos.z, itemStack);
        entity.setDefaultPickUpDelay();
        level.addFreshEntity(entity);
    }

    public static void revokeAdvancement(ServerPlayer player, AdvancementHolder advancementHolder) {
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancementHolder);

        for (String s: progress.getCompletedCriteria()) {
            player.getAdvancements().revoke(advancementHolder, s);
        }
    }

    public static boolean advancementExists(ResourceLocation resourceLocation) {
        return CommonGameEvents.SERVER.getAdvancements().get(resourceLocation) != null;
    }
}
