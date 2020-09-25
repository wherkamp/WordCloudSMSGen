package me.kingtux.wcsmsg;

public enum Type {
    SENT,
    RECEIVED;

    public static Type fromID(String msg_box) {
        if (msg_box.equals("1")) {
            return RECEIVED;
        }
        if (msg_box.equals("2")) {
            return SENT;
        }
        return null;
    }
}
