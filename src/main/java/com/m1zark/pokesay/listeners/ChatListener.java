package com.m1zark.pokesay.listeners;

import com.m1zark.pokesay.PokeSay;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;

public class ChatListener {
    @Listener
    public void onChatPlayer(MessageChannelEvent.Chat event, @Root Player player) {
        Text msgBody = event.getFormatter().getBody().format();
        int occurrence = StringUtils.countMatches(msgBody.toPlain(), "{pokemon");
        int occurrence2 = StringUtils.countMatches(msgBody.toPlain(), "{poke");

        if ((msgBody.toPlain().contains("{pokemon") || msgBody.toPlain().contains("{poke") || msgBody.toPlain().contains("{item")) && (occurrence <= 6 || occurrence2 <= 6)) {
            event.getFormatter().setBody(PokeSay.getInstance().processPlaceholders(msgBody, PokeSay.getInstance().buildPlaceholders(player)));
        } else {
            event.getFormatter().setBody(msgBody);
        }
    }
}
