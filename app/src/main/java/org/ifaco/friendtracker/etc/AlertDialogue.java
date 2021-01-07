package org.ifaco.friendtracker.etc;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.ifaco.friendtracker.R;

import static org.ifaco.friendtracker.Fun.dirLtr;
import static org.ifaco.friendtracker.Fun.dm;
import static org.ifaco.friendtracker.Fun.dp;
import static org.ifaco.friendtracker.Fun.fontText;
import static org.ifaco.friendtracker.Fun.night;

@SuppressWarnings({"unused", "RedundantSuppression", "UnusedReturnValue"})
public
class AlertDialogue {
    public static AlertDialog alertDialogue(AppCompatActivity that, Object title, Object msg, View view,
                                            boolean cancellable, int yesId, int noId,
                                            DialogInterface.OnClickListener onYes, DialogInterface.OnClickListener onNo) {
        Drawable marker = ContextCompat.getDrawable(that, R.drawable.marker_1);
        if (night && marker != null) marker.setColorFilter(new PorterDuffColorFilter(
                ContextCompat.getColor(that, R.color.CP), PorterDuff.Mode.SRC_IN));
        AlertDialog.Builder adb = new AlertDialog.Builder(that)
                .setIcon(marker)
                .setCancelable(cancellable);
        if (title instanceof String) adb.setTitle((String) title);
        if (title instanceof Integer) adb.setTitle((int) title);
        if (msg instanceof String) adb.setMessage((String) msg);
        if (msg instanceof Integer) adb.setMessage((int) msg);
        if (view != null) adb.setView(view);
        adb.setPositiveButton(yesId, onYes);
        adb.setNegativeButton(noId, onNo);
        AlertDialog ad = adb.create();
        ad.show();
        fixADButton(that, ad.getButton(AlertDialog.BUTTON_POSITIVE), fontText);
        fixADButton(that, ad.getButton(AlertDialog.BUTTON_NEGATIVE), fontText);
        fixADTitle(that, ad, fontText);
        fixADMsg(that, ad, fontText, false);
        return ad;
    }

    public static AlertDialog alertDialogue(AppCompatActivity that, Object title, Object msg, boolean cancellable,
                                            DialogInterface.OnClickListener onOK) {
        Drawable marker = ContextCompat.getDrawable(that, R.drawable.marker_1);
        if (night && marker != null) marker.setColorFilter(new PorterDuffColorFilter(
                ContextCompat.getColor(that, R.color.CP), PorterDuff.Mode.SRC_IN));
        AlertDialog.Builder adb = new AlertDialog.Builder(that)
                .setIcon(marker)
                .setCancelable(cancellable);
        if (title instanceof String) adb.setTitle((String) title);
        if (title instanceof Integer) adb.setTitle((int) title);
        if (msg instanceof String) adb.setMessage((String) msg);
        if (msg instanceof Integer) adb.setMessage((int) msg);
        adb.setNeutralButton(R.string.ok, onOK);
        AlertDialog ad = adb.create();
        ad.show();
        fixADButton(that, ad.getButton(AlertDialog.BUTTON_NEUTRAL), fontText);
        fixADTitle(that, ad, fontText);
        fixADMsg(that, ad, fontText, false);
        return ad;
    }

    public static void fixADButton(AppCompatActivity that, Button button, Typeface font) {
        if (button == null) return;
        button.setTextColor(ContextCompat.getColor(that, R.color.adButtonTV));
        button.setBackgroundColor(ContextCompat.getColor(that, R.color.adButtonBG));
        button.setTypeface(font);
        button.setTextSize(that.getResources().getDimension(R.dimen.alert1Button) / dm.density);
        if (dirLtr) {
            ViewGroup.MarginLayoutParams buttonLP = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
            buttonLP.setMarginStart((int) button.getTextSize());
        }
    }

    public static TextView fixADTitle(AppCompatActivity that, AlertDialog ad, Typeface font) {
        TextView tvTitle = ad.findViewById(R.id.alertTitle);//that.getWindow()
        if (tvTitle != null) {
            tvTitle.setTypeface(font, Typeface.BOLD);
            tvTitle.setTextSize(that.getResources().getDimension(R.dimen.alert1Title) / dm.density);
            tvTitle.setTextColor(ContextCompat.getColor(that, R.color.adTitle));
        }
        return tvTitle;
    }

    public static TextView fixADMsg(AppCompatActivity that, AlertDialog ad, Typeface font, boolean linkify) {
        TextView tvMsg = ad.findViewById(android.R.id.message);
        if (tvMsg != null) {
            tvMsg.setTypeface(font);
            tvMsg.setLineSpacing(that.getResources().getDimension(R.dimen.alert1MsgLine) / dm.density, 0f);
            tvMsg.setTextSize(that.getResources().getDimension(R.dimen.alert1Msg) / dm.density);
            tvMsg.setPadding(dp(28), dp(15), dp(28), dp(15));
            tvMsg.setTextColor(ContextCompat.getColor(that, R.color.adMessage));
            if (linkify) Linkify.addLinks(tvMsg, Linkify.ALL);
        }
        return tvMsg;
    }
}
