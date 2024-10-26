package ir.mrsf.antisardard.listeners;

import ir.mrsf.antisardard.enums.Settings;
import ir.mrsf.antisardard.utils.DataUtil;
import ir.mrsf.antisardard.utils.TimedHashSet;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AudioListener implements AudioReceiveHandler {
    private final Guild guild;
    private final TimedHashSet<Long> inPunishments;
    private final HashMap<Long, Integer> average;

    public AudioListener(Guild guild) {
        this.guild = guild;
        this.inPunishments = new TimedHashSet<>();
        this.average = new HashMap<>();
    }

    @Override
    public boolean canReceiveUser() {
        return true;
    }

    @Override
    public void handleUserAudio(@NotNull UserAudio userAudio) {
        if (!DataUtil.toggle) return;
        final long id = userAudio.getUser().getIdLong();
        if (!Settings.TARGETS.getAs(List.class).contains(id)) return;
        final double volume = calculateVolume(userAudio.getAudioData(1.0));
        if (!(volume > Settings.VOLUME_THRESHOLD.getAs(Double.class))) {
            if (Settings.MODE.getAs(String.class).equalsIgnoreCase("average")) {
                this.average.remove(id);
            }
            return;
        }
        if (Settings.MODE.getAs(String.class).equalsIgnoreCase("average")) {
            this.average.put(id, this.average.getOrDefault(id, 0) + 1);
            if (this.average.get(id) < 3) return;
        }
        final Member targetMember = guild.getMemberById(id);
        if (targetMember == null) return;
        final GuildVoiceState voiceState = targetMember.getVoiceState();
        if (voiceState == null) return;
        if (!voiceState.inAudioChannel()) return;
        if (this.inPunishments.contains(id)) return;
        guild.kickVoiceMember(targetMember).queue();
        this.inPunishments.add(id, 5, TimeUnit.SECONDS);
    }


    private double calculateVolume(byte[] audioData) {
        int sum = 0;
        for (byte b : audioData) {
            sum += Math.abs(b);
        }
        return (double) sum / audioData.length / 128;
    }

}
