package snow.music.util.http;// Created byjinengmao

// on 2023/4/6
// Description：
public interface HttpApi {
    String HOST = "http://123.56.216.144";
    String HTTP_HEAD = HOST + "/api/";

    String userRequest = HTTP_HEAD + "user-request";
}
