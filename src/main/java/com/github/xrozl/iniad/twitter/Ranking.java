package com.github.xrozl.iniad.twitter;

import twitter4j.*;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class Ranking {

    public static Twitter twitter;
    public static void main(String[] args) throws Exception {
        twitter = TwitterFactory.getSingleton();

        List<String> black = List.of(
        );

        Properties prop = new Properties();
        prop.load(new FileInputStream("config.properties"));

        twitter.setOAuthConsumer(prop.getProperty("consumerKey"), prop.getProperty("consumerSecret"));
        twitter.setOAuthAccessToken(new twitter4j.auth.AccessToken(prop.getProperty("accessToken"), prop.getProperty("accessTokenSecret")));

        Map<String, Integer> map = new HashMap<>();
        UsersResponse lsp = ListsExKt.getListMembers(twitter, Long.parseLong(prop.getProperty("list")), null, 100, null, null, "public_metrics");
        for (User2 u : lsp.getUsers()) {
            if (u.getPublicMetrics().getTweetCount() < 1) continue;
            if (black.contains(u.getUsername())) {
                System.out.println(u.getUsername() + " is blacklisted");
                continue;
            }
            map.put(u.getName(), u.getPublicMetrics().getTweetCount());
        }
        String next = null;
        if (lsp.getMeta().getNextToken() != null) {
            next = lsp.getMeta().getNextToken();
        }
        while (true) {
            System.out.println("next: " + next);
            if (next != null) {
                lsp = ListsExKt.getListMembers(twitter, Long.parseLong(prop.getProperty("list")), null, 100, next, null, "public_metrics");
                for (User2 u : lsp.getUsers()) {
                    if (u.getPublicMetrics().getTweetCount() < 1) continue;
                    if (black.contains(u.getUsername())) {
                        System.out.println(u.getUsername() + " is blacklisted");
                        continue;
                    }
                    map.put(u.getName(), u.getPublicMetrics().getTweetCount());
                }
                if (lsp.getMeta().getNextToken() != null) {
                    next = lsp.getMeta().getNextToken();
                } else {
                    next = null;
                }
            } else {
                break;
            }
        }

        List<Map.Entry<String, Integer>> entity = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
        Collections.sort(entity, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2)
            {
                return obj2.getValue().compareTo(obj1.getValue());
            }
        });
        int i = 0;
        Map<String, Integer> map2 = new HashMap<>();
        StringBuffer buffer = new StringBuffer();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        buffer.append("ツイート数ランキング " + sdf.format(new Date()) + "\n");
        for(Map.Entry<String, Integer> entry : entity) {
            // i == 5
            if (i >= 5) break;
            //System.out.println(entry.getKey() + " : " + entry.getValue());
            buffer.append((i+1) + "位 " + entry.getKey() + " " + entry.getValue() + "ﾂｲｰﾄ\n");
            map2.put(entry.getKey(), entry.getValue());
            i++;
        }

        buffer.append("\n#iniad_6th_ranking");
        System.out.println(buffer.toString());

    }
}
