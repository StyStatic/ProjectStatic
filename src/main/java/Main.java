import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;

public class Main {

    public static JDA jda;
    public static Guild testguild;

    public static void main(String[] args) throws LoginException, InterruptedException {
        jda = JDABuilder
                .createLight(Config.get("TOKEN"))
                .addEventListeners(new EventListener())
                .setActivity(Activity.listening("you sleep"))
                .enableCache(CacheFlag.VOICE_STATE)
                .enableCache(CacheFlag.ONLINE_STATUS)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_PRESENCES)
                .build();

        testguild = jda.awaitReady().getGuildById("700170808626118656");
        testguild.upsertCommand("shutdown", "shuts down the discord bot").queue();
        testguild.upsertCommand("ping", "Returns bot ping").queue();
        testguild.upsertCommand("join", "makes the bot join vc").queue();
        testguild.upsertCommand("play", "plays a song").addOption(OptionType.STRING, "link", "The link for music", true).queue();
        testguild.upsertCommand("stop", "stops the music player").queue();
        testguild.upsertCommand("skip", "skips the current song").queue();
        testguild.upsertCommand("nowplaying", "Returns the current track").queue();
        testguild.upsertCommand("queue", "Returns the first 20 tracks in the queue").queue();
        testguild.upsertCommand("role", "gives user a role").addOption(OptionType.ROLE, "role", "Role ID", true).addOption(OptionType.USER, "adder", "person you add roles to", true).queue();
        testguild.upsertCommand("ban", "bans chris").addOption(OptionType.USER, "bannee", "Person you ban", true).queue();
        testguild.upsertCommand("derole", "removes a role").addOption(OptionType.ROLE, "role", "Role ID", true).addOption(OptionType.USER, "removee", "person you remove roles to", true).queue();
    }
}
