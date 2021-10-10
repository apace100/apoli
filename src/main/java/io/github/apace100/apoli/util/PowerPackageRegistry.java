package io.github.apace100.apoli.util;

import com.google.common.collect.ImmutableList;

import java.util.LinkedList;
import java.util.List;

public class PowerPackageRegistry {

    private static List<String> PACKAGES = new LinkedList<>();

    public static void register(String pkg) {
        PACKAGES.add(pkg);
    }

    public static ImmutableList<String> getPackages() {
        return ImmutableList.copyOf(PACKAGES);
    }

    public static String transformJsonToClass(String jsonName) {
        StringBuilder builder = new StringBuilder();
        boolean caps = true;
        int capsOffset = 'A' - 'a';
        for(char c : jsonName.toCharArray()) {
            if(c == '_') {
                caps = true;
                continue;
            }
            if(caps) {
                builder.append(Character.toUpperCase(c));
                caps = false;
            } else {
                builder.append(c);
            }
        }
        builder.append("Power");
        return builder.toString();
    }
}
