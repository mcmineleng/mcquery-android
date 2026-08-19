package cn.mcleng.mineleng.mcserverquery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import me.dilley.MineStat;

public class JavaEditionQuery {

    public static ServerResult query(String input) {
        String host;
        int port = 25565;
        boolean portExplicit = false;

        if (input.contains(":")) {
            String[] parts = input.split(":", 2);
            host = parts[0];
            try {
                port = Integer.parseInt(parts[1]);
                portExplicit = true;
            } catch (NumberFormatException e) {
                port = 25565;
            }
        } else {
            host = input;
        }

        if (!portExplicit) {
            String[] srv = resolveSrv(host);
            if (srv != null) {
                host = srv[0];
                port = Integer.parseInt(srv[1]);
                DebugLog.log("Java", "SRV resolved " + input + " -> " + host + ":" + port);
            }
        }

        DebugLog.log("Java", "Querying " + host + ":" + port);

        try {
            MineStat ms = new MineStat(host, port, 5, MineStat.Request.JSON, true);
            DebugLog.log("Java", "JSON: serverUp=" + ms.isServerUp()
                    + " status=" + ms.getConnectionStatus()
                    + " motd=" + ms.getMotd()
                    + " version=" + ms.getVersion()
                    + " latency=" + ms.getLatency());

            if (!ms.isServerUp()) {
                DebugLog.log("Java", "Trying legacy...");
                ms = new MineStat(host, port, 5, MineStat.Request.LEGACY, true);
                DebugLog.log("Java", "Legacy: serverUp=" + ms.isServerUp()
                        + " status=" + ms.getConnectionStatus());
            }

            if (!ms.isServerUp()) {
                DebugLog.log("Java", "Trying extended...");
                ms = new MineStat(host, port, 5, MineStat.Request.EXTENDED, true);
                DebugLog.log("Java", "Extended: serverUp=" + ms.isServerUp()
                        + " status=" + ms.getConnectionStatus());
            }

            if (!ms.isServerUp()) {
                DebugLog.log("Java", "Trying bedrock fallback...");
                ms = new MineStat(host, port, 5, MineStat.Request.BEDROCK, true);
                DebugLog.log("Java", "Bedrock fallback: serverUp=" + ms.isServerUp()
                        + " status=" + ms.getConnectionStatus());
            }

            ServerResult result = new ServerResult();
            result.online = ms.isServerUp();
            if (!result.online) {
                DebugLog.log("Java", "ALL PROTOCOLS FAILED for " + host + ":" + port);
                return result;
            }

            result.motd = ms.getStrippedMotd();
            result.version = ms.getVersion();
            result.ping = (int) ms.getLatency();
            result.onlinePlayers = ms.getCurrentPlayers();
            result.maxPlayers = ms.getMaximumPlayers();

            String faviconB64 = ms.getFaviconB64();
            if (faviconB64 != null && !faviconB64.isEmpty()) {
                try {
                    byte[] iconBytes = Base64.decode(faviconB64, Base64.DEFAULT);
                    result.icon = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.length);
                } catch (Exception e) {
                    DebugLog.error("Java", "favicon decode failed", e);
                }
            }

            DebugLog.log("Java", "SUCCESS: " + result.motd);
            return result;
        } catch (Exception e) {
            DebugLog.error("Java", "Exception", e);
            return ServerResult.offline();
        }
    }

    private static final String[] DNS_SERVERS = {
            "223.5.5.5", "223.6.6.6",
            "119.29.29.29",
            "180.76.76.76",
            "1.1.1.1", "1.0.0.1",
            "8.8.8.8", "8.8.4.4"
    };

    private static String[] resolveSrv(String host) {
        String srvDomain = "_minecraft._tcp." + host;
        for (String dns : DNS_SERVERS) {
            try {
                SimpleResolver resolver = new SimpleResolver(dns);
                resolver.setTimeout(java.time.Duration.ofSeconds(3));
                Lookup lookup = new Lookup(srvDomain, Type.SRV);
                lookup.setResolver(resolver);
                Record[] records = lookup.run();
                if (records != null && records.length > 0) {
                    for (Record record : records) {
                        if (record instanceof SRVRecord) {
                            SRVRecord srv = (SRVRecord) record;
                            String target = srv.getTarget().toString();
                            if (target.endsWith(".")) {
                                target = target.substring(0, target.length() - 1);
                            }
                            int port = srv.getPort();
                            return new String[]{target, String.valueOf(port)};
                        }
                    }
                }
                if (lookup.getResult() == Lookup.TYPE_NOT_FOUND) {
                    return null;
                }
            } catch (Exception e) {
                DebugLog.log("Java", "SRV via " + dns + " failed: " + e.getMessage());
            }
        }
        return null;
    }
}
