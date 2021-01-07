package org.ifaco.friendtracker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.icu.util.HebrewCalendar;
import android.icu.util.JapaneseCalendar;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.EncodeStrategy;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.model.LatLng;

import org.ifaco.friendtracker.etc.Fonts;
import org.ifaco.friendtracker.etc.SolarHijri;
import org.ifaco.friendtracker.nav.Settings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static org.ifaco.friendtracker.Nav.here;

@SuppressWarnings({"unused", "RedundantSuppression", "UnusedReturnValue"})
@SuppressLint("SetTextI18n")
public
class Fun {
    public static Context c;
    public static SharedPreferences sp;
    public static DisplayMetrics dm = new DisplayMetrics();
    public static boolean dirLtr = true, cmCallbackSet = false, connected = false, night = false,
            hasAvatar = false;
    public static String cloudFol = "https://ifaco.org/android/ftracker/",
            cloud = cloudFol + "manager.php", exCode = "code", exNumb = "numb", exName = "name";
    public static Typeface fontLogo, fontField, fontText, fontEng;
    public static int calendar = 1;
    private static ValueAnimator coverer;
    private static AnimatorSet navDrawer;
    private static final Class<?>[] routes = new Class<?>[]{Home.class, Settings.class};

    public static View.OnClickListener doNothing = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };


    public static AppCompatActivity init(AppCompatActivity that, ViewGroup body) {
        c = that.getApplicationContext();
        sp = that.getSharedPreferences(that.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        dm = that.getResources().getDisplayMetrics();
        dirLtr = that.getResources().getBoolean(R.bool.dirLtr);
        if (!dirLtr) body.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        night = isNight(that.getResources().getConfiguration());
        calendar = that.getResources().getInteger(R.integer.calendar);

        // Network State
        ConnectivityManager cm = (ConnectivityManager) that.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return that;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!cmCallbackSet)
                cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                    private final ArrayList<Network> activeNetworks = new ArrayList<>();

                    @Override
                    public void onAvailable(@NonNull Network network) {
                        super.onAvailable(network);
                        boolean found = false;
                        for (Network an : activeNetworks)
                            if (an.getNetworkHandle() == network.getNetworkHandle())
                                found = true;
                        if (!found) activeNetworks.add(network);
                        send();
                    }

                    @Override
                    public void onLost(@NonNull Network network) {
                        super.onLost(network);
                        for (Network an : activeNetworks)
                            if (an.getNetworkHandle() == network.getNetworkHandle())
                                activeNetworks.remove(network);
                        send();
                    }

                    void send() {
                        connected = activeNetworks.size() > 0;
                        Handler[] hs = new Handler[]{Intro.nwHandler, Home.nwHandler};
                        for (Handler h : hs) if (h != null) h.obtainMessage().sendToTarget();
                    }
                });
        } else {
            NetworkInfo nwi = cm.getActiveNetworkInfo();
            connected = (nwi != null && nwi.isConnected());
        }
        cmCallbackSet = true;

        // Fonts
        if (fontLogo == null) fontLogo = fonts(Fonts.LOGO);
        if (fontField == null) fontField = fonts(Fonts.FIELD);
        if (fontText == null) fontText = fonts(Fonts.TEXT);
        if (fontEng == null) fontEng = fonts(Fonts.ENG);

        return that;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static CountDownTimer onLoad(final View view, final Function func) {
        return new CountDownTimer(10000, 50) {
            @Override
            public void onFinish() {
            }

            @Override
            public void onTick(long millisUntilFinished) {
                if (view.getWidth() <= 0) return;
                func.execute();
                this.cancel();
            }
        }.start();
    }

    public static long now() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static ValueAnimator VA(View v, String prop, int dur, float val1, float val2) {
        final ValueAnimator mValueAnimator1 = ObjectAnimator.ofFloat(v, prop, val1, val2).setDuration(dur);
        mValueAnimator1.start();
        return mValueAnimator1;
    }

    public static ObjectAnimator OA(View v, String prop, float value, int dur) {
        ObjectAnimator mAnim = ObjectAnimator.ofFloat(v, prop, value).setDuration(dur);
        mAnim.start();
        return mAnim;
    }

    public static void goTo(AppCompatActivity that, Class<?> cls) {
        final Intent intent = new Intent(that, cls);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) that.startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(that).toBundle());
        else that.startActivity(intent);
    }

    public static void goTo(AppCompatActivity that, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) that.startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(that).toBundle());
        else that.startActivity(intent);
    }

    public static void vis(View v, boolean b) {
        v.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public static int dp(int px) {
        return (int) (dm.density * (float) px);
    }

    public static JsonReader jsonReader(String json) {
        return new JsonReader(new InputStreamReader(new ByteArrayInputStream(
                json.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
    }

    public static String compileTime(long time) {
        Calendar gc = Calendar.getInstance();
        gc.setTimeInMillis(time);
        String date = "";
        switch (calendar) {
            case 0:// Solar Hijri
                SolarHijri shamsi = new SolarHijri(gc);
                date = z(shamsi.Y) + "/" + z(shamsi.M + 1) + "/" + z(shamsi.D);
                break;
            case 2:// Lunar Hijri
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    IslamicCalendar ic = new IslamicCalendar();
                    ic.setTimeInMillis(time);
                    date = z(ic.get(Calendar.YEAR)) + "/" +
                            z(ic.get(Calendar.MONTH) + 1) + "/" + z(ic.get(Calendar.DAY_OF_MONTH));
                }*/
                break;
            case 3:// Hebrew
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    HebrewCalendar hc = new HebrewCalendar();
                    hc.setTimeInMillis(time);
                    date = z(hc.get(Calendar.YEAR)) + "." +
                            z(hc.get(Calendar.MONTH) + 1) + "." + z(hc.get(Calendar.DAY_OF_MONTH));
                }
                break;
            case 4:// Japanese
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    JapaneseCalendar jc = new JapaneseCalendar();
                    jc.setTimeInMillis(time);
                    date = z(jc.get(Calendar.YEAR)) + "." +
                            z(jc.get(Calendar.MONTH) + 1) + "." + z(jc.get(Calendar.DAY_OF_MONTH));
                }
                break;
        }
        if (date.equals("")) date = z(gc.get(Calendar.YEAR)) + "." +
                z(gc.get(Calendar.MONTH) + 1) + "." + z(gc.get(Calendar.DAY_OF_MONTH));
        return date + " - " + z(gc.get(Calendar.HOUR_OF_DAY)) + ":" + z(gc.get(Calendar.MINUTE)) + ":" +
                z(gc.get(Calendar.SECOND));
    }

    public static String z(int n) {
        String s = Integer.toString(n);
        if (s.length() == 1) return "0" + s;
        else return s;
    }

    public static String dist(LatLng latlng) {
        if (here == null) return "";
        else {
            float[] results = new float[1];
            Location.distanceBetween(here.getLatitude(), here.getLongitude(), latlng.latitude, latlng.longitude, results);
            float res = results[0];
            int metric = R.string.metres;
            if (res > 1000f) {
                res /= 1000f;
                metric = R.string.kilometres;
            }
            String ruturn = c.getString(metric, new DecimalFormat("#.##").format(res));
            ruturn = ruturn.replace("٫", dirLtr ? "." : "/");
            return ruturn;
        }
    }

    public static boolean isNight(Configuration con) {
        return (con.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static Typeface fonts(Fonts which) {
        return Typeface.createFromAsset(c.getAssets(), c.getResources().getString(which.id));
    }

    public static void nav(final AppCompatActivity that, ConstraintLayout nav, int i) {
        nav.setTranslationX(transNav(nav));
        final ConstraintLayout navTitle = (ConstraintLayout) nav.getChildAt(0);
        final ImageView navLogo = (ImageView) navTitle.getChildAt(0);
        final TextView navTitleText = (TextView) navTitle.getChildAt(1);
        onLoad(navLogo, new Fun.Function() {
            @Override
            public void execute() {
                ConstraintLayout.LayoutParams navLogoLP = (ConstraintLayout.LayoutParams) navLogo.getLayoutParams();
                navLogoLP.height = (int) (dm.widthPixels * 0.15f);
                navLogoLP.topMargin = (int) (navLogoLP.height * 0.45);
                navLogoLP.bottomMargin = (int) (navLogoLP.height * 0.45);
                navLogo.setLayoutParams(navLogoLP);

                ConstraintLayout.LayoutParams navTitleTextLP =
                        (ConstraintLayout.LayoutParams) navTitleText.getLayoutParams();
                navTitleTextLP.setMarginStart((int) (that.getResources().getDimension(R.dimen.tbTitleMargin) +
                        (navLogoLP.height * 0.0225f)));
                navTitleText.setLayoutParams(navTitleTextLP);
            }
        });
        final LinearLayout layout = (LinearLayout) ((ScrollView) nav.getChildAt(
                nav.getChildCount() - 1)).getChildAt(0);
        for (int v = 0; v < layout.getChildCount(); v++) {
            LinearLayout item = (LinearLayout) layout.getChildAt(v);
            TextView text = (TextView) item.getChildAt(0);
            if (v != i) {
                final int V = v;
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (routes[V] == null) return;
                        if (V > 0) that.startActivity(new Intent(that, routes[V]));
                        else that.onBackPressed();
                    }
                });
            } else item.setBackgroundResource(R.drawable.nav_item_pressed);
            text.setTypeface(fontText);
        }
    }

    public static void cover(final View cover, final boolean b) {
        if (coverer != null) {
            try {
                coverer.cancel();
            } catch (Exception ignored) {
            }
            coverer = null;
        }
        if (b) Fun.vis(cover, true);
        coverer = ObjectAnimator.ofFloat(cover, "alpha", b ? 1f : 0f);
        coverer.setDuration(400);
        coverer.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!b) Fun.vis(cover, false);
            }
        });
        coverer.start();
    }

    public static void navDrawer(final ConstraintLayout nav, final View navShadow, final boolean b) {
        if (navDrawer != null) {
            try {
                navDrawer.cancel();
            } catch (Exception ignored) {
            }
            navDrawer = null;
        }
        if (b) {
            Fun.vis(nav, true);
            Fun.vis(navShadow, true);
        }
        float dest = b ? 0f : transNav(nav);
        navDrawer = new AnimatorSet();
        navDrawer.playTogether(
                ObjectAnimator.ofFloat(nav, "translationX", dest),
                ObjectAnimator.ofFloat(navShadow, "translationX", dest)
        );
        navDrawer.setDuration(250);
        navDrawer.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!b) {
                    Fun.vis(nav, false);
                    Fun.vis(navShadow, false);
                }
            }
        });
        navDrawer.start();
    }

    public static float transNav(ConstraintLayout nav) {
        float dist = dm.widthPixels * ((ConstraintLayout.LayoutParams) nav.getLayoutParams()).matchConstraintPercentWidth;
        return dirLtr ? -dist : dist;
    }

    public static void explode(View v, long dur, float max) {
        ConstraintLayout parent;
        try {
            parent = (ConstraintLayout) v.getParent();
        } catch (Exception ignored) {
            return;
        }
        if (parent == null) return;
        final ConstraintLayout PARENT = parent;

        final View ex = new View(c);
        ConstraintLayout.LayoutParams exLP = new ConstraintLayout.LayoutParams(0, 0);
        exLP.topToTop = v.getId();
        exLP.leftToLeft = v.getId();
        exLP.rightToRight = v.getId();
        exLP.bottomToBottom = v.getId();
        ex.setBackgroundResource(R.drawable.explosive_1);
        ex.setTranslationX(v.getTranslationX());
        ex.setTranslationY(v.getTranslationY());
        ex.setScaleX(v.getScaleX());
        ex.setScaleY(v.getScaleY());
        parent.addView(ex, parent.indexOfChild(v), exLP);

        AnimatorSet explode = new AnimatorSet().setDuration(dur);
        ValueAnimator hide = ObjectAnimator.ofFloat(ex, "alpha", 0f);
        hide.setStartDelay(explode.getDuration() / 4);
        explode.playTogether(
                ObjectAnimator.ofFloat(ex, "scaleX", ex.getScaleX() * max),
                ObjectAnimator.ofFloat(ex, "scaleY", ex.getScaleY() * max),
                hide
        );
        explode.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                PARENT.removeView(ex);
            }
        });
        explode.start();
    }

    public static void configureNav(AppCompatActivity that, ConstraintLayout nav, ImageView navLogo,
                                    TextView navTitleText, TextView navVersion, View navShadow) {
        nav.setOnClickListener(doNothing);
        if (!night) navLogo.setColorFilter(new PorterDuffColorFilter(
                ContextCompat.getColor(c, R.color.navLogo), PorterDuff.Mode.SRC_IN));
        if (that.getResources().getBoolean(R.bool.boldLogo))
            navTitleText.setTypeface(fontLogo, Typeface.BOLD);
        else navTitleText.setTypeface(fontLogo);
        String version = "";
        try {
            version = that.getPackageManager().getPackageInfo(that.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        if (!version.equals("")) navVersion.setText("v" + version);
        if (!dirLtr) navShadow.setRotationY(180f);
    }

    public static Bitmap bmpRound(Bitmap bitmap) {
        int widthLight = bitmap.getWidth(), heightLight = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(widthLight, heightLight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paintColor = new Paint();
        paintColor.setFlags(Paint.ANTI_ALIAS_FLAG);
        RectF rectF = new RectF(new Rect(0, 0, widthLight, heightLight));
        canvas.drawRoundRect(rectF, widthLight / 2f, heightLight / 2f, paintColor);
        Paint paintImage = new Paint();
        paintImage.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(bitmap, 0f, 0f, paintImage);
        return output;
    }

    @SuppressWarnings("ConstantConditions")
    public static void loadAvatar(AppCompatActivity that, final ImageView iv, String number) {
        String numb = convertNumbersToEnglish(number);
        hasAvatar = false;
        if (numb != null) try {
            Glide.with(that).asBitmap()
                    .diskCacheStrategy(new DiskCacheStrategy() {
                        @Override
                        public boolean isDataCacheable(DataSource dataSource) {
                            return false;
                        }

                        @Override
                        public boolean isResourceCacheable(boolean isFromAlternateCacheKey,
                                                           DataSource dataSource, EncodeStrategy encodeStrategy) {
                            return false;
                        }

                        @Override
                        public boolean decodeCachedResource() {
                            return false;
                        }

                        @Override
                        public boolean decodeCachedData() {
                            return false;
                        }
                    })
                    .load(cloudFol + "avatar/" + numb + ".jpg")
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            iv.setImageResource(R.drawable.default_user_1);
                        }

                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            iv.setImageBitmap(bmpRound(resource));
                            hasAvatar = true;
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            iv.setImageResource(R.drawable.default_user_1);
                        }
                    });
        } catch (Exception ignored) {
        }
        else iv.setImageResource(R.drawable.default_user_1);
    }

    public static String convertNumbersToEnglish(String str) {
        String answer = str;
        answer = answer.replace("١", "1");
        answer = answer.replace("٢", "2");
        answer = answer.replace("٣", "3");
        answer = answer.replace("٤", "4");
        answer = answer.replace("٥", "5");
        answer = answer.replace("٦", "6");
        answer = answer.replace("٧", "7");
        answer = answer.replace("٨", "8");
        answer = answer.replace("٩", "9");
        answer = answer.replace("٠", "0");
        return answer;
    }

    public static String imageToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes, Base64.DEFAULT);
    }


    public interface Function {
        void execute();
    }
}
