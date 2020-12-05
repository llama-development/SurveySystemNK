package net.lldv.surveysystem;

import cn.nukkit.plugin.PluginBase;
import lombok.Getter;
import net.lldv.surveysystem.commands.SurveyCommand;
import net.lldv.surveysystem.commands.SurveyadminCommand;
import net.lldv.surveysystem.components.api.API;
import net.lldv.surveysystem.components.forms.FormListener;
import net.lldv.surveysystem.components.forms.FormWindows;
import net.lldv.surveysystem.components.language.Language;
import net.lldv.surveysystem.components.provider.MongodbProvider;
import net.lldv.surveysystem.components.provider.MySqlProvider;
import net.lldv.surveysystem.components.provider.Provider;
import net.lldv.surveysystem.components.provider.YamlProvider;

import java.util.HashMap;
import java.util.Map;

public class SurveySystem extends PluginBase {

    private final Map<String, Provider> providers = new HashMap<>();
    public Provider provider;

    @Getter
    private static API api;

    @Override
    public void onEnable() {
        try {
            this.saveDefaultConfig();
            this.providers.put("MongoDB", new MongodbProvider());
            this.providers.put("MySql", new MySqlProvider());
            this.providers.put("Yaml", new YamlProvider());
            if (!this.providers.containsKey(this.getConfig().getString("Provider"))) {
                this.getLogger().error("§4Please specify a valid provider: Yaml, MySql, MongoDB");
                return;
            }
            this.provider = this.providers.get(this.getConfig().getString("Provider"));
            this.provider.connect(this);
            this.getLogger().info("§aSuccessfully loaded " + this.provider.getProvider() + " provider.");
            api = new API(this.provider, new FormWindows(this.provider));
            Language.init(this);
            this.loadPlugin();
            this.getLogger().info("§aSurveySystem successfully started.");
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().error("§4Failed to load SurveySystem.");
        }
    }

    private void loadPlugin() {
        this.getServer().getPluginManager().registerEvents(new FormListener(), this);
        this.getServer().getCommandMap().register("surveysystem", new SurveyCommand(this));
        this.getServer().getCommandMap().register("surveysystem", new SurveyadminCommand(this));


    }

    @Override
    public void onDisable() {
        this.provider.disconnect(this);
    }

}
