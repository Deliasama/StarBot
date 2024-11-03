package de.delia.starBot.menus;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EmbedMenuInstance <T extends CacheableEmbedMenu> {
    private final T embedMenu;
    private final net.dv8tion.jda.api.entities.Member member;
    private final Guild guild;
    private final EmbedBuilder embedBuilder;
    private final List<ActionRow> actionRows;
    private final long timestamp;

    public EmbedMenuInstance(T embedMenu, Member member, Guild guild, EmbedBuilder embedBuilder, List<ActionRow> actionRows, long timestamp) {
        this.embedMenu = embedMenu;
        this.member = member;
        this.guild = guild;
        this.embedBuilder = embedBuilder;
        this.actionRows = actionRows;
        this.timestamp = timestamp;
    }

    public boolean isExpired(long ttl) {
        return System.currentTimeMillis() - timestamp > ttl;
    }
}
