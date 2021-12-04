package Utility;

import java.io.Serializable;

/**
 * define a Response class to define the format of the responses returned by server
 */
public class Response implements Serializable {
    String responseCode;
    String operation;
    String message;
    String key;
    String value;

    public Response() { }

    public Response(String responseCode, String operation, String message) {
        this.responseCode = responseCode;
        this.operation = operation;
        this.message = message;
    }

    public Response(String responseCode, String operation, String message, String key) {
        this.responseCode = responseCode;
        this.operation = operation;
        this.message = message;
        this.key = key;
    }

    public Response(String responseCode, String operation, String message, String key, String value) {
        this.responseCode = responseCode;
        this.operation = operation;
        this.message = message;
        this.key = key;
        this.value = value;
    }

    /**
     * serialize response to string to return to the client
     * @return a String (the serialized response)
     */
    public String serialize() {
        if (this.value != null && this.key != null) {
            return "responseCode=>" + this.responseCode + " " + this.operation + " " + "key:" + this.key + " value:" + this.value + " " + this.message;
        } else if (this.key != null) {
            return "responseCode=>" + this.responseCode + " " + this.operation + " " + "key:" + this.key + " " + this.message;
        } else {
            return "responseCode=>" + this.responseCode + " " + this.operation + " " + this.message;
        }
    }
}

