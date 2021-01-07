package org.ifaco.friendtracker.nav;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.ifaco.friendtracker.Alarm;
import org.ifaco.friendtracker.Fun;
import org.ifaco.friendtracker.Nav;
import org.ifaco.friendtracker.R;

import java.util.HashMap;
import java.util.Map;

import static org.ifaco.friendtracker.Fun.c;
import static org.ifaco.friendtracker.Fun.configureNav;
import static org.ifaco.friendtracker.Fun.cover;
import static org.ifaco.friendtracker.Fun.dm;
import static org.ifaco.friendtracker.Fun.exCode;
import static org.ifaco.friendtracker.Fun.exName;
import static org.ifaco.friendtracker.Fun.exNumb;
import static org.ifaco.friendtracker.Fun.fontField;
import static org.ifaco.friendtracker.Fun.fontLogo;
import static org.ifaco.friendtracker.Fun.fontText;
import static org.ifaco.friendtracker.Fun.onLoad;
import static org.ifaco.friendtracker.Fun.sp;

public class Settings extends AppCompatActivity {
    ConstraintLayout body, toolbar, nav;
    ImageView tbNav, navLogo, sCAImage, sCAImageHover;
    TextView tbTitle, sCATitle, sCNTitle, sCNSubmit, sCPTitle, sCPSubmit, sFTTitle, sFTDesc, sBTTitle, sBTDesc,
            navTitleText, navVersion;
    ScrollView sSV;
    LinearLayout sLL, navItems;
    EditText sCNEdit, sCPOld, sCPNew, sCPRep, sFTEdit, sBTEdit;
    View cover, navShadow;

    AppCompatActivity that;
    boolean showNav = false, changingPass = false, changingName = false, changedFT = false,
            changedBT = false, changingAvatar = false;
    final int pickAvatarReq = 666;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        body = findViewById(R.id.body);
        toolbar = findViewById(R.id.toolbar);
        tbNav = findViewById(R.id.tbNav);
        tbTitle = findViewById(R.id.tbTitle);
        sSV = findViewById(R.id.sSV);
        sLL = findViewById(R.id.sLL);
        sCATitle = findViewById(R.id.sCATitle);
        sCAImage = findViewById(R.id.sCAImage);
        sCAImageHover = findViewById(R.id.sCAImageHover);
        sCNTitle = findViewById(R.id.sCNTitle);
        sCNEdit = findViewById(R.id.sCNEdit);
        sCNSubmit = findViewById(R.id.sCNSubmit);
        sCPTitle = findViewById(R.id.sCPTitle);
        sCPOld = findViewById(R.id.sCPOld);
        sCPNew = findViewById(R.id.sCPNew);
        sCPRep = findViewById(R.id.sCPRep);
        sCPSubmit = findViewById(R.id.sCPSubmit);
        sFTTitle = findViewById(R.id.sFTTitle);
        sFTEdit = findViewById(R.id.sFTEdit);
        sFTDesc = findViewById(R.id.sFTDesc);
        sBTTitle = findViewById(R.id.sBTTitle);
        sBTEdit = findViewById(R.id.sBTEdit);
        sBTDesc = findViewById(R.id.sBTDesc);
        cover = findViewById(R.id.cover);
        nav = findViewById(R.id.nav);
        navLogo = findViewById(R.id.navLogo);
        navTitleText = findViewById(R.id.navTitleText);
        navVersion = findViewById(R.id.navVersion);
        navItems = findViewById(R.id.navItems);
        navShadow = findViewById(R.id.navShadow);

        that = Fun.init(this, body);
        getWindow().getDecorView().setBackgroundColor(
                ContextCompat.getColor(c, R.color.otherPagesBG));
        restoration(savedInstanceState);
        Fun.nav(that, nav, 1);
        changedFT = false;
        changedBT = false;


        // Authorization & Initial Location
        if (Fun.sp == null || !Fun.sp.contains(exCode) || !Fun.sp.contains(exNumb)) {
            onBackPressed();
            return;
        }

        // Load
        onLoad(sLL, new Fun.Function() {
            @Override
            public void execute() {
                ConstraintLayout.LayoutParams sSVLP = (ConstraintLayout.LayoutParams) sSV.getLayoutParams();
                final float pWidth = sSVLP.matchConstraintPercentWidth;
                sSVLP.matchConstraintPercentWidth = 1;
                sSV.setLayoutParams(sSVLP);
                for (int l = 0; l < sLL.getChildCount(); l++) {
                    View v = sLL.getChildAt(l);
                    LinearLayout.LayoutParams llLP = (LinearLayout.LayoutParams) v.getLayoutParams();
                    llLP.width = (int) (dm.widthPixels * pWidth);
                    v.setLayoutParams(llLP);
                }
            }
        });

