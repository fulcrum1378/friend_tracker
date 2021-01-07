package org.ifaco.friendtracker.etc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.ifaco.friendtracker.Fun;
import org.ifaco.friendtracker.Home;
import org.ifaco.friendtracker.Nav;

import java.util.HashMap;
import java.util.Map;

import static org.ifaco.friendtracker.Fun.c;
import static org.ifaco.friendtracker.Fun.exCode;
import static org.ifaco.friendtracker.Fun.exNumb;
import static org.ifaco.friendtracker.Fun.sp;
import static org.ifaco.friendtracker.Nav.ftInterval;
import static org.ifaco.friendtracker.Nav.here;

public class Navigator extends BroadcastReceiver {
    @Override
    public void onReceive(final Context c, Intent intent) {
        Fun.sp = PreferenceManager.getDefaultSharedPreferences(c);
        new Nav(new Fun.Function() {
            @Override
            public void execute() {
                sync(here.getLatitude(), here.getLongitude());
            }
        }, new Fun.Function() {
            @Override
            public void execute() {
            }
        });
    }

    public static void sync(final double lat, final double lng) {
        StringRequest srt = new StringRequest(Request.Method.POST, Fun.cloud + "?action=sync",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        if (res.startsWith("done") && Home.syncHandler != null)
                            Home.syncHandler.obtainMessage(0, res).sendToTarget();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("code", sp.getString(exCode, ""));
                params.put("numb", sp.getString(exNumb, ""));
                params.put("time", Long.toString(Fun.now()));
                params.put("lat", Double.toString(lat));
                params.put("lng", Double.toString(lng));
                return params;
            }
        };
        srt.setTag("sync");
        srt.setRetryPolicy(new DefaultRetryPolicy((int) ftInterval, 1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(c).add(srt);
    }
}
