package de.delia.starBot.main;

import de.delia.starBot.features.birthday.Birthday;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

public class StarBotListenerAdapter {
    public static List<StarBotListenerAdapter> listeners = new ArrayList<>();

    public static void registerEvent(StarBotListenerAdapter listener) {
        listeners.add(listener);
    }

    public static void unregisterEvent(StarBotListenerAdapter listener) {
        listeners.remove(listener);
    }

    public static void callMemberBirthday(Guild guild, Member member, Birthday birthday) {
        listeners.forEach(listener -> listener.onMemberBirthday(guild, member, birthday));
    }

    public void onMemberBirthday(Guild guild, Member member, Birthday birthday) {

    }
}
