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
            List<PersonalityTrait> traits = aiData.personality();

            String key0 = AIMSG_KEY.formatted(icm.trigger(), "any", traits.get(0) + "-" + traits.get(1));
            String key1 = AIMSG_KEY.formatted(icm.trigger(), "any", traits.get(0));
            String key2 = AIMSG_KEY.formatted(icm.trigger(), "any", traits.get(1));
            String key3 = AIMSG_KEY.formatted(icm.trigger(), "any", "any");

            String key;

            if (I18n.exists(key0)) {
                key = key0;
            } else if (I18n.exists(key1)) {
                key = key1;
            } else if (I18n.exists(key2)) {
                key = key2;
            } else {
                key = key3;
            }

            player.sendSystemMessage(Component.literal("<").append(icm.item().getDisplayName()).append(Component.literal("> "))
                    .append(Component.translatable(key, icm.formatArgs.toArray())));
        }

    }
}
