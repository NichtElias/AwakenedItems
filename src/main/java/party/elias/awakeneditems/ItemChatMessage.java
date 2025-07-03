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

import java.util.Comparator;
import java.util.List;

public record ItemChatMessage(ItemStack item, String context, List<Component> formatArgs) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ItemChatMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "item_chat_message"));

    public static final Codec<Component> COMPONENT_CODEC = Codec.STRING.xmap(
            s -> Component.Serializer.fromJson(s, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)),
            c -> Component.Serializer.toJson(c, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY))
    );

    public static final StreamCodec<ByteBuf, Component> COMPONENT_STREAM_CODEC = ByteBufCodecs.fromCodec(COMPONENT_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemChatMessage> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ItemChatMessage::item,
            ByteBufCodecs.STRING_UTF8, ItemChatMessage::context,
            COMPONENT_STREAM_CODEC.apply(ByteBufCodecs.list()), ItemChatMessage::formatArgs,
            ItemChatMessage::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ItemChatMessage icm, IPayloadContext context) {

        sendItemChatMessage(context.player(), icm);

    }

    private static int checkVariants(String key) {
        int variantCount = 0;
        for (int i = 1; I18n.exists(key + "." + i); i++) {
            variantCount = i;
        }
        return variantCount;
    }

    public static void sendItemChatMessage(Player player, ItemChatMessage icm) {

        AwakenedItemData aiData = icm.item().get(AwakenedItems.AWAKENED_ITEM_COMPONENT);

        if (aiData != null) {
            String key = "chat.awakeneditems.aimsg.%s.%s-%s".formatted(icm.context(), aiData.personality().major().lower(), aiData.personality().minor().lower());

            if (I18n.exists(key)) {
                int variantCount = checkVariants(key);

                if (variantCount > 0) {
                    int selectedVariant = (int) (Math.random() * (variantCount + 1));

                    if (selectedVariant != 0) {
                        key = key + "." + selectedVariant;
                    }
                }

                player.sendSystemMessage(Component.literal("<").append(icm.item().getDisplayName()).append(Component.literal("> "))
                        .append(Component.translatable(key, icm.formatArgs.toArray())));
            }
        }

    }
}
