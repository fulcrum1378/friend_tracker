package org.ifaco.friendtracker;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import static org.ifaco.friendtracker.Fun.c;
import static org.ifaco.friendtracker.Home.fPinned;
import static org.ifaco.friendtracker.Home.friendsOff;

class Friend implements Parcelable {
    long id, lastSync;
    String name;
    double lat, lng;
    boolean status, fromMe, isOn, pinned;

    public Friend(long id, String name, long lastSync, double lat, double lng, boolean status,
                  boolean fromMe, boolean isOn, boolean pinned) {
        this.id = id;
        this.name = name;
        this.lastSync = lastSync;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
        this.fromMe = fromMe;
        this.isOn = isOn;
        this.pinned = pinned;
    }

    public Friend(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.lastSync = in.readLong();
        this.lat = in.readDouble();
        this.lng = in.readDouble();
        this.status = in.readByte() == (byte) 1;
        this.fromMe = in.readByte() == (byte) 1;
        this.isOn = in.readByte() == (byte) 1;
        this.pinned = in.readByte() == (byte) 1;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(name);
        out.writeLong(lastSync);
        out.writeDouble(lat);
        out.writeDouble(lng);
        out.writeByte((byte) (status ? 1 : 0));
        out.writeByte((byte) (fromMe ? 1 : 0));
        out.writeByte((byte) (isOn ? 1 : 0));
        out.writeByte((byte) (pinned ? 1 : 0));
    }

    public static final Parcelable.Creator<Friend> CREATOR = new Parcelable.Creator<Friend>() {
        public Friend createFromParcel(Parcel in) {
            return new Friend(in);
        }

        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public boolean notEqual(Friend other) {
        boolean yes = false;
        if (id != other.id ||
                !name.equals(other.name) ||
                lastSync != other.lastSync ||
                lat != other.lat ||
                lng != other.lng ||
                status != other.status ||
                fromMe != other.fromMe
        ) yes = true;
        return yes;
    }


    public static ArrayList<Friend> parse(JsonReader r) throws IOException {
        ArrayList<Friend> f = new ArrayList<>();
        r.beginObject();
        while (r.hasNext()) if (r.nextName().equals("friends")) {
            if (r.peek() == JsonToken.BEGIN_ARRAY) {
                r.beginArray();
                r.endArray();
                break;
            }
            r.beginObject();
            while (r.hasNext()) {
                long id = Long.parseLong(r.nextName()), lastSync = -1;
                String name = null;
                double lat = 1000.0, lng = 1000.0;
                boolean status = false, fromMe = false, isOn = true, pinned = false;
                r.beginObject();
                while (r.hasNext()) switch (r.nextName()) {
                    case "n":
                        name = r.nextString();
                        break;
                    case "t":
                        if (r.peek() != JsonToken.NULL) lastSync = Long.parseLong(r.nextString());
                        else r.skipValue();
                        break;
                    case "y":
                        if (r.peek() != JsonToken.NULL) lat = Double.parseDouble(r.nextString());
                        else r.skipValue();
                        break;
                    case "x":
                        if (r.peek() != JsonToken.NULL) lng = Double.parseDouble(r.nextString());
                        else r.skipValue();
                        break;
                    case "f":
                        status = Byte.parseByte(r.nextString()) == (byte) 1;
                        break;
                    case "m":
                        fromMe = Byte.parseByte(r.nextString()) == (byte) 1;
                        break;
                    default:
                        r.skipValue();
                        break;
                }
                r.endObject();
                if (lat == 1000.0 || lng == 1000.0) isOn = false;
                if (friendsOff != null) for (String o : friendsOff)
                    if (o.equals(Long.toString(id))) isOn = false;
                if (fPinned != null && status) for (String o : fPinned)
                    if (o.equals(Long.toString(id))) pinned = true;
                if (name == null) name = c.getString(R.string.unknownInvited, id);
                f.add(new Friend(id, name, lastSync, lat, lng, status, fromMe, isOn, pinned));
            }
            r.endObject();
        }
        r.endObject();
        return f;
    }

    void setOn(boolean b) {
        this.isOn = b;
    }

    public static Friend findFriendById(long id, ArrayList<Friend> list) {
        if (list == null) return null;
        Friend f = null;
        for (Friend ff : list) if (ff.id == id) f = ff;
        return f;
    }


    static class SortFriends implements Comparator<Friend> {
        int by;

        SortFriends(int by) {
            this.by = by;
        }

        @Override
        public int compare(Friend a, Friend b) {
            switch (by) {
                case 1:
                    return a.name.compareTo(b.name);
                case 2:
                    return (int) (a.lastSync - b.lastSync);
                case 3:
                    return (b.status ? 1 : 0) - (a.status ? 1 : 0);
                case 4:
                    return (b.fromMe ? 1 : 0) - (a.fromMe ? 1 : 0);
                case 5:
                    return judge(a) - judge(b);
                default:
                    return (int) (a.id - b.id);
            }
        }

        private int judge(Friend f) {
            if (f.status) {
                if (f.pinned) return 0;
                else return 1;
            } else if (!f.fromMe) return 2;
            else return 3;
        }
    }
}
