package org.ifaco.friendtracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.ifaco.friendtracker.Fun.c;
import static org.ifaco.friendtracker.Fun.fontText;
import static org.ifaco.friendtracker.Fun.night;
import static org.ifaco.friendtracker.Fun.sp;
import static org.ifaco.friendtracker.Home.fAdapter;
import static org.ifaco.friendtracker.Home.fMarkerIds;
import static org.ifaco.friendtracker.Home.fMarkers;
import static org.ifaco.friendtracker.Home.fPinned;
import static org.ifaco.friendtracker.Home.friends;
import static org.ifaco.friendtracker.Home.friendsOff;
import static org.ifaco.friendtracker.etc.AlertDialogue.alertDialogue;

class FriAdap extends RecyclerView.Adapter<FriAdap.MyViewHolder> {
    private final ArrayList<Friend> data;
    private final AppCompatActivity that;

    public FriAdap(ArrayList<Friend> data, AppCompatActivity that) {
        this.data = data;
        this.that = that;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout cl;

        MyViewHolder(ConstraintLayout cl) {
            super(cl);
            this.cl = cl;
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout cl = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new MyViewHolder(cl);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder h, int i) {
        TextView name = (TextView) h.cl.getChildAt(0);
        ImageView marker = (ImageView) h.cl.getChildAt(1);
        Friend f = data.get(i);

        // BG
        if (f.pinned) h.cl.setBackgroundResource(R.drawable.hover_tp_darken_2);

        // Texts
        if (f.status || !f.fromMe) name.setText(f.name);
        else name.setText(c.getString(R.string.unknownInvited, (int) f.id));
        name.setTypeface(fontText);

        // Icons
        int icon = R.drawable.marker_1;
        if (!f.status) icon = f.fromMe ? R.drawable.requested_1 : R.drawable.request_1;
        marker.setImageResource(icon);
        if (!night) {
            if (f.isOn || !f.status) marker.clearColorFilter();
            else marker.setColorFilter(new PorterDuffColorFilter(
                    ContextCompat.getColor(c, R.color.markerOff), PorterDuff.Mode.SRC_IN));
        } else marker.setColorFilter(new PorterDuffColorFilter(
                ContextCompat.getColor(c, (f.isOn || !f.status) ? R.color.CP : R.color.markerOff), PorterDuff.Mode.SRC_IN));

        // Clicks
        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = h.getLayoutPosition();
                final Friend f = data.get(h.getLayoutPosition());
                int iOfFriends = friends.indexOf(Friend.findFriendById(f.id, friends));
                if (friends.get(iOfFriends).lat == 1000.0 || friends.get(iOfFriends).lng == 1000.0) {
                    Toast.makeText(c, R.string.noData, Toast.LENGTH_SHORT).show();
                    return;
                }
                int iOfMarkers = fMarkerIds.indexOf(friends.get(pos).id);
                if (iOfMarkers == -1 || fMarkers == null || friends == null) return;
                boolean newB = !fMarkers.get(iOfMarkers).isVisible();
                Marker marker = fMarkers.get(iOfMarkers);
                marker.setVisible(newB);
                fMarkers.set(iOfMarkers, marker);
                Friend ff = friends.get(iOfFriends);
                ff.setOn(newB);
                friends.set(iOfFriends, ff);
                fAdapter.notifyItemChanged(pos);

                if (friendsOff == null) friendsOff = new HashSet<>();
                String sId = Long.toString(friends.get(pos).id);
                if (!newB) friendsOff.add(sId);// Sets don't allow duplicates!
                else friendsOff.remove(sId);// Null-Safe
                SharedPreferences.Editor editor = sp.edit();
                editor.remove(Home.exFriendsOff);
                editor.commit();// NECESSARY (for putStringSet trap)
                editor.putStringSet(Home.exFriendsOff, friendsOff);
                editor.commit();
            }
        };
        if (!f.status && f.fromMe) onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogue(that, R.string.request, R.string.waitingMsg,
                        true, null);
            }
        };
        if (!f.status && !f.fromMe) onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askAccept(data.get(h.getLayoutPosition()));
            }
        };
        h.cl.setOnClickListener(onClick);
        View.OnLongClickListener onLongClick;
        if (f.status) onLongClick = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Friend f = data.get(h.getLayoutPosition());
                final Intent view = new Intent(Intent.ACTION_VIEW);
                view.setData(Uri.parse("geo:0,0?q=" + f.lat + "," + f.lng + "(" + f.name + ")"));

                PopupMenu pm = new PopupMenu(c, v);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.iflViewInMaps:
                                if (view.resolveActivity(c.getPackageManager()) != null)
                                    that.startActivity(view);
                                return true;
                            case R.id.iflShare:
                                Intent share = new Intent(Intent.ACTION_SEND);
                                share.setType("text/plain");
                                share.putExtra(Intent.EXTRA_TEXT,
                                        "https://www.google.com/maps/?q=" + f.lat + "," + f.lng);
                                that.startActivity(Intent.createChooser(share,
                                        c.getResources().getString(R.string.iflShare)));
                                return true;
                            case R.id.iflDirections:
                                Intent direct = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("google.navigation:q=" + f.lat + "," + f.lng));
                                that.startActivity(direct);
                                return true;
                            case R.id.iflPin:
                                int iOfFriends = friends.indexOf(Friend.findFriendById(f.id, friends));
                                boolean newB = !f.pinned;
                                Friend ff = friends.get(iOfFriends);
                                ff.setOn(newB);
                                friends.set(iOfFriends, ff);
                                fAdapter.notifyItemChanged(h.getLayoutPosition());// DOESN'T WORK... FIX IT!

                                if (fPinned == null) fPinned = new HashSet<>();
                                String sId = Long.toString(f.id);
                                if (newB) fPinned.add(sId);
                                else fPinned.remove(sId);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.remove(Home.exFPinned);
                                editor.commit();// NECESSARY
                                editor.putStringSet(Home.exFPinned, fPinned);
                                editor.commit();
                                Home.update();
                                return true;
                            case R.id.iflSuspend:
                                suspend(f);
                                return true;
                            case R.id.iflBreak:
                                alertDialogue(that, R.string.titleBreak, R.string.sureBreak,
                                        null, true,
                                        R.string.yes, R.string.no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                breakRel(f);
                                            }
                                        }, null);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                pm.inflate(R.menu.item_friend_long);
                pm.show();

                Menu menu = pm.getMenu();
                if (f.fromMe) menu.findItem(R.id.iflSuspend).setVisible(false);
                if (f.pinned) menu.findItem(R.id.iflPin).setTitle(R.string.iflUnpin);
                if (!f.status) menu.findItem(R.id.iflPin).setVisible(false);
                if (f.lat == 1000.0 || f.lng == 1000.0) {
                    if (view.resolveActivity(c.getPackageManager()) == null)
                        menu.findItem(R.id.iflViewInMaps).setVisible(false);
                    menu.findItem(R.id.iflShare).setVisible(false);
                    menu.findItem(R.id.iflDirections).setVisible(false);
                }
                return true;
            }
        };
        else onLongClick = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Friend f = data.get(h.getLayoutPosition());
                PopupMenu pm = new PopupMenu(c, v);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.iflAccept:
                                askAccept(f);
                                return true;
                            case R.id.iflCancel:
                                cancelReq(f);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                pm.inflate(R.menu.item_request_long);
                pm.show();
                if (f.fromMe) pm.getMenu().findItem(R.id.iflAccept).setVisible(false);
                return true;
            }
        };
        h.cl.setOnLongClickListener(onLongClick);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    void askAccept(final Friend f) {
        alertDialogue(that, R.string.accept, R.string.acceptMsg,
                null, true, R.string.yes, R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        accept(f);
                    }
                }, null);
    }

    public static void accept(final Friend f) {
        StringRequest srt = new StringRequest(Request.Method.POST, Fun.cloud + "?action=accept",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        switch (res) {
                            case "done":
                                Toast.makeText(c, R.string.done, Toast.LENGTH_LONG).show();
                                Home.navHandler.obtainMessage(1).sendToTarget();
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
                params.put("code", sp.getString(Fun.exCode, ""));
                params.put("numb", sp.getString(Fun.exNumb, ""));
                params.put("req", Long.toString(f.id));
                return params;
            }
        };
        srt.setTag("accept");
        srt.setRetryPolicy(new DefaultRetryPolicy(10000, 1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(c).add(srt);
    }

    public static void suspend(final Friend f) {
        StringRequest srt = new StringRequest(Request.Method.POST, Fun.cloud + "?action=suspend",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        switch (res) {
                            case "done":
                                Toast.makeText(c, R.string.done, Toast.LENGTH_LONG).show();
                                Home.navHandler.obtainMessage(1).sendToTarget();
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
                params.put("code", sp.getString(Fun.exCode, ""));
                params.put("numb", sp.getString(Fun.exNumb, ""));
                params.put("req", Long.toString(f.id));
                return params;
            }
        };
        srt.setTag("suspend");
        srt.setRetryPolicy(new DefaultRetryPolicy(10000, 1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(c).add(srt);
    }

    public static void breakRel(final Friend f) {
        StringRequest srt = new StringRequest(Request.Method.POST, Fun.cloud + "?action=break",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        switch (res) {
                            case "done":
                                Toast.makeText(c, R.string.done, Toast.LENGTH_LONG).show();
                                Home.navHandler.obtainMessage(1).sendToTarget();
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
                params.put("code", sp.getString(Fun.exCode, ""));
                params.put("numb", sp.getString(Fun.exNumb, ""));
                params.put("req", Long.toString(f.id));
                return params;
            }
        };
        srt.setTag("break");
        srt.setRetryPolicy(new DefaultRetryPolicy(10000, 1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(c).add(srt);
    }

    public static void cancelReq(final Friend f) {
        StringRequest srt = new StringRequest(Request.Method.POST, Fun.cloud + "?action=cancel_req",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        switch (res) {
                            case "done":
                                Toast.makeText(c, R.string.done, Toast.LENGTH_LONG).show();
                                Home.navHandler.obtainMessage(1).sendToTarget();
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
                params.put("code", sp.getString(Fun.exCode, ""));
                params.put("numb", sp.getString(Fun.exNumb, ""));
                params.put("req", Long.toString(f.id));
                return params;
            }
        };
        srt.setTag("cancel_req");
        srt.setRetryPolicy(new DefaultRetryPolicy(10000, 1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(c).add(srt);
    }
}