package ir.mrsf.antisardard.listeners;

import ir.mrsf.antisardard.enums.Settings;
import ir.mrsf.antisardard.utils.DataUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;

public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        final long userId = event.getUser().getIdLong();

        if (userId != Settings.ADMIN_ID.getAs(Long.class)) {
            event.reply("Shoma Dastresi Nadarid.").setEphemeral(true).queue();
            return;
        }

        switch (event.getName()) {
            case "reload" -> {
                DataUtil.settingsFile = new File("settings.yml");
                for (Settings val : Settings.values()) {
                    val.reload();
                }
                event.reply("Reloaded").setEphemeral(true).queue();
            }
            case "toggle" -> {
                DataUtil.toggle = !DataUtil.toggle;
                event.reply("Toggle: " + DataUtil.toggle).setEphemeral(true).queue();
            }
            default -> event.reply("Error!").setEphemeral(true).queue();
        }
    }
}
