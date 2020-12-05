package net.lldv.surveysystem.components.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lldv.surveysystem.components.forms.FormWindows;
import net.lldv.surveysystem.components.provider.Provider;

@AllArgsConstructor
@Getter
public class API {

    private final Provider provider;
    private final FormWindows formWindows;

}
