package payment.enums;

public enum HttpRequestMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH");


    private final String method;

    HttpRequestMethod(String status){
        this.method = status;
    }

    public String getString(){
        return method;
    }
}
