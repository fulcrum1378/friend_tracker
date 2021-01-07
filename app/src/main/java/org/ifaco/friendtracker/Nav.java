package org.ifaco.friendtracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import static org.ifaco.friendtracker.Fun.c;
import static org.ifaco.friendtracker.Fun.sp;

public class Nav {
    public static long defFT = 60000L, defBT = -1;
    public static long ftInterval = defFT, btInterval = defBT;
    private static LocationRequest locreq = null;
    public FusedLocationProviderClient flpc;
    public static Location here = null, reqHere = null;
    public static final String exFTNumber = "ftNumber", exBTNumber = "btNumber";

    public Nav(final Fun.Function onSuccess, final Fun.Function onFailed) {
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            onFailed.execute();
            return;
        }
        flpc = LocationServices.getFusedLocationProviderClient(c);
        if (locreq != null) flpc.requestLocationUpdates(locreq, locCallback, Looper.getMainLooper());
        flpc.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                here = location;// NULLABLE (especially in Android Oreo 8)
                if (here == null) here = reqHere;
                if (here == null) {
                    onFailed.execute();
                    return;
                }
                onSuccess.execute();
                if (Home.navHandler != null) Home.navHandler.obtainMessage(0).sendToTarget();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onFailed.execute();
            }
        });
    }

    public static LocationCallback locCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) return;
            for (Location location : locationResult.getLocations()) {
                if (reqHere != null) if (location.getTime() <= reqHere.getTime()) continue;
                reqHere = location;
            }
        }
    };


    @SuppressWarnings("MissingPermission")
    public static void requestLocation(final AppCompatActivity that, final Handler handler) {
        ftInterval = sp.getLong(exFTNumber, defFT);
        locreq = LocationRequest.create();
        locreq.setInterval(ftInterval);
        locreq.setFastestInterval(ftInterval / 6);
        locreq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder lrBuilder = new LocationSettingsRequest.Builder().addLocationRequest(locreq);
        Task<LocationSettingsResponse> lrTask = LocationServices.getSettingsClient(that).checkLocationSettings(lrBuilder.build());
        lrTask.addOnSuccessListener(that, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (handler != null) handler.obtainMessage(1).sendToTarget();
            }
        });
        lrTask.addOnFailureListener(that, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (handler != null) handler.obtainMessage(0, e).sendToTarget();
            }
        });
    }
}
