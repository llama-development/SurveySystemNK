package net.lldv.surveysystem.components.provider;

import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.network.protocol.PlaySoundPacket;
import net.lldv.surveysystem.SurveySystem;
import net.lldv.surveysystem.components.data.Survey;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class Provider {

    public final Map<String, Survey> surveyMap = new HashMap<>();

    public void connect(SurveySystem instance) {

    }

    public void disconnect(SurveySystem instance) {

    }

    public void createSurvey(String title, String text, long time) {

    }

    public void deleteSurvey(String id) {

    }

    public void closeSurvey(String id) {

    }

    public void updateSurvey(String id, String player, boolean vote) {

    }

    public void reloadSurveyData() {

    }

    public void convertToID(String title, Consumer<String> id) {

    }

    public void getSurvey(String id, Consumer<Survey> survey) {

    }

    public void surveyExists(String title, Consumer<Boolean> exists) {

    }

    public String getProvider() {
        return null;
    }

    public String getRandomIDCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder stringBuilder = new StringBuilder();
        Random rnd = new Random();
        while (stringBuilder.length() < 6) {
            int index = (int) (rnd.nextFloat() * chars.length());
            stringBuilder.append(chars.charAt(index));
        }
        return stringBuilder.toString();
    }

    public String getDate() {
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
        return dateFormat.format(now);
    }

    public void playSound(Player player, Sound sound) {
        PlaySoundPacket packet = new PlaySoundPacket();
        packet.name = sound.getSound();
        packet.x = new Double(player.getLocation().getX()).intValue();
        packet.y = (new Double(player.getLocation().getY())).intValue();
        packet.z = (new Double(player.getLocation().getZ())).intValue();
        packet.volume = 1.0F;
        packet.pitch = 1.0F;
        player.dataPacket(packet);
    }

}
