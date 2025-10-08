package com.example.boulder_backend.util;

import java.util.List;

import java.util.*;

public final class FontGradeCodec {
    private static final List<String> ORDER = List.of(
            "3","4","5A","5B","5C",
            "6A","6A+","6B","6B+","6C","6C+",
            "7A","7A+","7B","7B+","7C","7C+",
            "8A","8A+","8B","8B+","8C","8C+", "9A"
    );

    private static final Map<String,Integer> TO_NUM;
    static {
        Map<String,Integer> m = new HashMap<>();
        for (int i = 0; i < ORDER.size(); i++) m.put(ORDER.get(i).toUpperCase(Locale.ROOT), i);
        TO_NUM = Collections.unmodifiableMap(m);
    }

    private FontGradeCodec() {}

    /** Normalisiert Eingaben (Whitespace, Großschrift). Gibt null bei Unbekannt. */
    public static Integer toNumber(String fontGrade) {
        if (fontGrade == null) return null;
        String g = fontGrade.trim().toUpperCase(Locale.ROOT);
        return TO_NUM.get(g);
    }

    /** Rundet auf den nächsten diskreten Font-Grad. */
    public static String fromNumber(double value) {
        int idx = (int)Math.round(value);
        idx = Math.max(0, Math.min(idx, ORDER.size() - 1));
        return ORDER.get(idx);
    }
}
