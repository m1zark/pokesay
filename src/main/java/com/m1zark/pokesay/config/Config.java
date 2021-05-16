package com.m1zark.pokesay.config;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.m1zark.pokesay.PokeSay;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static CommentedConfigurationNode main;

    public Config() {
        this.loadConfig();
    }

    private void loadConfig(){
        Path configFile = Paths.get(PokeSay.getInstance().getConfigDir() + "/auras.conf");

        loader = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            if (!Files.exists(PokeSay.getInstance().getConfigDir())) Files.createDirectory(PokeSay.getInstance().getConfigDir());

            if (!Files.exists(configFile)) Files.createFile(configFile);

            if (main == null) {
                main = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            }

            CommentedConfigurationNode general = main.getNode("General");
            general.getNode("Auras").getList(TypeToken.of(String.class), Lists.newArrayList("angelic,&fAngelic","apocalyptic,&8Apocalyptic"));
            general.getNode("CustomTextures").getList(TypeToken.of(String.class), Lists.newArrayList("event,&eEvent"));
            general.getNode("Items_Blacklist").getList(TypeToken.of(String.class), Lists.newArrayList("minecraft:purple_shulker_box"));

            loader.save(main);
        } catch (ObjectMappingException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        try {
            loader.save(main);
        } catch (IOException var1) {
            var1.printStackTrace();
        }
    }

    public void reload() {
        try {
            main = loader.load();
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    public static List<String> getAura() {
        try {
            return main.getNode("General","Auras").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            return Lists.newArrayList();
        }
    }

    public static List<String> getCustomTextures() {
        try {
            return main.getNode("General","CustomTextures").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            return Lists.newArrayList();
        }
    }

    public static List<String> getBlacklist() {
        try {
            return main.getNode("General","Items_Blacklist").getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            return Lists.newArrayList();
        }
    }
}
