package party.elias.awakeneditems.ruledb;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record RuleDatabase(ResourceLocation ruleDatabase, Map<String, Rule> rules) {

    public static final Codec<RuleDatabase> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("rule_database").forGetter(RuleDatabase::ruleDatabase),
                    Codec.unboundedMap(Codec.STRING, Rule.CODEC).fieldOf("rules").forGetter(RuleDatabase::rules)
            ).apply(instance, RuleDatabase::new)
    );
}
