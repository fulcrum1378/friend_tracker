package org.ifaco.friendtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.transition.TransitionManager;
import android.transition.AutoTransition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blure.complexview.ComplexView;
import com.google.android.gms.common.api.ResolvableApiException;

import java.util.HashMap;
import java.util.Map;

import static org.ifaco.friendtracker.Fun.c;
import static org.ifaco.friendtracker.Fun.connected;
import static org.ifaco.friendtracker.Fun.dm;
import static org.ifaco.friendtracker.Fun.exCode;
import static org.ifaco.friendtracker.Fun.exName;
import static org.ifaco.friendtracker.Fun.exNumb;
import static org.ifaco.friendtracker.Fun.fontField;
import static org.ifaco.friendtracker.Fun.fontText;
import static org.ifaco.friendtracker.Fun.sp;
import static org.ifaco.friendtracker.Fun.vis;
import static org.ifaco.friendtracker.Nav.requestLocation;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public class Intro extends AppCompatActivity {
    ConstraintLayout body, iOverflow;
    ComplexView iName, iId, iCode, iSubmit;
    EditText iNameET, iIdET, iCodeET;
    TextView iSubmitTV, iSwitch, iGuide;
    ImageView iSubmitting;
    LottieAnimationView logo;

    AppCompatActivity that;
    final int permLoc = 111, REQUEST_CHECK_SETTINGS = 1110;
    boolean waitingForNetwork = true, submitting = false, canMinimize = false, switching = false;
    public static Handler nwHandler = null, navHandler = null;
    final float endHeight = .3f, endVBias = .05f;
    final long endDur = 900;
    int whyMinimize = 0, stage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);

        body = findViewById(R.id.body);
        iOverflow = findViewById(R.id.iOverflow);
        iName = findViewById(R.id.iName);
        iNameET = findViewById(R.id.iNameET);
        iId = findViewById(R.id.iId);
        iIdET = findViewById(R.id.iIdET);
        iCode = findViewById(R.id.iCode);
        iCodeET = findViewById(R.id.iCodeET);
        iSubmit = findViewById(R.id.iSubmit);
        iSubmitTV = findViewById(R.id.iSubmitTV);
        iSubmitting = findViewById(R.id.iSubmitting);
        iSwitch = findViewById(R.id.iSwitch);
        iGuide = findViewById(R.id.iGuide);
        logo = findViewById(R.id.logo);

        that = Fun.init(this, body);
        restoration(savedInstanceState);
        final ComplexView[] ofFields = new ComplexView[]{iName, iId, iCode, iSubmit};


        // Handlers
        nwHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (waitingForNetwork) check();
            }
        };
        navHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 1) start();
                else if (msg.what == 0) {
                    guide(7);
                    if (msg.obj instanceof ResolvableApiException) try {
                        ResolvableApiException resolvable = (ResolvableApiException) msg.obj;
                        resolvable.startResolutionForResult(that, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException ignored) {
                    }
                    //else {}
                }
            }
        };

        // Check for Network
        showGuide(true);
        guide(0);
        if (connected) {
            waitingForNetwork = false;
            check();
        }

        // onLoad
        Fun.onLoad(body, new Fun.Function() {
            @Override
            public void execute() {
                for (ComplexView cv : ofFields) {
                    ConstraintLayout.LayoutParams cvLP = (ConstraintLayout.LayoutParams) cv.getLayoutParams();
                    cvLP.height = (int) (body.getHeight() * .12f);
                    if (cv == iId) cvLP.bottomMargin = cvLP.height;
                    if (cv == iCode) cvLP.topMargin = cvLP.height;
                    cv.setLayoutParams(cvLP);
                    final float textSize = cvLP.height * (getResources().getInteger(R.integer.textPercentOfField) / 100f);
                    ((TextView) ((ConstraintLayout) cv.getChildAt(0)).getChildAt(0))
                            .setTextSize(textSize);
                    iSwitch.setTextSize(textSize * .75f);
                    iGuide.setTextSize(textSize * .75f);
                }
            }
        });

        // Animate Logo
        final long animateAfter = 3000;
        if (stage == 0) {
            logo.playAnimation();
            new CountDownTimer(animateAfter, animateAfter) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    canMinimize = true;
                    minimizeLogo(whyMinimize);
                }
            }.start();
            for (ComplexView cv : ofFields) cv.setTranslationY(dm.heightPixels * .8f);
        } else {
            logo.setProgress(1f);
            ConstraintLayout.LayoutParams logoLP = (ConstraintLayout.LayoutParams) logo.getLayoutParams();
            logoLP.matchConstraintPercentHeight = endHeight;
            logoLP.verticalBias = endVBias;
            logo.setLayoutParams(logoLP);
            vis(iSubmit, true);
            if (stage == 1) {
                vis(iName, true);
            } else {
                vis(iId, true);
                vis(iCode, true);
            }
            vis(iSwitch, true);
            iSwitch.setText((stage == 1) ? R.string.iGoToSignIn : R.string.iGoToSignUp);
            iSubmitTV.setText((stage == 1) ? R.string.iSignUp : R.string.iSignIn);
            showGuide(false);
        }

        // Submit
        iSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (submitting) return;
                if (stage == 1) signUp(iNameET.getText().toString());
                else if (stage == 2)
                    signIn(iIdET.getText().toString(), iCodeET.getText().toString());
            }
        });
        iSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!switching && !submitting) signUpOrIn(stage != 1);
            }
        });

        // Fonts
        iNameET.setTypeface(fontField);
        iIdET.setTypeface(fontField);
        iCodeET.setTypeface(fontField);
        iSubmitTV.setTypeface(fontField);
        iGuide.setTypeface(fontText);
        iSwitch.setTypeface(fontText);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case permLoc:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    requestLocation(this, navHandler);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == RESULT_OK) start();
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("stage", stage);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoration(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (submitting) return;
        super.onBackPressed();
    }


    void restoration(Bundle state) {
        if (state == null) return;
        if (state.containsKey("stage")) stage = state.getInt("stage", 0);
    }

    void guide(int what) {
        int id = -1;
        String str = "";
        switch (what) {
            case 0:
                id = R.string.iGuideNWCheck;
                break;
            case 1:
                id = R.string.iGuideCheckCredentials;
                break;
            case 2:
                id = R.string.iGuideCredentialsOK;
                break;
            case 3:
                id = R.string.iGuideRegistering;
                break;
            case 4:
                id = R.string.iGuideRegistered;
                break;
            case 5:
                id = R.string.iGuideSigningIn;
                break;
            case 6:
                id = R.string.iGuideSignedIn;
                break;
            case 7:
                id = R.string.iGuideNeedLocation;
                break;
            case 8:
                id = R.string.iGuideDetecting;
                break;
            case 9:
                id = R.string.errNavLoop;
                break;
        }
        if (id != -1) str = getResources().getString(id);
        iGuide.setText(str);
    }

    void showGuide(final boolean b) {
        float alpha = 1f;
        if (!b) alpha = 0f;
        else vis(iGuide, true);
        ValueAnimator an = ObjectAnimator.ofFloat(iGuide, "alpha", alpha);
        an.setDuration(650);
        an.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!b) {
                    vis(iGuide, false);
                    guide(-1);
                }
            }
        });
        an.start();
    }

    void check() {
        if (sp == null || !sp.contains(exCode) || !sp.contains(exNumb)) {
            whyMinimize = 1;
            minimizeLogo(whyMinimize);
            return;
        }
        if (Home.debugIntro) {
            whyMinimize = 1;
            minimizeLogo(whyMinimize);
            return;
        }
        guide(1);
        StringRequest srt = new StringRequest(Request.Method.POST, Fun.cloud + "?action=check2",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        if (res.startsWith("yes")) {
                            guide(2);
                            permit();
                            if (res.length() > 5)
                                sp.edit().putString(exName, res.substring(4, res.length() - 1)).apply();
                        } else checkFailed();//if (res.equals("no")
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                checkFailed();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("code", sp.getString(exCode, ""));
                params.put("numb", sp.getString(exNumb, ""));
                return params;
            }
        };
        srt.setTag("sign");
        srt.setRetryPolicy(new DefaultRetryPolicy(15000, 3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(c).add(srt);
    }

    void checkFailed() {
        iNameET.setText(sp.getString(exNumb, ""));
        iIdET.setText(sp.getString(exNumb, ""));
        iCodeET.setText(sp.getString(exCode, ""));
        whyMinimize = 2;
        minimizeLogo(whyMinimize);
    }

    void minimizeLogo(final int why) {
        if (why == 0 || !canMinimize) return;
        showGuide(false);
        ConstraintSet cs = new ConstraintSet();
        cs.clone(body);
        TransitionManager.beginDelayedTransition(body, new AutoTransition().setDuration(endDur));
        cs.constrainPercentHeight(R.id.logo, endHeight);
        cs.setVerticalBias(R.id.logo, endVBias);
        cs.applyTo(body);

        iName.setTranslationX((why == 1) ? 0f : -dm.widthPixels);
        iId.setTranslationX((why == 1) ? dm.widthPixels : 0f);
        iCode.setTranslationX((why == 1) ? dm.widthPixels : 0f);
        iNameET.setEnabled(why == 1);
        iCodeET.setEnabled(why != 1);
        iIdET.setEnabled(why != 1);
        vis(iSwitch, true);
        vis(iName, true);
        vis(iId, true);
        vis(iCode, true);
        vis(iSubmit, true);
        iSubmitTV.setText((why == 1) ? R.string.iSignUp : R.string.iSignIn);
        iSwitch.setText((stage == 1) ? R.string.iGoToSignIn : R.string.iGoToSignUp);
        AnimatorSet ass = new AnimatorSet();
        ass.setDuration(endDur);
        ValueAnimator[] assess = new ValueAnimator[]{
                ObjectAnimator.ofFloat(iSwitch, "translationY", 0f),
                ObjectAnimator.ofFloat(iName, "translationY", 0f),
                ObjectAnimator.ofFloat(iId, "translationY", 0f),
                ObjectAnimator.ofFloat(iCode, "translationY", 0f),
                ObjectAnimator.ofFloat(iSubmit, "translationY", 0f)
        };
        ass.playTogether(assess);
        ass.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                stage = why;
            }
        });
        ass.start();
    }

    void signUpOrIn(final boolean b) {
        switching = true;
        AnimatorSet ass = new AnimatorSet();
        ass.setDuration(333);
        ValueAnimator[] assess = new ValueAnimator[]{
                ObjectAnimator.ofFloat(iName, "translationX", b ? 0f : -dm.widthPixels),
                ObjectAnimator.ofFloat(iId, "translationX", b ? dm.widthPixels : 0f),
                ObjectAnimator.ofFloat(iCode, "translationX", b ? dm.widthPixels : 0f)
        };
        ass.playTogether(assess);
        ass.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                iSwitch.setText(b ? R.string.iGoToSignIn : R.string.iGoToSignUp);
                iSubmitTV.setText(b ? R.string.iSignUp : R.string.iSignIn);
                iNameET.setEnabled(b);
                iCodeET.setEnabled(!b);
                iIdET.setEnabled(!b);
                stage = b ? 1 : 2;
                switching = false;
            }
        });
        ass.start();
    }

    void signUp(final String name) {
        if (name.equals("")) return;
        submitting(true);
        guide(3);
        StringRequest srt = new StringRequest(Request.Method.POST, Fun.cloud + "?action=sign",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        submitting(false);
                        if (res.startsWith("done")) {
                            String[] gotIt = res.substring(4).split(":");
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString(exCode, gotIt[0]);
                            editor.putString(exNumb, gotIt[1]);
                            editor.putString(exName, name);
                            editor.apply();

                            guide(4);
                            permit();
                        } else Toast.makeText(c, R.string.iSignUpFailed, Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                submitting(false);
                Toast.makeText(c, R.string.iSignUpFailed, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("time", Long.toString(Fun.now()));
                return params;
            }
        };
        srt.setTag("sign");
        srt.setRetryPolicy(new DefaultRetryPolicy(15000, 1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(c).add(srt);
        guide(3);
    }

    void signIn(final String id, final String code) {
        if (id.equals("") || code.length() < 4) return;
        submitting(true);
        guide(5);
        StringRequest srt = new StringRequest(Request.Method.POST, Fun.cloud + "?action=recall",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        submitting(false);
                        if (res.startsWith("yes")) {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString(exCode, code);
                            editor.putString(exNumb, id);
                            if (res.length() > 5)
                                editor.putString(exName, res.substring(4, res.length() - 1));
                            editor.apply();
                            guide(6);
                            permit();
                        } else if (res.equals("no")) {
                            Toast.makeText(c, R.string.iWrongCredentials, Toast.LENGTH_LONG).show();
                        } else Toast.makeText(c, R.string.iSignInFailed, Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                submitting(false);
                Toast.makeText(c, R.string.iSignInFailed, Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("numb", id);
                params.put("code", code);
                return params;
            }
        };
        srt.setTag("sign_in");
        srt.setRetryPolicy(new DefaultRetryPolicy(15000, 1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(c).add(srt);
    }

    ValueAnimator anSubmitting = null;

    void submitting(boolean b) {
        submitting = b;
        iSubmitTV.setAlpha(b ? 0f : 1f);
        iSubmitting.setAlpha(b ? 1f : 0f);
        if (b) {
            if (anSubmitting == null) {
                anSubmitting = ObjectAnimator.ofFloat(iSubmitting, "rotation", 0f, 360f);
                anSubmitting.setDuration(343);
                anSubmitting.setInterpolator(new LinearInterpolator());
                anSubmitting.setRepeatCount(Animation.INFINITE);
                anSubmitting.start();
            } else anSubmitting.resume();
        } else if (anSubmitting != null) anSubmitting.pause();
    }

    void permit() {
        if (iGuide.getVisibility() == View.GONE) showGuide(true);
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(that,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, permLoc);
            guide(7);
        } else requestLocation(this, navHandler);
    }

    void start() {
        guide(8);
        final Intent intent = new Intent(this, Home.class);
        final Intro it = this;
        new Nav(new Fun.Function() {// On Success
            @Override
            public void execute() {
                Fun.goTo(that, intent);
            }
        }, new Fun.Function() {// On Failure
            @Override
            public void execute() {
                guide(9);
                new CountDownTimer(5000, 5000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        it.start();
                    }
                }.start();
            }
        });
    }
}
