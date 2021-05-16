package com.m1zark.pokesay.Utils;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
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
		if(name.equalsIgnoreCase("MrMime")) return "Mr. Mime";
		else if(name.equalsIgnoreCase("MimeJr")) return "Mime Jr.";
		else if(name.equalsIgnoreCase("Nidoranfemale")) return "Nidoran&d\u2640&r";
		else if(name.equalsIgnoreCase("Nidoranmale")) return "Nidoran&b\u2642&r";
		else if(name.equalsIgnoreCase("Farfetchd")) return "Farfetch'd";
		else if(name.contains("Alolan")){
			return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(name.replaceAll("\\d+", "")), " ");
		}

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
