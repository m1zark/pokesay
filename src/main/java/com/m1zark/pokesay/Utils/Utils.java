package com.m1zark.pokesay.Utils;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;

public class Utils {
	public static Text embedColours(String str) {
		return TextSerializers.FORMATTING_CODE.deserialize(str);
	}

	public static String capitalize(String line) {
		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}

	@Nullable public static PlayerPartyStorage getPlayerStorage(Player player) {
		return Pixelmon.storageManager.getParty((EntityPlayerMP) player);
	}


	public static boolean isHiddenAbility(Pokemon p) {
		return p.getAbilitySlot() == 2;
	}

	public static String updatePokemonName(String name){
		if(name.equalsIgnoreCase("MimeJr")) return "Mime Jr.";
		else if(name.equalsIgnoreCase("MrMime")) return "Mr. Mime";
		else if(name.equalsIgnoreCase("MrRime")) return "Mr. Rime";
		else if(name.equalsIgnoreCase("Nidoranfemale")) return "Nidoran&d\u2640&r";
		else if(name.equalsIgnoreCase("Nidoranmale")) return "Nidoran&b\u2642&r";
		else if(name.equalsIgnoreCase("Farfetchd")) return "Farfetch'd";
		else if(name.equalsIgnoreCase("Sirfetchd")) return "Sirfetch'd";
		else if(name.equalsIgnoreCase("Hooh")) return "Ho-Oh";
		else if(name.equalsIgnoreCase("PorygonZ")) return "Porygon-Z";
		else if(name.equalsIgnoreCase("Jangmoo")) return "Jangmo-o";
		else if(name.equalsIgnoreCase("Hakamoo")) return "Hakamo-o";
		else if(name.equalsIgnoreCase("Kommoo")) return "Kommo-o";
		else if(name.equalsIgnoreCase("Flabebe")) return "Flab\u00E9b\u00E9";
		else if(name.equalsIgnoreCase("Tapu_Koko")) return "Tapu Koko";
		else if(name.equalsIgnoreCase("Tapu_Lele")) return "Tapu Lele";
		else if(name.equalsIgnoreCase("Tapu_Bulu")) return "Tapu Bulu";
		else if(name.equalsIgnoreCase("Tapu_Fini")) return "Tapu Fini";
		else if(name.equalsIgnoreCase("TypeNull")) return "Type: Null";

		return name;
	}

	public static String getNatureShorthand(StatsType type) {
		switch (type) {
			case Accuracy: {
				return "Acc";
			}
			case HP: {
				return "HP";
			}
			case Speed: {
				return "Speed";
			}
			case Attack: {
				return "Atk";
			}
			case Defence: {
				return "Def";
			}
			case Evasion: {
				return "Eva";
			}
			case SpecialAttack: {
				return "SpAtk";
			}
			case SpecialDefence: {
				return "SpDef";
			}
			case None: {
				return "None";
			}
		}
		return "";
	}
}
