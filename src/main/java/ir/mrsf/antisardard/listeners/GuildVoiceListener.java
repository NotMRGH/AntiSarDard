package ir.mrsf.antisardard.listeners;

import ir.mrsf.antisardard.enums.Settings;
import ir.mrsf.antisardard.utils.BotUtil;
import ir.mrsf.antisardard.utils.DataUtil;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuildVoiceListener extends ListenerAdapter {

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (!DataUtil.toggle) return;
        final Member member = event.getEntity();
        final AudioChannel joinedChannel = event.getChannelJoined();

        if (Settings.TARGETS.getAs(List.class).contains(member.getIdLong())) {
            if (joinedChannel != null) {
                final GuildVoiceState voiceState = member.getVoiceState();
                if (voiceState != null) {
                    if (Settings.IGNORE_MUTED.getAs(Boolean.class)) {
                        if (voiceState.isMuted()) return;
                    }
                }
                BotUtil.joinVoiceChannel(joinedChannel);
                return;
            }
        }

        if (event.getChannelLeft() == null) return;
        if (!member.getUser().isBot() || member.getIdLong() != event.getJDA().getSelfUser().getIdLong()) return;
        for (Object targetId : Settings.TARGETS.getAs(List.class)) {
            final Member targetMember = event.getGuild().getMemberById(Long.parseLong(targetId.toString()));
            if (targetMember == null) return;
            final GuildVoiceState voiceState = targetMember.getVoiceState();
            if (voiceState == null) return;
            if (!voiceState.inAudioChannel()) return;
            if (Settings.IGNORE_MUTED.getAs(Boolean.class)) {
                if (voiceState.isMuted()) return;
            }
            final AudioChannelUnion channel = voiceState.getChannel();
            if (channel == null) return;
            BotUtil.joinVoiceChannel(channel);
            break;
        }
    }

    @Override
    public void onGuildVoiceDeafen(@NotNull GuildVoiceDeafenEvent event) {
        if (event.getMember().equals(event.getGuild().getSelfMember())) {
            if (event.getVoiceState().isDeafened()) {
                event.getGuild().getSelfMember().deafen(false).queue();
            }
        }
    }
}
