package net.runelite.client.plugins.discordfeed;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.HiscoreManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import net.runelite.http.api.hiscore.HiscoreEndpoint;
import net.runelite.http.api.hiscore.HiscoreResult;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

@PluginDescriptor(
        name = "Discord Feed",
        description = "Writes events to a json to be picked up by python",
        enabledByDefault = true
)
@Slf4j

public class DiscordFeed extends Plugin {
    @Inject
    private Client client;

    @Getter(AccessLevel.PACKAGE)
    private Actor enemy;
    private float playerLast = 0f;
    private float enemyLast = 0f;

    @Subscribe
    public void onGameTick(GameTick tick){
        writeHP();
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event)
    {
        if (event.getSource() != client.getLocalPlayer())
        {
            return;
        }

        if(event.getTarget() != null) {
            enemy = event.getTarget();
        }

        writeHP();
    }

    private void writeHP(){
        if(enemy != null){
            int playerCurrent = client.getBoostedSkillLevel(Skill.HITPOINTS);
            int playerMax = client.getRealSkillLevel(Skill.HITPOINTS);
            float playerHP = ((float) playerCurrent / (float) playerMax) * 100f;
            int scale = enemy.getHealth();
            int ratio = enemy.getHealthRatio();
            float enemyHP = ((float) ratio / (float) scale) * 100f;

            if(enemyHP != enemyLast || playerHP != playerLast) {
                String content = "" + (int)(Math.ceil(playerHP)) + "," + (int)(Math.ceil(enemyHP));

                try (FileWriter writer = new FileWriter("C:/staking/staking.txt");
                     BufferedWriter bw = new BufferedWriter(writer)) {
                    bw.write(content);
                } catch (IOException e) {
                    System.err.format("IOException: %s%n", e);
                }
                enemyLast = enemyHP;
                playerLast = playerHP;
            }
        }
    }
}
