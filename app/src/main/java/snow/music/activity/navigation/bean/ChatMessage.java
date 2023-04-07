package snow.music.activity.navigation.bean;// Created byjinengmao

// on 2023/4/7
// Description：
public class ChatMessage {
    public static final int TYPE_SEND = 1;
    public static final int TYPE_RECEIVED = 0;


    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    private String message;
    private int type;

    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
    }
}
