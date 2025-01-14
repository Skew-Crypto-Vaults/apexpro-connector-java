package exchange.apexpro.connector.impl;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import exchange.apexpro.connector.exception.ApexProApiException;
import exchange.apexpro.connector.impl.utils.JsonWrapper;

abstract class RestApiInvoker {

    private static final Logger log = LoggerFactory.getLogger(RestApiInvoker.class);
    private static final OkHttpClient client = new OkHttpClient();

    static void checkResponse(JsonWrapper json) {
        try {
            if (json.containKey("success")) {
                boolean success = json.getBoolean("success");
                if (!success) {
                    String err_code = json.getStringOrDefault("code", "");
                    String err_msg = json.getStringOrDefault("msg", "");
                    if ("".equals(err_code)) {
                        throw new ApexProApiException(ApexProApiException.EXEC_ERROR, "[Executing] " + err_msg);
                    } else {
                        throw new ApexProApiException(ApexProApiException.EXEC_ERROR,
                                "[Executing] " + err_code + ": " + err_msg);
                    }
                }
            } else if (json.containKey("code")) {

                int code = json.getInteger("code");
                if (code != 200) {
                    String message = json.getStringOrDefault("msg", "");
                    throw new ApexProApiException(ApexProApiException.EXEC_ERROR,
                            "[Executing] " + code + ": " + message);
                }
            }
        } catch (ApexProApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApexProApiException(ApexProApiException.RUNTIME_ERROR,
                    "[Invoking] Unexpected error: " + e.getMessage());
        }
    }

    static <T> T callSync(RestApiRequest<T> request) {
        try {
            String str;
            log.debug("Request URL " + request.request.url());
            Response response = client.newCall(request.request).execute();
            // System.out.println(response.body().string());
            if (response.isSuccessful() && response != null && response.body() != null) {
                str = response.body().string();
                response.close();
            } else {
                throw new ApexProApiException(ApexProApiException.ENV_ERROR,
                        "[Invoking] Cannot get the response from server");
            }
            log.debug("Request: "+request.request +"  Response =====> " + str);
            JsonWrapper jsonWrapper = JsonWrapper.parseFromString(str);
            checkResponse(jsonWrapper);
            return request.jsonParser!=null?request.jsonParser.parseJson(jsonWrapper):null;
        } catch (ApexProApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApexProApiException(ApexProApiException.ENV_ERROR,
                    "[Invoking] Unexpected error: " + e.getMessage());
        }
    }

    static WebSocket createWebSocket(Request request, WebSocketListener listener) {
        return client.newWebSocket(request, listener);
    }

}
