package net.lldv.surveysystem.components.provider;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.lldv.surveysystem.SurveySystem;
import net.lldv.surveysystem.components.data.Survey;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MongodbProvider extends Provider {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoCollection<Document> surveyData;

    @Override
    public void connect(SurveySystem instance) {
        CompletableFuture.runAsync(() -> {
            MongoClientURI uri = new MongoClientURI(instance.getConfig().getString("MongoDB.Uri"));
            this.mongoClient = new MongoClient(uri);
            this.mongoDatabase = this.mongoClient.getDatabase(instance.getConfig().getString("MongoDB.Database"));
            this.surveyData = this.mongoDatabase.getCollection("survey_data");

            for (Document document : this.surveyData.find()) {
                this.getSurvey(document.getString("id"), survey -> this.surveyMap.put(document.getString("title"), survey));
            }

            this.reloadSurveyData();
            instance.getLogger().info("[MongoClient] Connection opened.");
        });
    }

    @Override
    public void disconnect(SurveySystem instance) {
        this.mongoClient.close();
        instance.getLogger().info("[MongoClient] Connection closed.");
    }

    @Override
    public void createSurvey(String title, String text, long time) {
        CompletableFuture.runAsync(() -> {
            String id = this.getRandomIDCode();
            String date = this.getDate();
            List<String> list = new ArrayList<>();
            Map<String, Integer> map = new HashMap<>();
            long end = System.currentTimeMillis() + (time * 3600L) * 1000L;
            Document document = new Document("id", id)
                    .append("title", title)
                    .append("text", text)
                    .append("date", date)
                    .append("status", Survey.Status.OPEN.name().toUpperCase())
                    .append("time", end)
                    .append("voted", list);
            this.surveyData.insertOne(document);
            this.surveyMap.put(title, new Survey(title, text, Survey.Status.OPEN, id, end, map));
        });
    }

    @Override
    public void deleteSurvey(String id) {
        CompletableFuture.runAsync(() -> this.getSurvey(id, survey -> {
            this.surveyData.findOneAndDelete(new Document("id", id));
            this.surveyMap.remove(survey.getTitle());
        }));
    }

    @Override
    public void closeSurvey(String id) {
        CompletableFuture.runAsync(() -> this.getSurvey(id, survey -> {
            this.surveyData.updateOne(Objects.requireNonNull(this.surveyData.find(new Document("id", id)).first()), new Document("$set", new Document("status", Survey.Status.CLOSED.name().toUpperCase())));
            this.surveyMap.remove(survey.getTitle());
            this.surveyMap.put(survey.getTitle(), new Survey(survey.getTitle(), survey.getText(), Survey.Status.CLOSED, survey.getId(), survey.getTime(), survey.getVotedPlayers()));
        }));
    }

    @Override
    public void updateSurvey(String id, String player, boolean vote) {
        CompletableFuture.runAsync(() -> {
            int i = 0;
            if (vote) i = 1;
            Document document = this.surveyData.find(new Document("id", id)).first();
            if (document != null) {
                List<String> list = document.getList("voted", String.class);
                list.add(player + ":" + i);
                this.surveyData.updateOne(Objects.requireNonNull(this.surveyData.find(new Document("id", id)).first()), new Document("$set", new Document("voted", list)));
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
            Document document = this.surveyData.find(new Document("title", title)).first();
            if (document != null) {
                id.accept(document.getString("id"));
            }
        });
    }

    @Override
    public void getSurvey(String id, Consumer<Survey> survey) {
        CompletableFuture.runAsync(() -> {
            Survey back = null;
            Document document = this.surveyData.find(new Document("id", id)).first();
            if (document != null) {
                String title = document.getString("title");
                String text = document.getString("text");
                Survey.Status status = Survey.Status.valueOf(document.getString("status"));
                long time = document.getLong("time");
                Map<String, Integer> map = new HashMap<>();
                document.getList("voted", String.class).forEach(e -> map.put(e.split(":")[0], Integer.parseInt(e.split(":")[1])));
                back = new Survey(title, text, status, id, time, map);
            }
            survey.accept(back);
        });
    }

    @Override
    public void surveyExists(String title, Consumer<Boolean> exists) {
        CompletableFuture.runAsync(() -> {
           Document document = this.surveyData.find(new Document("title", title)).first();
           exists.accept(document != null);
        });
    }

    @Override
    public String getProvider() {
        return "MongoDB";
    }

}
