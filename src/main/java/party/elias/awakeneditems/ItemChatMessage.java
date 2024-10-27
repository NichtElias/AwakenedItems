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
import java.util.Collection;
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

    private static final List<List<Integer>> ICM_I18N_PRIORITY_MATRIX = List.of(
            List.of(0, 1, 2),
            List.of(0, 2, 1),
            List.of(1, 0, 2),
            List.of(1, 2, 0),
            List.of(2, 0, 1),
            List.of(2, 1, 0),
            List.of(0, 1),
            List.of(0, 2),
            List.of(1, 0),
            List.of(1, 2),
            List.of(2, 0),
            List.of(2, 1),
            List.of(0),
            List.of(1),
            List.of(2)
    );

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
            String key = null;

            List<PersonalityTrait> traits = aiData.personality();

            for (List<Integer> combination:
                    ICM_I18N_PRIORITY_MATRIX.stream().filter(l -> l.stream().max(Integer::compare).orElse(Integer.MAX_VALUE) < traits.size()).toList()) {

                String traitString = String.join("-",
                        combination.stream().map(traits::get).map(PersonalityTrait::lower).toList());

                String k = AIMSG_KEY.formatted(icm.trigger(), "any", traitString);

                if (I18n.exists(k)) {
                    key = k;
                    break;
                }
            }

            if (key == null) {
                key = AIMSG_KEY.formatted(icm.trigger(), "any", "any");
            }

            if (I18n.exists(key)) {
                int variantCount = checkVariants(key);

                if (variantCount > 0) {
                    int selectedVariant = (int) (Math.random() * (variantCount + 1));

                    if (selectedVariant != 0) {
                        key = key + "." + selectedVariant;
                    }
                }

                player.displayClientMessage(Component.literal("<").append(icm.item().getDisplayName()).append(Component.literal("> "))
                        .append(Component.translatable(key, icm.formatArgs.toArray())), false);
            }
        }

    }
}
