package net.llamadevelopment.surveysystem.components.managers;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.llamadevelopment.surveysystem.SurveySystem;
import net.llamadevelopment.surveysystem.components.managers.database.ProviderManager;
import net.llamadevelopment.surveysystem.components.utils.SurveyUtil;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoDBManager extends ProviderManager {

    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;
    private static MongoCollection<Document> surveyCollection;

    @Override
    public void setUp(SurveySystem instance) {
        try {
            MongoClientURI uri = new MongoClientURI(instance.getConfig().getString("MongoDB.Uri"));
            mongoClient = new MongoClient(uri);
            mongoDatabase = mongoClient.getDatabase(instance.getConfig().getString("MongoDB.Database"));
            surveyCollection = mongoDatabase.getCollection("surveys");
            instance.getLogger().info("§aConnected successfully to database!");
        } catch (Exception e) {
            instance.getLogger().error("§4Failed to connect to database.");
            instance.getLogger().error("§4Please check your details in the config.yml.");
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect(SurveySystem instance) {
        mongoClient.close();
    }

    @Override
    public boolean hasVoted(String player, String id) {
        Document document = surveyCollection.find(new Document("id", id)).first();
        if (document != null) {
            String[] s = document.getString("players").split(":");
            for (String e : s) {
                if (e.equalsIgnoreCase(player)) return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public void createSurvey(String title, String text, int time) {
        int seconds = time * 3600;
        long current = System.currentTimeMillis();
        long end = current + seconds * 1000L;
        Document document = new Document("title", title)
                .append("text", text)
                .append("status", "Open")
                .append("id", SurveySystem.getInstance().getSurveyID())
                .append("time", end)
                .append("yes", 0)
                .append("no", 0)
                .append("all", 0)
                .append("players", "");
        surveyCollection.insertOne(document);
    }

    @Override
    public void deleteSurvey(String id) {
        MongoCollection<Document> collection = surveyCollection;
        collection.deleteOne(new Document("id", id));
    }

    @Override
    public void closeSurvey(String id) {
        Document document = surveyCollection.find(new Document("id", id)).first();
        assert document != null;
        Bson bson = new Document("status", "Closed");
        Bson bson1 = new Document("$set", bson);
        surveyCollection.updateOne(document, bson1);
    }

    @Override
    public void checkSurvey() {
        for (Document doc : surveyCollection.find()) {
            SurveyUtil surveyUtil = getSurvey(doc.getString("id"));
            if (surveyUtil.getTime() < System.currentTimeMillis()) {
                closeSurvey(surveyUtil.getId());
            }
        }
    }

    private void addYes(String id) {
        Document document = surveyCollection.find(new Document("id", id)).first();
        assert document != null;
        int i = document.getInteger("yes");
        Bson bson = new Document("yes", i += 1);
        Bson bson1 = new Document("$set", bson);
        surveyCollection.updateMany(document, bson1);
    }

    private void addNo(String id) {
        Document document = surveyCollection.find(new Document("id", id)).first();
        assert document != null;
        int i = document.getInteger("no");
        Bson bson = new Document("no", i += 1);
        Bson bson1 = new Document("$set", bson);
        surveyCollection.updateMany(document, bson1);
    }

    private void addAll(String id) {
        Document document = surveyCollection.find(new Document("id", id)).first();
        assert document != null;
        int i = document.getInteger("all");
        Bson bson = new Document("all", i += 1);
        Bson bson1 = new Document("$set", bson);
        surveyCollection.updateMany(document, bson1);
    }

    private void addPlayer(String id, String player) {
        Document document = surveyCollection.find(new Document("id", id)).first();
        assert document != null;
        String e = document.getString("players");
        Bson bson4 = new Document("players", e + player + ":");
        Bson bson5 = new Document("$set", bson4);
        surveyCollection.updateMany(document, bson5);
    }

    @Override
    public void updateSurvey(String id, String player, boolean type) {
        if (type) {
            addYes(id);
            addAll(id);
            addPlayer(id, player);
        } else {
            addNo(id);
            addAll(id);
            addPlayer(id, player);
        }
    }

    @Override
    public String convertToID(String title) {
        Document document = surveyCollection.find(new Document("title", title)).first();
        if (document != null) return document.getString("id");
        return null;
    }

    @Override
    public SurveyUtil getSurvey(String id) {
        Document document = surveyCollection.find(new Document("id", id)).first();
        String title = "";
        String text = "";
        String status = "";
        long time = 0;
        int yes = 0, no = 0, all = 0;
        if (document != null) {
            title = document.getString("title");
            text = document.getString("text");
            status = document.getString("status");
            time = document.getLong("time");
            yes = document.getInteger("yes");
            no = document.getInteger("no");
            all = document.getInteger("all");
        }
        return new SurveyUtil(title, text, status, id, time, yes, no, all);
    }

    @Override
    public boolean surveyExistsByTitle(String title) {
        for (Document doc : surveyCollection.find(new Document("title", title))) {
            if (doc.getString("title").equalsIgnoreCase(title)) return true;
            else return false;
        }
        return false;
    }

    @Override
    public String getProvider() {
        return "MongoDB";
    }

    public static MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    public static MongoCollection<Document> getSurveyCollection() {
        return surveyCollection;
    }
}
