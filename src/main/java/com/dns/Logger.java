package com.dns;

import java.util.List;

public class Logger {
    public static void Info(String info) {
        System.out.println(info);
    }

    public static void outputFlags(List<Integer> flags) {
        if (flags.get(0) == 0) {
            Logger.Info("Type: Query");
        } else {
            Logger.Info("Type: Response");
        }

        if (flags.get(1) == 0) {
            Logger.Info("Option: Standard");
        } else {
            Logger.Info("Option: Other");
        }

        if (flags.get(2) == 1) {
            Logger.Info("Authoritative Answer");
        }

        if (flags.get(3) == 1) {
            Logger.Info("This message was truncated");
        }

        if (flags.get(4) == 1) {
            Logger.Info("Recursion: Desired");
        }
    }

    public static void outputQuestion(List<String> question) {
        Logger.Info("Resolving: " + question.get(0));
        Logger.Info("Record Type: " + question.get(1));
        Logger.Info("Class: " + question.get(2));
    }
}
