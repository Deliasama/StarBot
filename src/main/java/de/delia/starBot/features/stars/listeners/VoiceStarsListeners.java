package de.delia.starBot.features.stars.listeners;

import com.iwebpp.crypto.TweetNaclFast;
import de.delia.starBot.features.stars.tables.StarProfile;
import de.delia.starBot.features.stars.town.Building;
import de.delia.starBot.features.stars.town.Telescope;
import de.delia.starBot.main.Main;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VoiceStarsListeners extends ListenerAdapter {
    Map<Long, Long> membersInVoice = new HashMap<>();

    @Override
    public void onReady(ReadyEvent event) {
        event.getJDA().getVoiceChannels().forEach(this::updateVoice);

        Main.scheduler.scheduleAtFixedRate(() -> {
            membersInVoice.forEach((m, g) -> {
                StarProfile starProfile = StarProfile.getTable().get(g, m);
                Telescope telescope = (Telescope) Building.loadBuilding(Telescope.class, g, m);
                double multiplier = 1.0;
                if (telescope != null) multiplier+=((double) telescope.getLevel() * 0.5);

                starProfile.addStars((int) Math.round(multiplier));

                System.out.println("someone received stars from voice activity! ID: " + m);
            });
        }, 0, 10, TimeUnit.MINUTES);
    }

    @Override
    public void onGenericGuildVoice(GenericGuildVoiceEvent event) {
        // Voice Activity Stars
        if (event instanceof GuildVoiceUpdateEvent updateEvent) {
            if (updateEvent.getChannelLeft() != null && updateEvent.getChannelJoined() == null) membersInVoice.remove(updateEvent.getMember().getIdLong());

            updateVoice(updateEvent.getChannelLeft());
            updateVoice(updateEvent.getChannelJoined());
        } else updateVoice(event.getVoiceState().getChannel());
    }

    private void updateVoice(AudioChannel channel) {
        if (channel == null) return;

        List<Member> members = channel.getMembers().stream()
                .filter(member -> !member.getUser().isBot())
                .filter(member -> !member.getVoiceState().isMuted())
                .toList();

        if (members.size() >= 2) members.forEach(member -> membersInVoice.put(member.getIdLong(), channel.getGuild().getIdLong()));

        channel.getMembers().forEach(m -> {
            if (!members.contains(m)) membersInVoice.remove(m.getIdLong());
        });
    }
}