        // Toolbar
        tbNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNav(true);
            }
        });
        cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showNav) showNav(false);
            }
        });
        tbTitle.setTypeface(fontLogo);
        configureNav(this, nav, navLogo, navTitleText, navVersion, navShadow);

        // Fonts
        TextView[] fTextBold = new TextView[]{sCATitle, sCNTitle, sCPTitle, sFTTitle, sBTTitle},
                fField = new TextView[]{sCNEdit, sCPOld, sCPNew, sCPRep, sFTEdit, sFTDesc,
                        sBTEdit, sBTDesc},
                fFieldBold = new TextView[]{sCNSubmit, sCPSubmit};
        for (TextView tv : fTextBold) tv.setTypeface(fontText, Typeface.BOLD);
        for (TextView tv : fField) tv.setTypeface(fontField);
        for (TextView tv : fFieldBold) tv.setTypeface(fontField, Typeface.BOLD);

        // Change Visible Name & Password
        sCNSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeName(sCNEdit.getText().toString());
            }
        });
        sCPSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePass(sCPOld.getText().toString(),
                        sCPNew.getText().toString(), sCPRep.getText().toString());
            }
        });

        // Change Tracking
        sFTEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    long interval = Long.parseLong(s.toString());
                    if (interval < 1 || interval > 100) return;
                    interval = 3600000L / interval;
                    sp.edit().putLong(Nav.exFTNumber, interval).apply();
                    Nav.ftInterval = interval;
                    changedFT = true;
                } catch (Exception ignored) {
                }
            }
        });
        sBTEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    long interval = Long.parseLong(s.toString());
                    if (interval < 0 || interval > 100) return;
                    if (interval == 0) interval = -1;
                    else interval = 604800000L / interval;
                    sp.edit().putLong(Nav.exBTNumber, interval).apply();
                    Nav.btInterval = interval;
                    changedBT = true;
                } catch (Exception ignored) {
                }
            }
        });

        // Avatar
        sCAImageHover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(c, v);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.savUpload:
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(intent, pickAvatarReq);
                                return true;
                            case R.id.savRemove:
                                removeAvatar();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                pm.inflate(R.menu.s_avatar);
                pm.show();

                if (!Fun.hasAvatar) pm.getMenu().findItem(R.id.savRemove).setVisible(false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sCNEdit.setText(sp.getString(Fun.exName, ""));
        sCPOld.setText(sp.getString(Fun.exCode, ""));
        String[] inter = interval();
        sFTEdit.setText(inter[0]);
        sBTEdit.setText(inter[1]);
        reloadAvatar();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("showNav", showNav);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoration(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (changedFT) Nav.requestLocation(this, null);
        if (changedBT) Alarm.awaken(c);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pickAvatarReq) {
            if (resultCode != RESULT_OK && data != null && data.getData() != null) try {
                changingAvatar = true;
                uploadAvatar(BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData())));
            } catch (Exception ignored) {
                Toast.makeText(c, R.string.couldNotLoadAvatar, Toast.LENGTH_LONG).show();
                changingAvatar = false;
            }
            else Toast.makeText(c, R.string.couldNotLoadAvatar, Toast.LENGTH_LONG).show();
        }
    }


    void restoration(Bundle state) {
        if (state == null) return;
        if (state.containsKey("showNav")) showNav(state.getBoolean("showNav", false));
    }

    void showNav(boolean b) {
        showNav = b;
        cover(cover, showNav);
        Fun.navDrawer(nav, navShadow, b);
    }

    void uploadAvatar(Bitmap icon) {
        if (icon == null) {
            Toast.makeText(c, R.string.couldNotLoadAvatar, Toast.LENGTH_LONG).show();
            return;
        }
        if (icon.getWidth() != icon.getHeight()) {
            int[] size, position = new int[]{0, 0};
            if (icon.getWidth() > icon.getHeight()) {
                size = new int[]{icon.getHeight(), icon.getHeight()};
                position[0] = (icon.getWidth() - icon.getHeight()) / 2;
            } else {
                size = new int[]{icon.getWidth(), icon.getWidth()};
                position[1] = (icon.getHeight() - icon.getWidth()) / 2;
            }
            icon = Bitmap.createBitmap(icon, position[0], position[1], size[0], size[1]);
        }
        final Bitmap ICON = icon;
        changingAvatar = true;
        StringRequest srt = new StringRequest(Request.Method.POST, Fun.cloud + "?action=upload_avatar",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        changingAvatar = false;
                        if (res.equals("done")) reloadAvatar();
                        else Toast.makeText(c, R.string.anError, Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                changingAvatar = false;
                Toast.makeText(c, R.string.anError, Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("code", sp.getString(Fun.exCode, ""));
                params.put("numb", sp.getString(Fun.exNumb, ""));
                params.put("new", Fun.imageToString(ICON));
                return params;
            }
        };
        srt.setTag("upload_avatar");
        srt.setRetryPolicy(new DefaultRetryPolicy(10000, 1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(c).add(srt);
    }

    void removeAvatar() {
        changingAvatar = true;
        StringRequest srt = new StringRequest(Request.Method.POST, Fun.cloud + "?action=remove_avatar",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        changingAvatar = false;
                        if (res.equals("done"))
                            sCAImage.setImageResource(R.drawable.default_user_1);
                        else Toast.makeText(c, R.string.anError, Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                changingAvatar = false;
                Toast.makeText(c, R.string.anError, Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("code", sp.getString(Fun.exCode, ""));
                params.put("numb", sp.getString(Fun.exNumb, ""));
                return params;
            }
        };
        srt.setTag("remove_avatar");
        srt.setRetryPolicy(new DefaultRetryPolicy(10000, 1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(c).add(srt);
    }

    void changeName(final String str) {
        if (changingName) return;
        if (str.equals("")) return;
        if (str.equals(sp.getString(exName, ""))) return;
        changingName = true;
        StringRequest srt = new StringRequest(Request.Method.POST, Fun.cloud + "?action=change_name",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        changingName = false;
                        if (res.equals("done")) {
                            sp.edit().putString(exName, str).commit();
                            sCNEdit.setText(str);
                            Toast.makeText(c, R.string.done, Toast.LENGTH_LONG).show();
                        } else Toast.makeText(c, R.string.anError, Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                changingName = false;
                Toast.makeText(c, R.string.anError, Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("code", sp.getString(Fun.exCode, ""));
                params.put("numb", sp.getString(Fun.exNumb, ""));
                params.put("new", str);
                return params;
            }
        };
        srt.setTag("change_name");
        srt.setRetryPolicy(new DefaultRetryPolicy(10000, 1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(c).add(srt);
    }

    void changePass(final String old, final String str, final String rep) {
        if (changingPass) return;
        if (str.equals("")) return;
        if (!str.equals(rep)) {
            Toast.makeText(c, R.string.sCPNotMatch, Toast.LENGTH_SHORT).show();
            return;
        }
        if (str.length() < 4) {
            Toast.makeText(c, R.string.sCPMin, Toast.LENGTH_SHORT).show();
            return;
        }
        if (str.length() > 10) {
            Toast.makeText(c, R.string.sCPMax, Toast.LENGTH_SHORT).show();
            return;
        }
        changingPass = true;
        StringRequest srt = new StringRequest(Request.Method.POST, Fun.cloud + "?action=change_pass",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        changingPass = false;
                        if (res.equals("done")) {
                            sp.edit().putString(exCode, str).commit();
                            sCPOld.setText(str);
                            sCPNew.setText("");
                            sCPRep.setText("");
                            Toast.makeText(c, R.string.done, Toast.LENGTH_LONG).show();
                        } else Toast.makeText(c, R.string.anError, Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                changingPass = false;
                Toast.makeText(c, R.string.anError, Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("code", old);
                params.put("numb", sp.getString(Fun.exNumb, ""));
                params.put("new", str);
                return params;
            }
        };
        srt.setTag("change_pass");
        srt.setRetryPolicy(new DefaultRetryPolicy(10000, 1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(c).add(srt);
    }

    String[] interval() {
        long btInt = sp.getLong(Nav.exBTNumber, Nav.defBT);
        if (btInt == -1) btInt = 0;
        else btInt = 604800000L / btInt;
        //1  -> 604800000 -> 1            1  -> 3600000 -> 1
        //7  -> 86400000  -> 7            30 -> 120000  -> 30
        //14 -> 43200000  -> 14           60 -> 60000   -> 60
        return new String[]{
                Long.toString(3600000L / sp.getLong(Nav.exFTNumber, Nav.defFT)),
                Long.toString(btInt)
        };
    }

    void reloadAvatar() {
        String numb = sp.getString(exNumb, null);
        Fun.loadAvatar(this, sCAImage, numb);
    }
}
