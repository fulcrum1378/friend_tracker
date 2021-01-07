package org.ifaco.friendtracker.etc;

import org.ifaco.friendtracker.R;

public enum Fonts {
    LOGO(R.string.fnLogo),
    FIELD(R.string.fnField),
    TEXT(R.string.fnText),
    ENG(R.string.fnEng);

    public final int id;

    Fonts(int id) {
        this.id = id;
    }
}
