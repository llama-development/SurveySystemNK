package net.lldv.surveysystem.components.forms;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.level.Sound;
import lombok.AllArgsConstructor;
import net.lldv.surveysystem.components.data.Survey;
import net.lldv.surveysystem.components.forms.custom.CustomForm;
import net.lldv.surveysystem.components.forms.modal.ModalForm;
import net.lldv.surveysystem.components.forms.simple.SimpleForm;
import net.lldv.surveysystem.components.language.Language;
import net.lldv.surveysystem.components.provider.Provider;

import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
public class FormWindows {

    private final Provider provider;

    public void openSurveys(final Player player) {
        this.provider.reloadSurveyData();
        SimpleForm.Builder form = new SimpleForm.Builder(Language.getNP("surveys.title"), Language.getNP("surveys.content"));
        this.provider.surveyMap.forEach((title, survey) -> {
            if (survey.getStatus() == Survey.Status.OPEN) form.addButton(new ElementButton(Language.getNP("surveys.button", title, this.provider.getRemainingTime(survey.getTime()))), e -> this.openSurvey(player, survey.getId()));
        });
        form.build().send(player);
    }

    public void openSurvey(final Player player, final String id) {
        this.provider.reloadSurveyData();
        this.provider.getSurvey(id, survey -> {
            if (!survey.getVotedPlayers().containsKey(player.getName())) {
            SimpleForm.Builder form = new SimpleForm.Builder(survey.getTitle(), Language.getNP("survey.content", survey.getText(), this.provider.getRemainingTime(survey.getTime())));
                form.addButton(new ElementButton(Language.getNP("survey.vote.yes")), e -> {
                    if (survey.getStatus() != Survey.Status.OPEN) return;
                    this.provider.updateSurvey(survey.getId(), player.getName(), true);
                    player.sendMessage(Language.get("survey.voted.for"));
                    this.provider.playSound(player, Sound.NOTE_PLING);
                });
                form.addButton(new ElementButton(Language.getNP("survey.vote.no")), e -> {
                    if (survey.getStatus() != Survey.Status.OPEN) return;
                    this.provider.updateSurvey(survey.getId(), player.getName(), false);
                    player.sendMessage(Language.get("survey.voted.against"));
                    this.provider.playSound(player, Sound.NOTE_PLING);
                });
                form.addButton(new ElementButton(Language.getNP("ui.back")), e -> this.openSurveys(player));
                form.build().send(player);
            } else {
                AtomicInteger i = new AtomicInteger();
                AtomicInteger h = new AtomicInteger();
                survey.getVotedPlayers().forEach((key, value) -> {
                    if (value == 1) i.getAndIncrement();
                    else h.getAndIncrement();
                });
                SimpleForm.Builder form = new SimpleForm.Builder(survey.getTitle(), Language.getNP("survey.content.voted", survey.getText(), i.get(), h.get(), this.provider.getRemainingTime(survey.getTime())));
                form.addButton(new ElementButton(Language.getNP("ui.back")), e -> this.openSurveys(player));
                form.build().send(player);
            }
        });
    }

    public void openAdminPanel(final Player player) {
        this.provider.reloadSurveyData();
        SimpleForm form = new SimpleForm.Builder(Language.getNP("adminpanel.title"), Language.getNP("adminpanel.content"))
                .addButton(new ElementButton(Language.getNP("adminpanel.create")), e -> this.openCreateMenu(player))
                .addButton(new ElementButton(Language.getNP("adminpanel.delete")), e -> this.openDeleteMenu(player))
                .addButton(new ElementButton(Language.getNP("adminpanel.closed")), e -> this.openClosedSurveys(player))
                .build();
        form.send(player);
    }

    public void openCreateMenu(final Player player) {
        CustomForm form = new CustomForm.Builder(Language.getNP("create.title"))
                .addElement(new ElementInput(Language.getNP("create.survey.title"), Language.getNP("create.survey.title.placeholder")))
                .addElement(new ElementInput(Language.getNP("create.survey.text"), Language.getNP("create.survey.text.placeholder")))
                .addElement(new ElementInput(Language.getNP("create.survey.hours"), Language.getNP("create.survey.hours.placeholder")))
                .onSubmit((u, k) -> {
                    String title = k.getInputResponse(0);
                    String text = k.getInputResponse(1);
                    String hours = k.getInputResponse(2);
                    if (title.isEmpty() || text.isEmpty() || hours.isEmpty()) {
                        player.sendMessage(Language.get("invalid.input"));
                        this.provider.playSound(player, Sound.NOTE_BASS);
                        return;
                    }
                    this.provider.surveyExists(title, exists -> {
                        if (exists) {
                            player.sendMessage(Language.get("survey.exists"));
                            this.provider.playSound(player, Sound.NOTE_BASS);
                            return;
                        }
                        try {
                            long time = Integer.parseInt(hours);
                            this.provider.createSurvey(title, text, time);
                            player.sendMessage(Language.get("survey.created", title));
                            this.provider.playSound(player, Sound.RANDOM_LEVELUP);
                        } catch (NumberFormatException e) {
                            player.sendMessage(Language.get("invalid.input"));
                            this.provider.playSound(player, Sound.NOTE_BASS);
                        }
                    });
                })
                .build();
        form.send(player);
    }

    public void openDeleteMenu(final Player player) {
        SimpleForm.Builder form = new SimpleForm.Builder(Language.getNP("delete.title"), Language.getNP("delete.content"));
        this.provider.surveyMap.values().forEach(survey -> {
            if (survey.getStatus() == Survey.Status.CLOSED) {
                form.addButton(new ElementButton(survey.getTitle()), e -> {
                    ModalForm modalForm = new ModalForm.Builder(Language.getNP("delete.confirm.title"),
                            Language.getNP("delete.confirm.content"), Language.getNP("delete.confirm.delete"), Language.getNP("ui.back"))
                            .onYes(k -> {
                                this.provider.deleteSurvey(survey.getId());
                                player.sendMessage(Language.get("survey.deleted", survey.getId()));
                                this.provider.playSound(player, Sound.RANDOM_LEVELUP);
                            })
                            .onNo(g -> this.openDeleteMenu(player))
                            .build();
                    modalForm.send(player);
                });
            }
        });
        form.addButton(new ElementButton(Language.getNP("ui.back")), e -> this.openAdminPanel(player));
        form.build().send(player);
    }

    public void openClosedSurveys(final Player player) {
        SimpleForm.Builder form = new SimpleForm.Builder(Language.getNP("closed.title"), Language.getNP("closed.content"));
        this.provider.surveyMap.values().forEach(survey -> {
            if (survey.getStatus() == Survey.Status.CLOSED) {
                form.addButton(new ElementButton(survey.getTitle()), e -> this.openResults(player, survey.getId()));
            }
        });
        form.addButton(new ElementButton(Language.getNP("ui.back")), e -> this.openAdminPanel(player));
        form.build().send(player);
    }

    public void openResults(final Player player, final String id) {
        this.provider.getSurvey(id, survey -> {
            AtomicInteger i = new AtomicInteger();
            AtomicInteger h = new AtomicInteger();
            survey.getVotedPlayers().forEach((key, value) -> {
                if (value == 1) i.getAndIncrement();
                else h.getAndIncrement();
            });
            SimpleForm form = new SimpleForm.Builder(survey.getTitle(), Language.getNP("results.content", survey.getText(), survey.getId(), survey.getStatus(), i.get(), h.get()))
                    .addButton(new ElementButton(Language.getNP("ui.back")), e -> this.openClosedSurveys(player))
                    .build();
            form.send(player);
        });
    }

}
