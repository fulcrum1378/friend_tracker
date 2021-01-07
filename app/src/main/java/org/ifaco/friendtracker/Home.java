package org.ifaco.friendtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.ifaco.friendtracker.etc.Navigator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.ifaco.friendtracker.FriAdap.accept;
import static org.ifaco.friendtracker.Fun.c;
import static org.ifaco.friendtracker.Fun.compileTime;
import static org.ifaco.friendtracker.Fun.configureNav;
import static org.ifaco.friendtracker.Fun.connected;
import static org.ifaco.friendtracker.Fun.cover;
import static org.ifaco.friendtracker.Fun.dirLtr;
import static org.ifaco.friendtracker.Fun.dm;
import static org.ifaco.friendtracker.Fun.doNothing;
import static org.ifaco.friendtracker.Fun.dp;
import static org.ifaco.friendtracker.Fun.exCode;
import static org.ifaco.friendtracker.Fun.exName;
import static org.ifaco.friendtracker.Fun.exNumb;
import static org.ifaco.friendtracker.Fun.fontField;
import static org.ifaco.friendtracker.Fun.fontLogo;
import static org.ifaco.friendtracker.Fun.fontText;
import static org.ifaco.friendtracker.Fun.loadAvatar;
import static org.ifaco.friendtracker.Fun.sp;
import static org.ifaco.friendtracker.Nav.ftInterval;
import static org.ifaco.friendtracker.Nav.here;
import static org.ifaco.friendtracker.etc.AlertDialogue.alertDialogue;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public class Home extends AppCompatActivity {
    ConstraintLayout body, toolbar, nav, dFriends;
    ImageView tbNav, radar, navLogo, dFriendsOverflow;
    RelativeLayout tbRadar;
    TextView tbTitle, navTitleText, navVersion, dFriendsTitle, dfRequest;
    SupportMapFragment fMap;
    View cover, navShadow;
    LinearLayout navItems;
    RecyclerView rvFriends;

    public static final boolean debugIntro = false;
    AppCompatActivity that;
    GoogleMap map;
    boolean navigating = false, firstBackToExit = false, showFriends = false, showNav = false;
    public static ArrayList<Friend> friends = new ArrayList<>();
    public static ArrayList<Marker> fMarkers = new ArrayList<>();
    public static ArrayList<Long> fMarkerIds = new ArrayList<>();
    public static FriAdap fAdapter;
    public static Handler nwHandler = null, navHandler = null, syncHandler = null;
    AnimatorSet friendsShower;
    public static CountDownTimer navigator = null;
    Fun.Function onMapLoaded = null;
    public static Set<String> friendsOff = new HashSet<>(), fPinned = new HashSet<>();
    public static final String exFriendsOff = "friendsOff", exFPinned = "fPinned";


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        body = findViewById(R.id.body);
        toolbar = findViewById(R.id.toolbar);
        tbNav = findViewById(R.id.tbNav);
        tbTitle = findViewById(R.id.tbTitle);
        tbRadar = findViewById(R.id.tbRadar);
        radar = findViewById(R.id.radar);
        fMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fMap);
        cover = findViewById(R.id.cover);
        nav = findViewById(R.id.nav);
        navLogo = findViewById(R.id.navLogo);
        navTitleText = findViewById(R.id.navTitleText);
        navVersion = findViewById(R.id.navVersion);
        navItems = findViewById(R.id.navItems);
        navShadow = findViewById(R.id.navShadow);
        dFriends = findViewById(R.id.dFriends);
        dFriendsOverflow = findViewById(R.id.dFriendsOverflow);
        dFriendsTitle = findViewById(R.id.dFriendsTitle);
        rvFriends = findViewById(R.id.rvFriends);
        dfRequest = findViewById(R.id.dfRequest);

        that = Fun.init(this, body);
        fMarkers = new ArrayList<>();
        fMarkerIds = new ArrayList<>();
        restoration(savedInstanceState);
        Fun.nav(that, nav, 0);
        friendsOff = sp.getStringSet(exFriendsOff, null);
        fPinned = sp.getStringSet(exFPinned, null);


        // Authorization & Initial Location
        if (sp == null || !sp.contains(exCode) || !sp.contains(exNumb) || here == null) {
            cut();
            return;
        }

        // Handlers
        nwHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (!connected)
                    Toast.makeText(c, R.string.turnNWOn, Toast.LENGTH_LONG).show();
            }
        };
        navHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 0:
                        //if (tbRadar != null) Fun.explode(tbRadar, 684, 3f);
                        break;
                    case 1:
                        update();
                        break;
                }
            }
        };
        syncHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                ArrayList<Friend> friendsBefore = new ArrayList<>(friends);
                try {
                    friends = Friend.parse(Fun.jsonReader(((String) msg.obj).substring(4)));
                } catch (IOException ignored) {
                }
                arrangeFriends();
                safelyUpdateMap(friendsBefore, false);
            }
        };

        // Map
        final AppCompatActivity THIS = this;
        fMap.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.setMapStyle(MapStyleOptions.loadRawResourceStyle(c, R.raw.map));
                map.setMyLocationEnabled(true);
                if (onMapLoaded != null) {
                    onMapLoaded.execute();
                    eventuallyRemoveOnMapLoaded();
                } else {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    if (here != null)
                        builder.include(new LatLng(here.getLatitude(), here.getLongitude()));
                    LatLngBounds bounds = builder.build();
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,
                            dm.widthPixels, dm.heightPixels, (int) (dm.widthPixels * 0.16)));
                }
                map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker arg0) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        LinearLayout info = new LinearLayout(c);
                        info.setLayoutParams(new ViewGroup.LayoutParams(
                                dp(248), ViewGroup.LayoutParams.WRAP_CONTENT));
                        info.setOrientation(LinearLayout.VERTICAL);
                        info.setPadding(dp(10), dp(8), dp(10), dp(10));
                        info.setBackgroundColor(ContextCompat.getColor(c, R.color.mInfoBG));
                        info.setGravity(Gravity.CENTER);

                        ImageView avatar = new ImageView(c);
                        LinearLayout.LayoutParams avatarLL = new LinearLayout.LayoutParams(dp(60), dp(60));
                        avatar.setLayoutParams(avatarLL);
                        avatar.setImageResource(R.drawable.default_user_1);
                        info.addView(avatar);
                        loadAvatar(THIS, avatar, marker.getSnippet()
                                .substring(marker.getSnippet().lastIndexOf(" ") + 1));

                        TextView title = new TextView(c);
                        title.setTextColor(ContextCompat.getColor(c, R.color.mInfoTitle));
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setTextSize(dm.density * 8f);
                        title.setText(marker.getTitle());
                        title.setTypeface(fontText, Typeface.BOLD);
                        info.addView(title);

                        TextView snippet = new TextView(c);
                        snippet.setPadding(0, dp(7), 0, 0);
                        snippet.setTextColor(ContextCompat.getColor(c, R.color.mInfoSnippet));
                        snippet.setGravity(Gravity.CENTER);
                        snippet.setTextSize(dm.density * 5.5f);
                        snippet.setText(marker.getSnippet());
                        snippet.setLineSpacing(dp(27), 0);
                        snippet.setTypeface(fontText);
                        info.addView(snippet);
                        return info;
                    }
                });
            }
        });

        // Profile Picture
        tbRadar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFriends(true);
            }
        });

        // Friends
        dFriends.setOnClickListener(doNothing);
        cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showFriends) showFriends(false);
                else if (showNav) showNav(false);
            }
        });
        dfRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText etReq = new EditText(c);
                ViewGroup.LayoutParams etReqLP = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                etReq.setLayoutParams(etReqLP);// Parent: FrameLayout
                etReq.setTextColor(ContextCompat.getColor(c, R.color.dialogue));
                etReq.setInputType(InputType.TYPE_CLASS_NUMBER);
                etReq.setMaxLines(1);
                etReq.setMaxEms(10);
                etReq.setTextSize(dm.density * 8.5f);
                etReq.setBackgroundColor(ContextCompat.getColor(c, R.color.adEditTextBG));
                etReq.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
                etReq.setId(R.id.etReq);
                etReq.setTypeface(fontField);
                etReq.setPaddingRelative(dp(15), dp(10), dp(15), dp(10));

                alertDialogue(that, R.string.request, R.string.requestMsg, etReq, true,
                        R.string.send, R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (etReq.getText().toString().equals("")) return;
                                try {
                                    request(Long.parseLong(etReq.getText().toString()));
                                } catch (Exception ignored) {
                                }
                            }
                        }, null);
                try {
                    ((ViewGroup) etReq.getParent()).setPaddingRelative(dp(20), 0, dp(20), 0);
                } catch (Exception ignored) {// MarginLayoutParams is useless!
                }
            }
        });
        dFriendsTitle.setTypeface(fontText);
        dfRequest.setTypeface(fontText);
        float[] putFriends = transFriends();
        dFriends.setTranslationX(putFriends[0]);
        dFriends.setTranslationY(putFriends[1]);

        // Friends Overflow
        dFriendsOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(c, v);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.fofRefresh:
                                update();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                pm.inflate(R.menu.friends_overflow);
                pm.show();
            }
        });

        // Toolbar
        tbNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNav(true);
            }
        });
        if (that.getResources().getBoolean(R.bool.boldLogo))
            tbTitle.setTypeface(fontLogo, Typeface.BOLD);
        else tbTitle.setTypeface(fontLogo);
        configureNav(this, nav, navLogo, navTitleText, navVersion, navShadow);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Navigation
        if (!navigating) navigate();

        // Identity
        dFriendsTitle.setText(getString(R.string.me, sp.getString(exName, c.getResources().getString(R.string.unknownMe)),
                sp.getString(exNumb, getResources().getString(R.string.unknownId))));
        String numb = sp.getString(exNumb, null);
        Fun.loadAvatar(this, radar, numb);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (navigating) try {
            navigator.cancel();
        } catch (Exception ignored) {
        }
        navigator = null;
        navigating = false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Friend[] arrFriends = new Friend[friends.size()];
        outState.putParcelableArray("friends", friends.toArray(arrFriends));
        outState.putBoolean("showNav", showNav);
        outState.putBoolean("showFriends", showFriends);
        if (map != null)
            outState.putParcelable("cameraPosition", map.getCameraPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoration(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (showFriends) {
            showFriends(false);
            return;
        }
        if (showNav) {
            showNav(false);
            return;
        }
        if (!firstBackToExit) {
            firstBackToExit = true;
            Toast.makeText(c, R.string.toExit, Toast.LENGTH_SHORT).show();
            new CountDownTimer(4000, 4000) {
                @Override
                public void onTick(long l) {
                }

                @Override
                public void onFinish() {
                    firstBackToExit = false;
                }
            }.start();
            return;
        }
        exit();
    }


    void restoration(Bundle state) {
        if (state == null) return;
        if (state.containsKey("friends")) try {
            Friend[] arrFriends = (Friend[]) state.getParcelableArray("friends");
            if (arrFriends != null) friends = new ArrayList<>(Arrays.asList(arrFriends));
            else friends = null;
            safelyUpdateMap(friendsBefore(state.containsKey("cameraPosition")), true);
        } catch (Exception ignored) {
        }
        if (state.containsKey("showNav")) showNav(state.getBoolean("showNav", false));
        if (state.containsKey("showFriends"))
            showFriends(state.getBoolean("showFriends", false));
        if (state.containsKey("cameraPosition") && map != null)
            map.moveCamera(CameraUpdateFactory.newCameraPosition(
                    (CameraPosition) state.getParcelable("cameraPosition")));
    }

    void showFriends(boolean b) {
        showFriends = b;
        cover(cover, showFriends || showNav);
        if (friendsShower != null) {
            try {
                friendsShower.cancel();
            } catch (Exception ignored) {
            }
            friendsShower = null;
        }
        if (b) Fun.vis(dFriends, true);
        friendsShower = new AnimatorSet();
        float[] go = transFriends();
        friendsShower.playTogether(
                ObjectAnimator.ofFloat(dFriends, "rotation", b ? 0f : 540f),
                ObjectAnimator.ofFloat(dFriends, "translationX", b ? 0f : go[0]),
                ObjectAnimator.ofFloat(dFriends, "translationY", b ? 0f : go[1]),
                ObjectAnimator.ofFloat(dFriends, "alpha", b ? 1f : 0f),
                ObjectAnimator.ofFloat(dFriends, "scaleX", b ? 1f : 0f),
                ObjectAnimator.ofFloat(dFriends, "scaleY", b ? 1f : 0f)
        );
        friendsShower.setDuration(450);
        friendsShower.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!showFriends) Fun.vis(dFriends, false);
            }
        });
        friendsShower.start();
    }

    float[] transFriends() {
        return new float[]{
                dirLtr ? ((float) dm.widthPixels / 2f) - (tbRadar.getWidth() / 2f)
                        : -(((float) dm.widthPixels / 2f) - (tbRadar.getWidth() / 2f)),
                -(((float) dm.heightPixels / 2f) - (tbRadar.getHeight() / 2f))
        };
    }

    void showNav(boolean b) {
        showNav = b;
        cover(cover, showFriends || showNav);
        Fun.navDrawer(nav, navShadow, b);
    }

    void navigate() {
        navigating = true;
        update();
        navigator = new CountDownTimer(ftInterval, ftInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                navigate();
            }
        }.start();
    }

    public static void update() {
        if (!connected) return;
        new Nav(new Fun.Function() {// On Success
            @Override
            public void execute() {
                Navigator.sync(here.getLatitude(), here.getLongitude());
            }
        }, new Fun.Function() {
            @Override
            public void execute() {
                Toast.makeText(c, c.getResources().getString(R.string.errNavLoop),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    void safelyUpdateMap(final ArrayList<Friend> friendsBefore, final boolean neverZoom) {
        if (map != null) {
            if (markers(friendsBefore, neverZoom)) zoom();
            eventuallyRemoveOnMapLoaded();
        } else onMapLoaded = new Fun.Function() {
            @Override
            public void execute() {
                if (markers(friendsBefore, neverZoom)) zoom();
            }
        };
    }

    public static ArrayList<Friend> friendsBefore(boolean doI) {
        ArrayList<Friend> friendsBefore = null;
        if (doI) {
            friendsBefore = new ArrayList<>();
            friendsBefore.add(new Friend(
                    0, "", 0, 0.0, 0.0, false, false, false, false));
        }
        return friendsBefore;
    }

    void eventuallyRemoveOnMapLoaded() {
        new CountDownTimer(500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                onMapLoaded = null;
            }
        }.start();
    }

    void arrangeFriends() {
        if (friends == null) return;
        try {
            Collections.sort(friends, new Friend.SortFriends(1));
            Collections.sort(friends, new Friend.SortFriends(5));
            fAdapter = new FriAdap(friends, that);
            rvFriends.setAdapter(fAdapter);
        } catch (Exception ignored) {
            Toast.makeText(c, R.string.couldNotList, Toast.LENGTH_SHORT).show();
        }
    }

    boolean markers(ArrayList<Friend> friendsBefore, boolean neverZoom) {
        ArrayList<Long> notChanged = new ArrayList<>();
        boolean doZoom = false;
        for (int m = fMarkers.size() - 1; m >= 0; m--) {
            Friend f = Friend.findFriendById(fMarkerIds.get(m), friends);
            boolean friendChanged = false;
            if (f != null && friendsBefore != null) {
                Friend o = Friend.findFriendById(f.id, friendsBefore);
                if (o != null) friendChanged = f.notEqual(o);
            }
            if (f == null || friendChanged) {
                fMarkers.get(m).remove();
                fMarkers.remove(m);
                fMarkerIds.remove(m);
                doZoom = true;
            } else notChanged.add(f.id);
        }
        for (Friend f : friends)
            if (!notChanged.contains(f.id) && f.lat != 1000.0 && f.lng != 1000.0 && f.status && map != null) {
                doZoom = true;
                LatLng coor = new LatLng(f.lat, f.lng);
                Marker newMar = map.addMarker(new MarkerOptions()
                        .position(coor)
                        .title(f.name)
                        .snippet(compileTime(f.lastSync) + Fun.dist(coor) +
                                getString(R.string.friendId, f.id))
                );
                newMar.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_1));
                if (!f.isOn) newMar.setVisible(false);
                fMarkers.add(newMar);
                fMarkerIds.add(f.id);
            }
        return doZoom && !neverZoom;
    }

    void zoom() {
        if ((here == null && friends == null) || map == null) return;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (here != null) builder.include(new LatLng(here.getLatitude(), here.getLongitude()));
        if (fMarkers != null) for (Marker m : fMarkers)
            if (m.isVisible()) builder.include(m.getPosition());
        LatLngBounds bounds = builder.build();
        int sWisth = dm.widthPixels, sHeight = dm.heightPixels;
        if (body.getWidth() > 0) sWisth = body.getWidth();
        if (body.getHeight() > 0) sHeight = body.getHeight();
        int padding = (int) (sWisth * 0.12); // offset from edges of the map 12% of screen
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, sWisth, sHeight, padding);
        map.animateCamera(cu);
    }

    void request(final long id) {
        final String myOwnID = sp.getString(exNumb, "");
        if (Long.toString(id).equals(myOwnID)) {
            Toast.makeText(c, R.string.requestedYourself, Toast.LENGTH_LONG).show();
            return;
        }
        if (friends != null) {
            final Friend already = Friend.findFriendById(id, friends);
            if (already != null) {
                if (already.status)
                    Toast.makeText(c, R.string.alreadyFriends, Toast.LENGTH_LONG).show();
                else if (already.fromMe)
                    Toast.makeText(c, R.string.alreadyRequested, Toast.LENGTH_LONG).show();
                else alertDialogue(this, R.string.accept, R.string.requestOnRequest,
                            null, false, R.string.yes, R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    accept(already);
                                }
                            }, null);
                return;// Logically impossible to cross
            }
        }
        StringRequest srt = new StringRequest(Request.Method.POST, Fun.cloud + "?action=request",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        switch (res) {
                            case "done":
                                Toast.makeText(c, R.string.done, Toast.LENGTH_LONG).show();
                                Home.navHandler.obtainMessage(1).sendToTarget();
                                break;
                            case "notExist":
                                Toast.makeText(c, R.string.notExist, Toast.LENGTH_LONG).show();
                                break;
                            default://R.string.anError
                                Toast.makeText(c, res, Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(c, R.string.anError, Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("code", sp.getString(exCode, ""));
                params.put("numb", myOwnID);
                params.put("req", Long.toString(id));
                return params;
            }
        };
        srt.setTag("request");
        srt.setRetryPolicy(new DefaultRetryPolicy((int) ftInterval, 1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(c).add(srt);
    }

    void cut() {
        try {
            super.onBackPressed();
            //startActivity(new Intent(c, Intro.class));
        } catch (Exception ignored) {
            exit();
        }
    }

    void exit() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
