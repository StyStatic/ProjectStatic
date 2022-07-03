import LavaPlayer.GuildMusicManager;
import LavaPlayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class EventListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventListener.class);

    public EventListener() throws InterruptedException {
    }

    private boolean isUrl(String link) {
        try {
            new URI(link);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private String formatTime(long timeInMillis) {
        final long hours = timeInMillis / TimeUnit.HOURS.toMillis(1);
        final long minutes = timeInMillis / TimeUnit.MINUTES.toMillis(1);
        final long seconds = timeInMillis % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        LOGGER.info("{} is ready", event.getJDA().getSelfUser().getAsTag());
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        switch (event.getName()) {
            case "shutdown":
                if (event.getUser().getId().equals(Config.get("OWNER_ID"))) {
                    LOGGER.info("Shutting down...");
                    event.getJDA().shutdownNow();
                }
                break;
            case "ping":
                event.deferReply().queue();
                event.getJDA().getRestPing().queue( (time)  ->
                        event.getHook().sendMessageFormat("Ping: %d ms", time).queue()
                );
                break;
            case "join":
                event.deferReply().queue();
                final MessageChannel channel = event.getChannel();
                final Member self = event.getGuild().getSelfMember();
                final GuildVoiceState selfVoiceState = self.getVoiceState();

                if (selfVoiceState.inVoiceChannel()) {
                    event.getHook().sendMessage("I'm already in a voice channel").queue();
                    return;
                }

                final Member member = event.getMember();
                final GuildVoiceState memberVoiceState = member.getVoiceState();

                if (memberVoiceState.getChannel() == null){
                    event.getHook().sendMessage("You need to be in a voice channel for this command to work").queue();
                    return;
                }

                final AudioManager audioManager = event.getGuild().getAudioManager();
                final VoiceChannel voiceChannel = memberVoiceState.getChannel();

                if (!self.hasPermission(voiceChannel, Permission.VOICE_CONNECT)) {
                    event.getHook().sendMessage("The bot can't connect to this channel").queue();
                    return;
                }

                audioManager.openAudioConnection(voiceChannel);
                event.getHook().sendMessageFormat("Connecting to `%s`", voiceChannel.getName()).queue();
                break;
            case "play":
                event.deferReply().queue();

                if (event.getOptions().isEmpty()) {
                    event.getHook().sendMessage("Correct usage is `/play <Youtube Link>`").queue();
                    return;
                }

                final Member self1 = event.getGuild().getSelfMember();
                final GuildVoiceState selfVoiceState1 = self1.getVoiceState();

                if (!selfVoiceState1.inVoiceChannel()) {
                    event.getHook().sendMessage("You need to be in a voice channel for this command to work").queue();
                    return;
                }

                final Member member1 = event.getMember();
                final GuildVoiceState memberVoiceState1 = member1.getVoiceState();

                if (!memberVoiceState1.inVoiceChannel()) {
                    event.getHook().sendMessage("You need to be in a voice channel for this command to work").queue();
                    return;
                }

                if (!memberVoiceState1.getChannel().equals(selfVoiceState1.getChannel())) {
                    event.getHook().sendMessage("You need to be in the same voice channel as me for this command to work").queue();
                    return;
                }

                final OptionMapping optionMapping = event.getOption("link");
                if (optionMapping == null) {
                    event.getHook().sendMessage("For some reason a link was not provided").queue();
                    return;
                }

                String link = optionMapping.getAsString();

                if (!isUrl(link)) {
                    link = "ytsearch:" + link;
                }

                PlayerManager.getInstance().loadAndPlay(event, link);
                break;
            case "stop":
                event.deferReply().queue();

                final Member self2 = event.getGuild().getSelfMember();
                final GuildVoiceState selfVoiceState2 = self2.getVoiceState();

                if (!selfVoiceState2.inVoiceChannel()) {
                    event.getHook().sendMessage("You need to be in a voice channel for this command to work").queue();
                    return;
                }

                final Member member2 = event.getMember();
                final GuildVoiceState memberVoiceState2 = member2.getVoiceState();

                if (!memberVoiceState2.inVoiceChannel()) {
                    event.getHook().sendMessage("You need to be in a voice channel for this command to work").queue();
                    return;
                }

                if (!memberVoiceState2.getChannel().equals(selfVoiceState2.getChannel())) {
                    event.getHook().sendMessage("You need to be in the same voice channel as me for this command to work").queue();
                    return;
                }

                final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());

                musicManager.scheduler.player.stopTrack();
                musicManager.scheduler.queue.clear();

                event.getHook().sendMessage("The player has been stopped and the queue has been cleared").queue();
                break;
            case "skip":
                event.deferReply().queue();

                final Member self3 = event.getGuild().getSelfMember();
                final GuildVoiceState selfVoiceState3 = self3.getVoiceState();

                if (!selfVoiceState3.inVoiceChannel()) {
                    event.getHook().sendMessage("You need to be in a voice channel for this command to work").queue();
                    return;
                }

                final Member member3 = event.getMember();
                final GuildVoiceState memberVoiceState3 = member3.getVoiceState();

                if (!memberVoiceState3.inVoiceChannel()) {
                    event.getHook().sendMessage("You need to be in a voice channel for this command to work").queue();
                    return;
                }

                if (!memberVoiceState3.getChannel().equals(selfVoiceState3.getChannel())) {
                    event.getHook().sendMessage("You need to be in the same voice channel as me for this command to work").queue();
                    return;
                }

                final GuildMusicManager musicManager2 = PlayerManager.getInstance().getMusicManager(event.getGuild());
                final AudioPlayer audioPlayer = musicManager2.audioPlayer;

                if (audioPlayer.getPlayingTrack() == null) {
                    event.getHook().sendMessage("There is no track playing currently").queue();
                    return;
                }

                musicManager2.scheduler.nextTrack();
                event.getHook().sendMessage("Skipped the current track").queue();
                break;
            case "nowplaying":
                event.deferReply().queue();

                final Member self4 = event.getGuild().getSelfMember();
                final GuildVoiceState selfVoiceState4 = self4.getVoiceState();

                if (!selfVoiceState4.inVoiceChannel()) {
                    event.getHook().sendMessage("You need to be in a voice channel for this command to work").queue();
                    return;
                }

                final Member member4 = event.getMember();
                final GuildVoiceState memberVoiceState4 = member4.getVoiceState();

                if (!memberVoiceState4.inVoiceChannel()) {
                    event.getHook().sendMessage("You need to be in a voice channel for this command to work").queue();
                    return;
                }

                if (!memberVoiceState4.getChannel().equals(selfVoiceState4.getChannel())) {
                    event.getHook().sendMessage("You need to be in the same voice channel as me for this command to work").queue();
                    return;
                }

                final GuildMusicManager musicManager3 = PlayerManager.getInstance().getMusicManager(event.getGuild());
                final AudioPlayer audioPlayer1 = musicManager3.audioPlayer;
                final AudioTrack track = audioPlayer1.getPlayingTrack();

                if (track == null) {
                    event.getHook().sendMessage("There is no track playing currently").queue();
                    return;
                }
                final AudioTrackInfo info = track.getInfo();
                event.getHook().sendMessageFormat("Now playing `%s` by `%s` (Link: <%s>)", info.title, info.author, info.uri).queue();
                break;
            case "queue":
                event.deferReply().queue();
                final GuildMusicManager musicManager1 = PlayerManager.getInstance().getMusicManager(event.getGuild());
                final BlockingQueue<AudioTrack> queue = musicManager1.scheduler.queue;

                if (queue.isEmpty()) {
                    event.getHook().sendMessage("The queue is currently empty").queue();
                    return;
                }

                final int trackCount = Math.min(queue.size(), 20);
                final List<AudioTrack> trackList = new ArrayList<>(queue);
                String message = "**Current Queue:**\n";

                for (int i = 0; i < trackCount; i++) {
                    final AudioTrack track1 = trackList.get(i);
                    final AudioTrackInfo info1 = track1.getInfo();
                    message = message + String.valueOf(i+1)
                            + " `"
                            + info1.title
                            + "` by `"
                            + info1.author
                            + "` [`"
                            + formatTime(track1.getDuration())
                            + "`]\n";
                }

                if (trackList.size() > trackCount) {
                    message = message
                            + "And `"
                            + String.valueOf(trackList.size() - trackCount)
                            + "` more...";

                }
                event.getHook().sendMessage(message).queue();
                break;
            case "role":
                if (event.getUser().getId().equals(Config.get("OWNER_ID"))) {
                    if (event.getOptions().isEmpty()) {
                        event.getHook().sendMessage("Correct usage is `/role <id>`").queue();
                        return;
                    }

                    final OptionMapping optionMapping2 = event.getOption("role");
                    final OptionMapping optionMapping4 = event.getOption("adder");

                    event.getGuild().addRoleToMember(event.getGuild().getMemberById(optionMapping4.getAsUser().getId()), optionMapping2.getAsRole()).queue();
                    event.getHook().sendMessage("Applying role: " + optionMapping2.getAsRole().getName());
                } else {
                    event.getHook().sendMessage("Shut up cracker");
                }
                break;
            case "ban":
                event.deferReply();
                final OptionMapping optionMapping3 = event.getOption("bannee");
                if (event.getUser().getId().equals(Config.get("OWNER_ID"))) {
                    event.getGuild().ban(optionMapping3.getAsUser(), 0).queue();
                }
                break;
            case "derole":
                if (event.getUser().getId().equals(Config.get("OWNER_ID"))) {
                    if (event.getOptions().isEmpty()) {
                        event.getHook().sendMessage("Correct usage is `/role <id>`").queue();
                        return;
                    }

                    final OptionMapping optionMapping2 = event.getOption("role");
                    final OptionMapping optionMapping4 = event.getOption("removee");

                    event.getGuild().removeRoleFromMember(event.getGuild().getMemberById(optionMapping4.getAsUser().getId()), optionMapping2.getAsRole()).queue();
                    event.getHook().sendMessage("Removing role: " + optionMapping2.getAsRole().getName());
                } else {
                    event.getHook().sendMessage("Shut up cracker");
                }
                break;
        }
    }
}
