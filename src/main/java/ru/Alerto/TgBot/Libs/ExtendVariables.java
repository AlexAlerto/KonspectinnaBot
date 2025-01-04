package ru.Alerto.TgBot.Libs;

public class ExtendVariables {
    public static boolean StrIsLong(String str){
        return str != null && str.matches("-?\\d+");
    }
}
