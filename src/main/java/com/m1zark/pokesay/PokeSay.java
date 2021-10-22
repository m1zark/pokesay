package com.m1zark.pokesay;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.m1zark.pokesay.Utils.Utils;
import com.m1zark.pokesay.config.Config;
import com.m1zark.pokesay.listeners.ChatListener;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Stats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.LakeTrioStats;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumSpecial;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import de.waterdu.aquaauras.auras.AuraStorage;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Plugin(id = PSInfo.ID, version = PSInfo.VERSION, name = PSInfo.NAME, description = PSInfo.DESCRIPTION, authors = "m1zark", dependencies={@Dependency(id="pixelmon")})
public class PokeSay {
    @Inject private Logger logger;
    @Inject private PluginContainer pluginContainer;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    private Config config;
    private static PokeSay instance;
    private boolean enabled = true;

    @Listener public void onServerStart(GameInitializationEvent e) {
        instance = this;
        PSInfo.startup();

        this.enabled = PSInfo.dependencyCheck();
        if(this.enabled) {
            this.config = new Config();

            Sponge.getEventManager().registerListeners(this, new ChatListener());
        }
    }

    @Listener public void onReload(GameReloadEvent e) {
        if(this.enabled) {
            this.config = new Config();
            getConsole().ifPresent(console -> console.sendMessages(Text.of(PSInfo.PREFIX, PSInfo.NAME, " configurations have been reloaded")));
        }
    }

    public Map<String, Text> buildPlaceholders(Player player) {
        Map<String, Text> placeholders = new HashMap<>();

        // POKEMON PLACEHOLDERS
        // {pokemon#slot} for party slot
        if (this.enabled) {
            PlayerPartyStorage storage = Utils.getPlayerStorage(player);

            if (storage != null) {
                try {
                    for (Pokemon pokemon : storage.getTeam()) {
                        if (pokemon == null) continue;
                        String key = "{pokemon" + (storage.getSlot(pokemon) + 1) + "}";
                        placeholders.put(key, buildPokemonStats(pokemon));

                        String key2 = "{poke" + (storage.getSlot(pokemon) + 1) + "}";
                        placeholders.put(key2, this.buildPokemonStats(pokemon));

                        String key3 = "{p" + (storage.getSlot(pokemon) + 1) + "}";
                        placeholders.put(key3, this.buildPokemonStats(pokemon));
                    }
                } catch (Exception partyLength) {
                    //
                }
            }
        }

        // ITEM PLACEHOLDERS
        // {item} for mainhand, {item#} for slot numbers
        player.getItemInHand(HandTypes.MAIN_HAND).ifPresent(itemStack -> {
            if(itemStack.getType() != ItemTypes.AIR || !itemStack.isEmpty()) {
                if(!isBlacklisted(itemStack)) placeholders.put("{item}", buildItemName(itemStack));
            }
        });

        int i = 1;
        Hotbar playerHotbar = player.getInventory().query(Hotbar.class);
        Iterator<Inventory> hotBarIterator = playerHotbar.slots().iterator();

        while (hotBarIterator.hasNext()) {
            Slot hotBarslot = (Slot) hotBarIterator.next();

            Optional<ItemStack> itemStackOptional = hotBarslot.peek();
            if (itemStackOptional.isPresent() && itemStackOptional.get().getType() != ItemTypes.AIR && !itemStackOptional.get().isEmpty() && !isBlacklisted(itemStackOptional.get())) {
                String key = "{item" + i + "}";
                placeholders.put(key, buildItemName(itemStackOptional.get()));
            }
            i++;
        }

        return placeholders;
    }

    public Text processPlaceholders(Text msg, Map<String, Text> placeholders) {
        if (!msg.getChildren().isEmpty()) {
            msg = msg.toBuilder().removeAll().append(msg.getChildren().stream().map(child -> processPlaceholders(child, placeholders)).collect(Collectors.toList())).build();
        }

        String plainMsg = msg.toPlain();
        for (String placeholder : placeholders.keySet()) {
            int matches = StringUtils.countMatches(plainMsg, placeholder);

            if (matches != 0) {
                String[] splitMessage = plainMsg.split(Pattern.quote(placeholder));
                Text.Builder finalMsgBuilder = Text.builder();
                for (int i = 0; i < splitMessage.length; i++) {
                    finalMsgBuilder.append(Text.of(splitMessage[i]));
                    if (matches > 0) {
                        finalMsgBuilder.append(placeholders.get(placeholder));
                        matches--;
                    }
                }

                while (matches > 0) {
                    finalMsgBuilder.append(placeholders.get(placeholder));
                    matches--;
                }

                msg = finalMsgBuilder.style(msg.getStyle()).color(msg.getColor()).build();
                return processPlaceholders(msg, placeholders);
            }
        }

        return msg;
    }

