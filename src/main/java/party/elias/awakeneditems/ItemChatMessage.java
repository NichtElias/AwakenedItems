package party.elias.awakeneditems;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record ItemChatMessage(ItemStack item, String trigger, List<Component> formatArgs) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ItemChatMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "item_chat_message"));

    public static final Codec<Component> COMPONENT_CODEC = Codec.STRING.xmap(
            s -> Component.Serializer.fromJson(s, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)),
            c -> Component.Serializer.toJson(c, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY))
    );

    public static final StreamCodec<ByteBuf, Component> COMPONENT_STREAM_CODEC = ByteBufCodecs.fromCodec(COMPONENT_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemChatMessage> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ItemChatMessage::item,
            ByteBufCodecs.STRING_UTF8, ItemChatMessage::trigger,
            COMPONENT_STREAM_CODEC.apply(ByteBufCodecs.list()), ItemChatMessage::formatArgs,
            ItemChatMessage::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final String AIMSG_KEY = "chat.awakeneditems.aimsg.%s.%s.%s";

    public static void handle(ItemChatMessage icm, IPayloadContext context) {

        sendItemChatMessage(context.player(), icm);

    }

    public static void sendItemChatMessage(Player player, ItemChatMessage icm) {

        AwakenedItemData aiData = icm.item().get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        if (aiData != null) {
            List<String> keys = getPossibleTranslationKeys(icm, aiData);

            String key = null;

            for (String k: keys) {
                if (I18n.exists(k)) {
                    key = k;
                    break;
                }
            }

            if (key == null) {
                key = keys.getLast();
            }

            player.sendSystemMessage(Component.literal("<").append(icm.item().getDisplayName()).append(Component.literal("> "))
                    .append(Component.translatable(key, icm.formatArgs.toArray())));
        }

    }

    private static @NotNull List<String> getPossibleTranslationKeys(ItemChatMessage icm, AwakenedItemData aiData) {
        List<PersonalityTrait> traits = aiData.personality();

        List<String> keys = new ArrayList<>();

        keys.add(AIMSG_KEY.formatted(icm.trigger(), "any", traits.get(0).lower() + "-" + traits.get(1).lower()));
        keys.add(AIMSG_KEY.formatted(icm.trigger(), "any", traits.get(1).lower() + "-" + traits.get(0).lower()));
        keys.add(AIMSG_KEY.formatted(icm.trigger(), "any", traits.get(0).lower()));
        keys.add(AIMSG_KEY.formatted(icm.trigger(), "any", traits.get(1).lower()));
        keys.add(AIMSG_KEY.formatted(icm.trigger(), "any", "any"));

        AwakenedItems.LOGGER.debug("tl keys: {}", keys);

        return keys;
    }
}
