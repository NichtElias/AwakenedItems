package party.elias.awakeneditems.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import party.elias.awakeneditems.AwakenedItems;

@JeiPlugin
public class AIJeiPlugin implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(AwakenedItems.MODID, "main");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (!ModList.get().isLoaded("emi")) {
            registration.addIngredientInfo(AwakenedItems.SOULSTUFF_ITEM, Component.translatable("info.awakeneditems.awakening"));
        }
    }
}
