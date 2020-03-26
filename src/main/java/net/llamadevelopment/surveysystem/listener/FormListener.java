package net.llamadevelopment.surveysystem.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import net.llamadevelopment.surveysystem.SurveySystem;
import net.llamadevelopment.surveysystem.components.managers.database.ProviderManager;
import net.llamadevelopment.surveysystem.components.utils.Configuration;
import net.llamadevelopment.surveysystem.components.utils.FormUI;

public class FormListener implements Listener {

    @EventHandler
    public void on(PlayerFormRespondedEvent event) {
        Configuration config = new Configuration();
        ProviderManager provider = SurveySystem.provider;
        if (event.getWindow() instanceof FormWindowSimple) {
            FormWindowSimple form = (FormWindowSimple) event.getWindow();
            Player player = event.getPlayer();
            if (form.getTitle().equalsIgnoreCase(config.getAndReplaceNP("Ui.Title.Surveys"))) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getAndReplaceNP("Ui.Button.Close"))) return;
                if (response.equalsIgnoreCase(config.getAndReplaceNP("Ui.Button.NothingToDisplay"))) return;
                if (response.equalsIgnoreCase(config.getAndReplaceNP("Ui.Button.Management"))) {
                    FormUI.openPanel(player);
                    return;
                }
                FormUI.openSurvey(player, provider.convertToID(response));
                FormUI.surveyOpenCache.put(player, response);
                form.setResponse("");
            } else if (form.getTitle().equalsIgnoreCase(config.getAndReplaceNP("Ui.Title.Management"))) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getAndReplaceNP("Ui.Button.CreateSurvey"))) FormUI.openCreationMenu(player);
                if (response.equalsIgnoreCase(config.getAndReplaceNP("Ui.Button.DeleteSurvey"))) FormUI.openDeleteMenu(player);
                if (response.equalsIgnoreCase(config.getAndReplaceNP("Ui.Button.ClosedSurveys"))) FormUI.openClosedSurveyMenu(player);
                form.setResponse("");
            } else if (form.getTitle().equalsIgnoreCase(config.getAndReplaceNP("Ui.Title.DeleteSurvey"))) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getAndReplaceNP("Ui.Button.Back"))) {
                    FormUI.openPanel(player);
                    form.setResponse("");
                    return;
                }
                if (response.equalsIgnoreCase(config.getAndReplaceNP("Ui.Button.NothingToDisplay"))) {
                    FormUI.openPanel(player);
                    form.setResponse("");
                    return;
                }
                FormUI.openDeleteConfirm(player, provider.convertToID(response));
                form.setResponse("");
            } else if (form.getTitle().equalsIgnoreCase(config.getAndReplaceNP("Ui.Title.ClosedSurveys"))) {
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButton().getText();
                if (response.equalsIgnoreCase(config.getAndReplaceNP("Ui.Button.Back"))) {
                    form.setResponse("");
                    FormUI.openPanel(player);
                    return;
                }
                if (response.equalsIgnoreCase(config.getAndReplaceNP("Ui.Button.NothingToDisplay"))) {
                    FormUI.openPanel(player);
                    form.setResponse("");
                    return;
                }
                FormUI.openResultMenu(player, provider.convertToID(response));
                form.setResponse("");
            }
        } else if (event.getWindow() instanceof FormWindowCustom) {
            FormWindowCustom form = (FormWindowCustom) event.getWindow();
            Player player = event.getPlayer();
            if (form.getTitle().equalsIgnoreCase(config.getAndReplaceNP("Ui.Title.CreateSurvey"))) {
                if (form.getResponse() == null) return;
                FormResponseCustom response = form.getResponse();
                try {
                    int i = Integer.parseInt(response.getInputResponse(2));
                    if (provider.surveyExistsByTitle(response.getInputResponse(0))) {
                        player.sendMessage(config.getAndReplace("Messages.SurveyExists"));
                        return;
                    }
                    provider.createSurvey(response.getInputResponse(0), response.getInputResponse(1), i);
                    player.sendMessage(config.getAndReplace("Messages.SurveyCreated", response.getInputResponse(0)));
                } catch (NumberFormatException e) {
                    player.sendMessage(config.getAndReplace("Messages.TimeMustNumber"));
                }
            }
        } else if (event.getWindow() instanceof FormWindowModal) {
            FormWindowModal form = (FormWindowModal) event.getWindow();
            Player player = event.getPlayer();
            if (form.getTitle().equalsIgnoreCase(config.getAndReplaceNP("Ui.Title.DeleteConfirm"))) {
                String id = FormUI.deleteCache.get(player);
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButtonText();
                if (response.equalsIgnoreCase(config.getAndReplaceNP("Ui.Button.Delete"))) {
                    provider.deleteSurvey(id);
                    player.sendMessage(config.getAndReplace("Messages.SurveyDeleted"));
                    FormUI.deleteCache.remove(player);
                } else if (response.equalsIgnoreCase(config.getAndReplaceNP("Ui.Button.Back"))) FormUI.openPanel(player);
            } else if (form.getTitle().equalsIgnoreCase(FormUI.surveyOpenCache.get(player))) {
                String id = provider.convertToID(FormUI.surveyOpenCache.get(player));
                if (form.getResponse() == null) return;
                String response = form.getResponse().getClickedButtonText();
                if (response.equalsIgnoreCase(config.getAndReplaceNP("Ui.Button.VoteYes"))) {
                    provider.updateSurvey(id, player.getName(), true);
                    player.sendMessage(config.getAndReplace("Messages.VotedYes", FormUI.surveyOpenCache.get(player)));
                    form.setResponse("");
                } else if (response.equalsIgnoreCase(config.getAndReplaceNP("Ui.Button.VoteNo"))) {
                    provider.updateSurvey(id, player.getName(), false);
                    player.sendMessage(config.getAndReplace("Messages.VotedNo", FormUI.surveyOpenCache.get(player)));
                    form.setResponse("");
                }
                FormUI.surveyOpenCache.remove(player);
            }
        }
    }

}
