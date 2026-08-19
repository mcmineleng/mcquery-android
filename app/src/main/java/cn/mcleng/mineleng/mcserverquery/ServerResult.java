package cn.mcleng.mineleng.mcserverquery;

import android.graphics.Bitmap;

public class ServerResult {
    public boolean online;
    public Bitmap icon;
    public String motd;
    public String version;
    public int ping;
    public int onlinePlayers;
    public int maxPlayers;

    public static ServerResult offline() {
        ServerResult r = new ServerResult();
        r.online = false;
        return r;
    }
}
