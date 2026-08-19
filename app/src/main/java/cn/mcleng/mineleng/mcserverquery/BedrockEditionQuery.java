package cn.mcleng.mineleng.mcserverquery;

import me.dilley.MineStat;

public class BedrockEditionQuery {

    public static ServerResult query(String host, int port) {
        DebugLog.log("Bedrock", "Querying " + host + ":" + port);

        try {
            MineStat ms = new MineStat(host, port, 5, MineStat.Request.BEDROCK, true);
            DebugLog.log("Bedrock", "Result: serverUp=" + ms.isServerUp()
                    + " status=" + ms.getConnectionStatus()
                    + " motd=" + ms.getMotd()
                    + " version=" + ms.getVersion()
                    + " latency=" + ms.getLatency());

            ServerResult result = new ServerResult();
            result.online = ms.isServerUp();
            if (!result.online) {
                DebugLog.log("Bedrock", "FAILED for " + host + ":" + port);
                return result;
            }

            result.motd = ms.getStrippedMotd();
            result.version = ms.getVersion();
            result.ping = (int) ms.getLatency();
            result.onlinePlayers = ms.getCurrentPlayers();
            result.maxPlayers = ms.getMaximumPlayers();

            DebugLog.log("Bedrock", "SUCCESS: " + result.motd);
            return result;
        } catch (Exception e) {
            DebugLog.error("Bedrock", "Exception", e);
            return ServerResult.offline();
        }
    }
}
