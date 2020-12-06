package net.lldv.surveysystem.components.provider;

import net.lldv.surveysystem.SurveySystem;
import net.lldv.surveysystem.components.data.Survey;
import net.lldv.surveysystem.components.simplesqlclient.MySqlClient;
import net.lldv.surveysystem.components.simplesqlclient.objects.SqlColumn;
import net.lldv.surveysystem.components.simplesqlclient.objects.SqlDocument;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MySqlProvider extends Provider {

    private MySqlClient client;

    @Override
    public void connect(SurveySystem instance) {
        CompletableFuture.runAsync(() -> {
            try {
                this.client = new MySqlClient(
                        instance.getConfig().getString("MySql.Host"),
                        instance.getConfig().getString("MySql.Port"),
                        instance.getConfig().getString("MySql.User"),
                        instance.getConfig().getString("MySql.Password"),
                        instance.getConfig().getString("MySql.Database")
                );

                this.client.createTable("survey_data", "id",
                        new SqlColumn("id", SqlColumn.Type.VARCHAR, 16)
                                .append("title", SqlColumn.Type.VARCHAR, 128)
                                .append("text", SqlColumn.Type.VARCHAR, 256)
                                .append("date", SqlColumn.Type.VARCHAR, 64)
                                .append("status", SqlColumn.Type.VARCHAR, 16)
                                .append("time", SqlColumn.Type.LONG)
                                .append("voted", SqlColumn.Type.LONGTEXT));

                this.client.find("survey_data").getAll().forEach(sqlDocument -> this.getSurvey(sqlDocument.getString("id"), survey -> {
                    this.surveyMap.put(sqlDocument.getString("title"), survey);
                }));

                this.reloadSurveyData();
                instance.getLogger().info("[MySqlClient] Connection opened.");
            } catch (Exception e) {
                e.printStackTrace();
                instance.getLogger().info("[MySqlClient] Failed to connect to database.");
            }
        });
    }

    @Override
    public void disconnect(SurveySystem instance) {
        instance.getLogger().info("[MySqlClient] Connection closed.");
    }

    @Override
    public void createSurvey(String title, String text, long time) {
        CompletableFuture.runAsync(() -> {
            String id = this.getRandomIDCode();
            String date = this.getDate();
            Map<String, Integer> map = new HashMap<>();
            long end = System.currentTimeMillis() + (time * 3600L) * 1000L;
            this.client.insert("survey_data", new SqlDocument("id", id)
                    .append("title", title)
                    .append("text", text)
                    .append("date", date)
                    .append("status", Survey.Status.OPEN.name().toUpperCase())
                    .append("time", end)
                    .append("voted", ""));
            this.surveyMap.put(title, new Survey(title, text, Survey.Status.OPEN, id, end, map));
        });
    }

    @Override
    public void deleteSurvey(String id) {
        CompletableFuture.runAsync(() -> this.getSurvey(id, survey -> {
            this.client.delete("survey_data", "id", id);
            this.surveyMap.remove(survey.getTitle());
        }));
    }

    @Override
    public void closeSurvey(String id) {
        this.getSurvey(id, survey -> {
            this.client.update("survey_data", "id", id, new SqlDocument("status", Survey.Status.CLOSED.name().toUpperCase()));
            this.surveyMap.remove(survey.getTitle());
            this.surveyMap.put(survey.getTitle(), new Survey(survey.getTitle(), survey.getText(), Survey.Status.CLOSED, survey.getId(), survey.getTime(), survey.getVotedPlayers()));
        });
    }

    @Override
    public void updateSurvey(String id, String player, boolean vote) {
        CompletableFuture.runAsync(() -> {
            int i = 0;
            if (vote) i = 1;
            SqlDocument document = this.client.find("survey_data", "id", id).first();
            if (document != null) {
                String voted = document.getString("voted");
                this.client.update("survey_data", "id", id, new SqlDocument("voted", voted + player + ":" + i + "#"));
            }
        });
    }

    @Override
    public void reloadSurveyData() {
        this.surveyMap.values().forEach(survey -> {
            if (survey.getStatus() == Survey.Status.OPEN) {
                if (survey.getTime() < System.currentTimeMillis()) this.closeSurvey(survey.getId());
            }
        });
    }

    @Override
    public void convertToID(String title, Consumer<String> id) {
        CompletableFuture.runAsync(() -> {
            SqlDocument document = this.client.find("survey_data", "title", title).first();
            if (document != null) {
                id.accept(document.getString("id"));
            }
        });
    }

    @Override
    public void getSurvey(String id, Consumer<Survey> survey) {
        CompletableFuture.runAsync(() -> {
            Survey back = null;
            SqlDocument document = this.client.find("survey_data", "id", id).first();
            if (document != null) {
                String title = document.getString("title");
                String text = document.getString("text");
                Survey.Status status = Survey.Status.valueOf(document.getString("status"));
                long time = document.getLong("time");
                Map<String, Integer> map = new HashMap<>();
                String voted = document.getString("voted");
                if (!voted.isEmpty()) {
                    for (String e : document.getString("voted").split("#")) {
                        map.put(e.split(":")[0], Integer.parseInt(e.split(":")[1]));
                    }
                }
                back = new Survey(title, text, status, id, time, map);
            }
            survey.accept(back);
        });
    }

    @Override
    public void surveyExists(String title, Consumer<Boolean> exists) {
        CompletableFuture.runAsync(() -> {
            SqlDocument document = this.client.find("survey_data", "title", title).first();
            exists.accept(document != null);
        });
    }

    @Override
    public String getProvider() {
        return "MySql";
    }

}