    private Text buildPokemonStats(Pokemon pokemon) {
        String heldItem;
        String customTexture;
        String displayName = Utils.updatePokemonName(pokemon.getSpecies().name());
        String pokerus = pokemon.getPokerus() != null ? (pokemon.getPokerus().canInfect() ? "&d[PKRS] " : "&7&m[PKRS] ") : "";
        boolean isTrio = false;
        Stats stats = pokemon.getStats();
        Gender gender = pokemon.getGender();
        EnumNature nature = pokemon.getMintNature() != null ? pokemon.getMintNature() : pokemon.getBaseNature();
        String natureColor = pokemon.getMintNature() != null ? "&3" : "";
        EVStore eVsStore = null;
        IVStore ivStore = null;
        boolean wasHyperTrained = false;
        String[] ht = new String[]{"","","","","",""};

        String formName = "";
        if (!(pokemon.getFormEnum().equals(EnumSpecial.Base) || pokemon.getFormEnum().getLocalizedName().equals("None") || pokemon.getFormEnum().getLocalizedName().equals("Standard") || pokemon.getFormEnum().getLocalizedName().equals("Normal"))) {
            formName = pokemon.getFormEnum().getLocalizedName();
        }

        if (!Strings.isNullOrEmpty((customTexture = pokemon.getCustomTexture()))) {
            for (String custom : Config.getCustomTextures()) {
                String[] texture = custom.split(",");
                if (!texture[0].equalsIgnoreCase(customTexture)) continue;
                customTexture = texture[1];
            }
        }

        switch (pokemon.getSpecies()) {
            case Mesprit:
            case Azelf:
            case Uxie:
                isTrio = true;
        }

        int ivSum = 0;
        int evSum = 0;
        Moveset moveset = pokemon.getMoveset();

        heldItem = !pokemon.getHeldItem().getDisplayName().equalsIgnoreCase("Air") ? pokemon.getHeldItem().getDisplayName() : "Nothing";

        if (stats != null) {
            eVsStore = stats.evs;
            ivStore = stats.ivs;

            ivSum = ivStore.getStat(StatsType.HP) + ivStore.getStat(StatsType.Attack) + ivStore.getStat(StatsType.Defence) + ivStore.getStat(StatsType.SpecialAttack) + ivStore.getStat(StatsType.SpecialDefence) + ivStore.getStat(StatsType.Speed);
            evSum = eVsStore.getStat(StatsType.HP) + eVsStore.getStat(StatsType.Attack) + eVsStore.getStat(StatsType.Defence) + eVsStore.getStat(StatsType.SpecialAttack) + eVsStore.getStat(StatsType.SpecialDefence) + eVsStore.getStat(StatsType.Speed);

            StatsType[] stat = new StatsType[]{StatsType.HP, StatsType.Attack, StatsType.Defence, StatsType.SpecialAttack, StatsType.SpecialDefence, StatsType.Speed};

            for(int i = 0; i < stat.length; ++i) {
                if (ivStore.isHyperTrained(stat[i])) {
                    ht[i] = "&3";
                    wasHyperTrained = true;
                }
            }
        }

        String pokeGender;
        if (gender.toString().equals("Female")) pokeGender = "&d" + gender.toString() + " \u2640";
        else if(gender.toString().equals("Male")) pokeGender = "&b" + gender.toString() + " \u2642";
        else pokeGender = "&8Genderless \u26A5";

        ArrayList<String> moves = new ArrayList<>();
        moves.add((moveset.get(0)==null) ? "&bNone" : "&b"+moveset.get(0).getActualMove().getAttackName());
        moves.add((moveset.get(1)==null) ? "&bNone" : "&b"+moveset.get(1).getActualMove().getAttackName());
        moves.add((moveset.get(2)==null) ? "&bNone" : "&b"+moveset.get(2).getActualMove().getAttackName());
        moves.add((moveset.get(3)==null) ? "&bNone" : "&b"+moveset.get(3).getActualMove().getAttackName());

        List<String> PokemonAuras = new ArrayList<>();
        AuraStorage auras = new AuraStorage(pokemon.getPersistentData());
        if(auras.hasAuras()) {
            auras.getAuras().forEach(aura -> {
                if(aura.isEnabled()) PokemonAuras.add(aura.getAuraDefinition().getDisplayName() + " " + aura.getEffectDefinition().getDisplayName());
            });
        }

        DecimalFormat df = new DecimalFormat("#0.##");
        int numEnchants = 0;
        try {
            if (pokemon.getExtraStats() != null && pokemon.getExtraStats() instanceof LakeTrioStats) {
                LakeTrioStats extra = (LakeTrioStats)pokemon.getExtraStats();
                numEnchants = PixelmonConfig.lakeTrioMaxEnchants - extra.numEnchanted;
            }
        }
        catch (Exception extra) {
            // empty catch block
        }

        TextColor nameColor = TextColors.DARK_AQUA;
        String pokeName = "&3" + displayName;

        if(auras.hasAuras()) {
            nameColor = TextColors.AQUA; pokeName = "&b" + displayName;
        }
        if(pokemon.isShiny() && !pokemon.isEgg()) {
            nameColor = TextColors.GOLD; pokeName = "&6" + displayName;
        }
        if(EnumSpecies.legendaries.contains(pokemon.getSpecies().name)) {
            nameColor = TextColors.LIGHT_PURPLE; pokeName = "&d" + displayName;
        }
        if(EnumSpecies.ultrabeasts.contains(pokemon.getSpecies().name)) {
            nameColor = TextColors.DARK_GREEN; pokeName = "&2" + displayName;
        }
        if(!Strings.isNullOrEmpty(customTexture)) {
            nameColor = TextColors.RED; pokeName = "&c" + displayName;
        }

        String pokeStats = pokerus + pokeName + " &7| &eLvl " + pokemon.getLevel() + " " + ((pokemon.isShiny()) ? "&7(&6Shiny&7)&r " : "") + "\n&r" +
                (new PokemonSpec("untradeable").matches(pokemon) ? "&4Untradeable" + "\n&r" : "") +
                (new PokemonSpec("unbreedable").matches(pokemon) ? "&4Unbreedable" + "\n&r" : "") +

                (!PokemonAuras.isEmpty() ? "&7Aura 1: " + PokemonAuras.get(0) + "\n&r" : "") +
                (auras.aurasEnabled() > 1 ? "&7Aura 2: " + PokemonAuras.get(1) + "\n&r" : "") +

				(pokemon.hasGigantamaxFactor() ? "&cGigantamax Potential" + "\n&r": "") +
                (pokemon.getDynamaxLevel() > 0 ? "&7Dynamax Level: &d" + pokemon.getDynamaxLevel() + "\n&r": "") +

                (!formName.isEmpty() ? "&7Form: &e" + Utils.capitalize(formName) + "\n&r" : "") +
                (isTrio ? "&7Ruby Enchant: &e" + (numEnchants != 0 ? numEnchants + " Available" : "None Available") + "\n&r" : "") +
                (!Strings.isNullOrEmpty(customTexture) ? "&7Custom Texture: &e" + Utils.capitalize(customTexture) + "\n&r" : "") +
                (!pokemon.getHeldItem().getDisplayName().equalsIgnoreCase("Air") ? "&7Held Item: &e" + heldItem + "\n&r" : "") +
                "&7Ability: &e" + pokemon.getAbility().getName() + ((Utils.isHiddenAbility(pokemon)) ? " &7(&6HA&7)&r" : "") + "\n&r" +
                "&7Nature: &e" + natureColor + nature.name() + " &7(&a+" + Utils.getNatureShorthand(nature.increasedStat) + " &7| &c-" + Utils.getNatureShorthand(nature.decreasedStat) + "&7)" + "\n&r" +
                "&7Gender: " + pokeGender + "\n&r" +
                "&7Size: &e" + pokemon.getGrowth().name() + "\n&r" +
                "&7Happiness: &e" + pokemon.getFriendship() + "\n&r" +
                "&7Hidden Power: &e" + HiddenPower.getHiddenPowerType(pokemon.getStats().ivs).getLocalizedName() + "\n&r" +
                "&7Caught Ball: &e" + pokemon.getCaughtBall().getItem().getLocalizedName() + "\n\n&r" +

                "&7IVs: &e" + ivSum + "&7/&e186 &7(&a" + df.format((int)(((double)ivSum/186)*100)) + "%&7) \n"
                + "&cHP: " + ht[0] + ivStore.getStat(StatsType.HP) + " &7/ " + "&6Atk: " + ht[1] + ivStore.getStat(StatsType.Attack) + " &7/ " + "&eDef: " + ht[2] + ivStore.getStat(StatsType.Defence) + "\n"
                + "&9SpA: " + ht[3] + ivStore.getStat(StatsType.SpecialAttack) + " &7/ " + "&aSpD: " + ht[4] + ivStore.getStat(StatsType.SpecialDefence) + " &7/ " + "&dSpe: " + ht[5] + ivStore.getStat(StatsType.Speed) + "\n" +

                "&7EVs: &e" + evSum + "&7/&e510 &7(&a" + df.format((int)(((double)evSum/510)*100)) + "%&7) \n"
                + "&cHP: " + eVsStore.getStat(StatsType.HP) + " &7/ " + "&6Atk: " + eVsStore.getStat(StatsType.Attack) + " &7/ " + "&eDef: " + eVsStore.getStat(StatsType.Defence) + "\n"
                + "&9SpA: " + eVsStore.getStat(StatsType.SpecialAttack) + " &7/ " + "&aSpD: " + eVsStore.getStat(StatsType.SpecialDefence) + " &7/ " + "&dSpe: " + eVsStore.getStat(StatsType.Speed) + "\n\n" +

                "&7Moves:\n" + moves.get(0) + " &7- " + moves.get(1) + "\n" + moves.get(2) + " &7- " + moves.get(3);

        if(!pokemon.isEgg()) {
            return Text.builder().color(nameColor)
                    .append(Text.of(Utils.embedColours("[" + displayName + "]")))
                    .onHover(TextActions.showText(Text.of(Utils.embedColours(pokeStats))
                    )).build();
        }else if(pokemon.isInRanch()) {
            return Text.builder().color(nameColor)
                    .append(Text.of(Utils.embedColours("&c[Bugged Pokemon]")))
                    .onHover(TextActions.showText(Text.of(Utils.embedColours("&7This Pokemon is stuck in a ranch block... help it out!"))
                    )).build();
        }else {
            return Text.builder().color(nameColor)
                    .append(Text.of(Utils.embedColours("[Pok\u00E9mon Egg]")))
                    .onHover(TextActions.showText(Text.of(Utils.embedColours("&7Wait til it hatches first..."))
                    )).build();
        }
    }

