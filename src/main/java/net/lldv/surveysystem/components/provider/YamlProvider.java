package net.lldv.surveysystem.components.provider;

import cn.nukkit.utils.Config;
import net.lldv.surveysystem.SurveySystem;
import net.lldv.surveysystem.components.data.Survey;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class YamlProvider extends Provider {

    private Config surveyData;

    @Override
    public void connect(SurveySystem instance) {
        CompletableFuture.runAsync(() -> {
            instance.saveResource("/data/survey_data.yml");
            this.surveyData = new Config(instance.getDataFolder() + "/data/survey_data.yml", Config.YAML);

            this.surveyData.getSection("survey").getAll().getKeys(false).forEach(s -> this.getSurvey(s, survey -> {
                this.surveyMap.put(this.surveyData.getString("survey." + s + ".title"), survey);
            }));

            this.reloadSurveyData();
            instance.getLogger().info("[Configuration] Ready.");
        });
    }

    @Override
    public void createSurvey(String title, String text, long time) {
        String id = this.getRandomIDCode();
        String date = this.getDate();
        List<String> list = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        long end = System.currentTimeMillis() + (time * 3600L) * 1000L;
        this.surveyData.set("survey." + id + ".title", title);
        this.surveyData.set("survey." + id + ".text", text);
        this.surveyData.set("survey." + id + ".date", date);
        this.surveyData.set("survey." + id + ".status", Survey.Status.OPEN.name().toUpperCase());
        this.surveyData.set("survey." + id + ".time", end);
        this.surveyData.set("survey." + id + ".voted", list);
        this.surveyData.save();
        this.surveyData.reload();
        this.surveyMap.put(title, new Survey(title, text, Survey.Status.OPEN, id, end, map));
    }

    @Override
    public void deleteSurvey(String id) {
        this.getSurvey(id, survey -> {
            Map<String, Object> map = this.surveyData.getSection("survey").getAllMap();
            map.remove(id);
            this.surveyData.set("survey", map);
            this.surveyData.save();
            this.surveyData.reload();
            this.surveyMap.remove(survey.getTitle());
        });
    }

    @Override
    public void closeSurvey(String id) {
        this.getSurvey(id, survey -> {
            this.surveyData.set("survey." + id + ".status", Survey.Status.CLOSED.name().toUpperCase());
            this.surveyData.save();
            this.surveyData.reload();
            this.surveyMap.remove(survey.getTitle());
            this.surveyMap.put(survey.getTitle(), new Survey(survey.getTitle(), survey.getText(), Survey.Status.CLOSED, survey.getId(), survey.getTime(), survey.getVotedPlayers()));
        });
    }

    @Override
    public void updateSurvey(String id, String player, boolean vote) {
        int i = 0;
        if (vote) i = 1;
        List<String> list = this.surveyData.getStringList("survey." + id + ".voted");
        list.add(player + ":" + i);
        this.surveyData.set("survey." + id + ".voted", list);
        this.surveyData.save();
        this.surveyData.reload();
        this.getSurvey(id, survey -> {
            this.surveyMap.remove(survey.getTitle());
            this.surveyMap.put(survey.getTitle(), survey);
        });
    }

    @Override
    public void reloadSurveyData() {
        try {
            for (Survey survey : this.surveyMap.values()) {
                if (survey.getStatus() == Survey.Status.OPEN) {
                    if (survey.getTime() < System.currentTimeMillis()) this.closeSurvey(survey.getId());
                }
            }
        } catch (ConcurrentModificationException ignored) {
        }
    }

    @Override
    public void convertToID(String title, Consumer<String> id) {
        String returnId = null;
        for (String s : this.surveyData.getSection("survey").getAll().getKeys(false)) {
            if (this.surveyData.getString("survey." + s + ".title").equals(title)) returnId = s;
        }
        id.accept(returnId);
    }

    @Override
    public void getSurvey(String id, Consumer<Survey> survey) {
        String title = this.surveyData.getString("survey." + id + ".title");
        String text = this.surveyData.getString("survey." + id + ".text");
        Survey.Status status = Survey.Status.valueOf(this.surveyData.getString("survey." + id + ".status"));
        long time = this.surveyData.getLong("survey." + id + ".time");
        Map<String, Integer> map = new HashMap<>();
        this.surveyData.getStringList("survey." + id + ".voted").forEach(e -> map.put(e.split(":")[0], Integer.parseInt(e.split(":")[1])));
        survey.accept(new Survey(title, text, status, id, time, map));
    }

    @Override
    public void surveyExists(String title, Consumer<Boolean> exists) {
        boolean b = false;
        for (String s : this.surveyData.getSection("survey").getAll().getKeys(false)) {
            if (this.surveyData.getString("survey." + s + ".title").equals(title)) b = true;
        }
        exists.accept(b);
    }

    @Override
    public String getProvider() {
        return "Yaml";
    }

}
