package com.example.autojob.skeleton.framework.network.codec;

import com.example.autojob.skeleton.framework.network.protocol.RPCRequest;
import com.example.autojob.skeleton.framework.network.protocol.RPCRequestParamsType;
import com.example.autojob.util.json.ClassCodec;
import com.example.autojob.util.json.JsonUtil;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * @Author Huang Yongxiang
 * @Date 2022/09/22 12:54
 */
public class RPCRequestJsonDeserializer implements JsonDeserializer<RPCRequest> {
    @Override
    public RPCRequest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            RPCRequest request = new RPCRequest();
            JsonObject jsonObject = json.getAsJsonObject();
            request.setServiceName(jsonObject.has("serviceName") ? jsonObject
                    .get("serviceName")
                    .getAsString() : null);
            request.setMethodName(jsonObject.has("methodName") ? jsonObject
                    .get("methodName")
                    .getAsString() : null);
            JsonArray paramsType = jsonObject.has("paramTypes") ? jsonObject
                    .get("paramTypes")
                    .getAsJsonArray() : null;
            JsonArray params = jsonObject.has("params") ? jsonObject
                    .get("params")
                    .getAsJsonArray() : null;
            JsonArray paramTypes = jsonObject.has("paramsTypes") ? jsonObject
                    .get("paramsTypes")
                    .getAsJsonArray() : null;
            if (paramTypes != null) {
                RPCRequestParamsType[] requestParamsTypes = new RPCRequestParamsType[paramTypes.size()];
                ClassCodec classCodec = new ClassCodec();
                for (int i = 0; i < paramTypes.size(); i++) {
                    JsonObject param = paramTypes
                            .get(i)
                            .getAsJsonObject();
                    Class<?> pType = classCodec.deserialize(param.get("type"), Class.class, context);
                    Class<?> gType = param.has("genericsType") ? classCodec.deserialize(param.get("genericsType"), Class.class, context) : null;
                    RPCRequestParamsType type = new RPCRequestParamsType(pType, gType);
                    requestParamsTypes[i] = type;
                }
                request.setParamsTypes(requestParamsTypes);
            }
            if (paramsType != null) {
                Class<?>[] paramsTypeArray = new Class[paramsType.size()];
                ClassCodec classCodec = new ClassCodec();
                for (int i = 0; i < paramsType.size(); i++) {
                    paramsTypeArray[i] = classCodec.deserialize(paramsType.get(i), Class.class, context);
                }
                request.setParamTypes(paramsTypeArray);
            }
            if (params != null) {
                Object[] paramsArray = new Object[params.size()];
                RPCRequestParamsType[] requestParamsTypes = request.getParamsTypes();
                for (int i = 0; i < params.size(); i++) {
                    if (params.get(i) == null || "null".equals(params
                            .get(i)
                            .toString())) {
                        paramsArray[i] = null;
                    } else {
                        if (requestParamsTypes[i].isList()) {
                            if (requestParamsTypes[i].getGenericsType() == null) {
                                paramsArray[i] = null;
                            } else {
                                paramsArray[i] = JsonUtil.jsonStringToList(params
                                        .get(i)
                                        .toString(), requestParamsTypes[i].getListGenericsType());
                            }
                        } else if (requestParamsTypes[i].isMap()) {
                            if (requestParamsTypes[i].getGenericsType() == null) {
                                paramsArray[i] = null;
                            } else {
                                paramsArray[i] = JsonUtil.jsonStringToMap(params
                                        .get(i)
                                        .toString(), requestParamsTypes[i].getMapKeyType(), requestParamsTypes[i].getMapValueType());
                            }
                        } else {
                            paramsArray[i] = JsonUtil.jsonStringToPojo(params
                                    .get(i)
                                    .toString(), requestParamsTypes[i].getType());
                        }
                    }
                }
                request.setParams(paramsArray);
            }
            return request;
        } catch (Exception e) {
            e.printStackTrace();
            throw new JsonParseException(e.getCause());
        }
    }


}
