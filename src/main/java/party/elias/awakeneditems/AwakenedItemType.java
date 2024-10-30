package party.elias.awakeneditems;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public enum AwakenedItemType {
    BREAKING_TOOL(5, Set.of(), item ->
            item.canPerformAction(ItemAbilities.PICKAXE_DIG) || item.canPerformAction(ItemAbilities.SHOVEL_DIG)
                    || item.canPerformAction(ItemAbilities.AXE_DIG) || item.canPerformAction(ItemAbilities.HOE_DIG)
                    || item.canPerformAction(ItemAbilities.SHEARS_DIG)
    ),
    MELEE_WEAPON(5, Set.of(), item ->
            item.is(Tags.Items.MELEE_WEAPON_TOOLS)
    ),
    ARMOR(5, Set.of(), item ->
            item.is(Tags.Items.ARMORS)
    );


    private final int specificity;
    private final Set<AwakenedItemType> subTypeOf;
    private final Predicate<ItemStack> predicate; // item is considered this type if this returns true

    AwakenedItemType(int specificity, Set<AwakenedItemType> subTypeOf, Predicate<ItemStack> predicate) {
        this.specificity = specificity;
        this.subTypeOf = subTypeOf;
        this.predicate = predicate;
    }

    public int getSpecificity() {
        return specificity;
    }

    public boolean isSubTypeOf(AwakenedItemType type) {
        return subTypeOf.contains(type);
    }

    public boolean checkItem(ItemStack item) {
        return predicate.test(item);
    }

    public static List<AwakenedItemType> getItemTypes(ItemStack item) {
        List<AwakenedItemType> types = new ArrayList<>();

        for (AwakenedItemType type: values()) {
            if (type.checkItem(item)) {
                types.add(type);
            }
        }

        return types;
    }

    private static boolean emptyPredicate(ItemStack item) {
        return false;
    }
}
