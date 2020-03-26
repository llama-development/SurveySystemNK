package net.llamadevelopment.surveysystem;

import cn.nukkit.command.CommandMap;
import cn.nukkit.plugin.PluginBase;
import net.llamadevelopment.surveysystem.commands.SurveyCommand;
import net.llamadevelopment.surveysystem.components.managers.MongoDBManager;
import net.llamadevelopment.surveysystem.components.managers.MySqlManager;
import net.llamadevelopment.surveysystem.components.managers.YamlManager;
import net.llamadevelopment.surveysystem.components.managers.database.ProviderManager;
import net.llamadevelopment.surveysystem.listener.FormListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SurveySystem extends PluginBase {

    private static SurveySystem instance;
    public static ProviderManager provider;
    public static Map<String, ProviderManager> providers = new HashMap<String, ProviderManager>();

    @Override
    public void onEnable() {
        instance = this;
        loadSystem();
        registerProvider(new MongoDBManager());
        registerProvider(new MySqlManager());
        registerProvider(new YamlManager());
        if (!providers.containsKey(getConfig().getString("Provider"))) {
            getLogger().error("Please specify a valid provider: Yaml, MySql, MongoDB");
            return;
        }
        provider = providers.get(getConfig().getString("Provider"));
        provider.setUp(this);
    }

    private void loadSystem() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new FormListener(), this);
        CommandMap map = getServer().getCommandMap();
        map.register(getConfig().getString("Commands.Survey"), new SurveyCommand(this));
    }

    @Override
    public void onDisable() {
        provider.disconnect(this);
    }

    public void registerProvider(ProviderManager provider) {
        providers.put(provider.getProvider(), provider);
    }

    public String getSurveyID() {
        String string = "S";
        int lastrandom = 0;
        for (int i = 0; i < 6; i++) {
            Random random = new Random();
            int rand = random.nextInt(9);
            while (rand == lastrandom) {
                rand = random.nextInt(9);
            }
            lastrandom = rand;
            string = string + rand;
        }
        return string;
    }

    public static SurveySystem getInstance() {
        return instance;
    }
}
