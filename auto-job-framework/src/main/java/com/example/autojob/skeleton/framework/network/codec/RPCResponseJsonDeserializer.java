package com.example.autojob.skeleton.framework.network.codec;

import com.example.autojob.skeleton.framework.network.protocol.RPCResponse;
import com.example.autojob.util.json.ClassCodec;
import com.example.autojob.util.json.JsonUtil;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * @Author Huang Yongxiang
 * @Date 2022/09/22 13:53
 */
public class RPCResponseJsonDeserializer implements JsonDeserializer<RPCResponse> {
    private static final ClassCodec CLASS_CODEC = new ClassCodec();

    @Override
    public RPCResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            RPCResponse response = new RPCResponse();
            JsonObject jsonObject = json.getAsJsonObject();
            response.setCode(jsonObject.has("code") ? jsonObject.get("code").getAsInt() : null);
            response.setMessage(jsonObject.has("message") ? jsonObject.get("message").getAsString() : null);
            response.setReturnType(jsonObject.has("returnType") ? CLASS_CODEC.deserialize(jsonObject.get("returnType"), Class.class, context) : null);
            if (response.getReturnType() == null) {
                return response;
            }
            JsonArray genericsType = jsonObject.has("genericsType") ? jsonObject.get("genericsType").getAsJsonArray() : null;
            if (genericsType != null && genericsType.size() > 0) {
                Class<?>[] genericsTypeArray = new Class[genericsType.size()];
                for (int i = 0; i < genericsType.size(); i++) {
                    genericsTypeArray[i] = CLASS_CODEC.deserialize(genericsType.get(i), Class.class, context);
                }
                response.setGenericsType(genericsTypeArray);
                if (response.getReturnType() != null && List.class.isAssignableFrom(response.getReturnType())) {
                    response.setResult(jsonObject.has("result") ? JsonUtil.jsonStringToList(jsonObject.get("result").toString(), genericsTypeArray[0]) : null);
                } else if (response.getReturnType() != null && Map.class.isAssignableFrom(response.getReturnType())) {
                    response.setResult(jsonObject.has("result") ? JsonUtil.jsonStringToMap(jsonObject.get("result").toString(), genericsTypeArray[0], genericsTypeArray[1]) : null);
                }
            } else {
                response.setResult(jsonObject.has("result") ? JsonUtil.jsonStringToPojo(jsonObject.get("result").toString(), response.getReturnType()) : null);
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new JsonParseException(e.getCause());
        }
    }

}