    private Text buildItemName(ItemStack itemStack) {
        Text displayName;
        TextColor itemColor;

        Optional<Text> displayNameOptional = itemStack.get(Keys.DISPLAY_NAME);

        // If the item has a display name, we'll use that
        if (displayNameOptional.isPresent()) {
            displayName = displayNameOptional.get();
            itemColor = displayName.getColor();

            if (!displayName.getChildren().isEmpty()) {
                itemColor = displayName.getChildren().get(0).getColor();
            }
        } else { // Just grab the item name
            displayName = Text.of(itemStack.getTranslation());
            itemColor = displayName.getColor();

            // Color the item aqua if it has an enchantment
            if(itemStack.get(Keys.ITEM_ENCHANTMENTS).isPresent()) itemColor = TextColors.AQUA;
        }

        // Build the item text with the color
        return Text.builder().color(itemColor)
                .append(Text.of(TextColors.GRAY,"["), displayName, Text.of(TextColors.GRAY,"]"))
                .onHover(TextActions.showItem(itemStack.createSnapshot())).build();
    }

    public static PokeSay getInstance() { return instance; }

    public Optional<ConsoleSource> getConsole() {
        return Optional.ofNullable(Sponge.isServerAvailable() ? Sponge.getServer().getConsole() : null);
    }

    private boolean isBlacklisted(ItemStack item) {
        for(String s : Config.getBlacklist()) {
            String[] check = s.trim().split("\\s*,\\s*");

            if(check[0].equals(item.getType().getId())) {
                if(check.length > 1) {
                    return check[1].equals(item.toContainer().getString(DataQuery.of("UnsafeDamage")).get());
                }
                return true;
            }
        }

        return false;
    }
}