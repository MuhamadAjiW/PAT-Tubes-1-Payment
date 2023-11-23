package payment.util.classes;

import lombok.Data;
import org.json.JSONObject;

@Data
public class Response {
    private String message;
    private boolean valid;
    private Object data;

    public Response(String message, boolean valid, Object data){
        this.message = message;
        this.valid = valid;
        this.data = data;
    }

    public String toJsonString(){
        JSONObject json = new JSONObject();
        json.put("message", message);
        json.put("valid", valid);
        json.put("data", data);
        return json.toString();
    }
}
