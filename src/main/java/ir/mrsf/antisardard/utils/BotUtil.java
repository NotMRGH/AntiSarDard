package ir.mrsf.antisardard.utils;

import ir.mrsf.antisardard.listeners.AudioListener;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;

@UtilityClass
public class BotUtil {
    public void joinVoiceChannel(AudioChannel channel) {
        try {
            Thread.sleep(100);
            final Guild guild = channel.getGuild();
            final AudioManager audioManager = guild.getAudioManager();
            audioManager.openAudioConnection(channel);
            audioManager.setReceivingHandler(new AudioListener(guild));
            if (!audioManager.isConnected()) return;
            final GuildVoiceState voiceState = guild.getSelfMember().getVoiceState();
            if (voiceState == null) return;
            if (!voiceState.isGuildDeafened()) return;
            guild.getSelfMember().deafen(false).queue();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnectFromVoiceChannel(Guild guild) {
        guild.getAudioManager().closeAudioConnection();
    }
}
