package ir.mrsf.antisardard;

import ir.mrsf.antisardard.enums.Settings;
import ir.mrsf.antisardard.listeners.GuildVoiceListener;
import ir.mrsf.antisardard.listeners.SlashCommandListener;
import ir.mrsf.antisardard.utils.BotUtil;
import ir.mrsf.antisardard.utils.DataUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    @Getter
    private static Main instance;

    @SuppressWarnings("InstantiationOfUtilityClass")
    public static void main(String[] args) {
        new Main();
    }

    @SneakyThrows
    public Main() {
        instance = this;
        final File configFile = DataUtil.settingsFile;
        if (!configFile.exists()) {
            if (!configFile.createNewFile()) System.out.println("Khata Dar Sakhtan File Config");
            else {
                System.out.println("Darhal Sakhtan Config File...");
                final FileWriter fileWriter = createConfig(configFile);
                System.out.println("Config generate Shod Lotfan Moshakhasat Ro Por Konid");
                fileWriter.close();
                System.exit(0);
                return;
            }
        }
        final JDA jda = JDABuilder.createDefault(Settings.TOKEN.getAs(String.class))
                .addEventListeners(new GuildVoiceListener())
                .addEventListeners(new SlashCommandListener())
                .build();
        jda.updateCommands().addCommands(
                Commands.slash("reload", "Reload config"),
                Commands.slash("toggle", "toggle off-on")
        ).queue();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            if (!DataUtil.toggle) return;
            for (Guild guild : jda.getGuilds()) {
                boolean done = false;
                for (Object targetId : Settings.TARGETS.getAs(List.class)) {
                    final Member targetMember = guild.getMemberById(Long.parseLong(targetId.toString()));
                    if (targetMember == null) continue;
                    final GuildVoiceState voiceState = targetMember.getVoiceState();
                    if (voiceState == null) continue;
                    if (!voiceState.inAudioChannel()) continue;
                    if (Settings.IGNORE_MUTED.getAs(Boolean.class)) {
                        if (voiceState.isMuted()) continue;
                    }
                    final AudioChannelUnion channel = voiceState.getChannel();
                    if (channel == null) continue;
                    BotUtil.joinVoiceChannel(channel);
                    done = true;
                    break;
                }
                if (!done) {
                    BotUtil.disconnectFromVoiceChannel(guild);
                }
            }
        }, 10, 30, TimeUnit.SECONDS);
    }

    @NotNull
    private static FileWriter createConfig(File configFile) throws IOException {
        final FileWriter fileWriter = new FileWriter(configFile);
        fileWriter.write("""
                Settings:
                  ignore-muted: true
                  admin-id: 78901243644335
                  token: "Mfesef2Nfstyfhfhffsfs6fsefsnd-Ti01KsfsfsgevmpivY_sXKsffesfVFCvfsef"
                  targets: #Target IDS
                    - 12345649556464
                    - 78901243646435
                  volume-threshold: 0.5 #Volume limit
                """);
        return fileWriter;
    }
}
