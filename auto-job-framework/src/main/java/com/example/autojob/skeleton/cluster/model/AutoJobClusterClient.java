package com.example.autojob.skeleton.cluster.model;

import com.example.autojob.logging.domain.AutoJobLog;
import com.example.autojob.logging.domain.AutoJobRunLog;
import com.example.autojob.skeleton.framework.config.ClusterConfig;
import com.example.autojob.skeleton.framework.task.AutoJobTask;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @Description 集群请求客户端
 * @Author Huang Yongxiang
 * @Date 2022/07/26 12:40
 */
@Slf4j
public class AutoJobClusterClient implements Closeable {
    private final ClusterNode clusterNode;

    private final ClusterConfig config;

    private long lastResponseTime;

    public AutoJobClusterClient(ClusterNode clusterNode, ClusterConfig config) {
        this.clusterNode = clusterNode;
        this.config = config;
    }


    /**
     * 判断节点是否还存活，如果对端节点离线或者对端节点禁止该节点访问时返回false
     *
     * @return boolean
     * @author Huang Yongxiang
     * @date 2022/7/26 14:29
     */
    public boolean isAlive() {
        //HttpHelper httpHelper = getHelloHttpHelper();
        //if (httpHelper != null) {
        //    try {
        //        httpHelper.sendWithBody();
        //        JsonObject callback = getCallback(httpHelper);
        //        lastResponseTime = httpHelper.getResponseTime();
        //        httpHelper.clear();
        //        boolean flag = isCallbackSuccess(callback);
        //        if (!flag) {
        //            log.error(getMessageEntry(callback));
        //        }
        //        return flag;
        //    } catch (Exception ignored) {
        //    }
        //}
        return false;
    }

    /**
     * 获取节点拥有的其他节点的信息
     *
     * @return java.util.List<com.example.autojob.skeleton.cluster.context.ClusterNode>
     * @author Huang Yongxiang
     * @date 2022/7/26 14:31
     */
    List<ClusterNode> getClusterNodes() {
        //HttpHelper httpHelper = getClusterGetHttpHelper(APIConstant.GET_CLUSTER_NODES);
        //if (httpHelper != null) {
        //    try {
        //        httpHelper.send();
        //        JsonObject callback = getCallback(httpHelper);
        //        lastResponseTime = httpHelper.getResponseTime();
        //        if (isCallbackSuccess(callback)) {
        //            List<ClusterNode> clusterNodes = new ArrayList<>();
        //            JsonArray data = callback.get("data").getAsJsonArray();
        //            for (int i = 0; i < data.size(); i++) {
        //                ClusterNode clusterNode = JsonUtil.jsonStringToPojo(data.get(i).getAsJsonObject().toString(), ClusterNode.class);
        //                if (clusterNode != null) {
        //                    clusterNodes.add(clusterNode);
        //                }
        //            }
        //            httpHelper.clear();
        //            return clusterNodes;
        //        }
        //    } catch (Exception e) {
        //        e.printStackTrace();
        //    }
        //}
        return Collections.emptyList();
    }

    public String transferTask(AutoJobTask task) {
        //if (task == null) {
        //    return null;
        //}
        //TaskVO taskVO = DTVConvert.task2VO(task);
        //try {
        //    Map<String, String> query = new HashMap<>();
        //    query.put(ClusterConstant.HOST_QUERY, ServletUtil.getLocalhostIp());
        //    query.put(ClusterConstant.PORT_QUERY, ServletUtil.getPort() + "");
        //    HttpHelper httpHelper = getClusterPostHttpHelper(JsonUtil.pojoToJsonString(taskVO), APIConstant.TRANSFER_TASK, query);
        //    if (httpHelper != null) {
        //        httpHelper.sendWithBody();
        //        lastResponseTime = httpHelper.getResponseTime();
        //        JsonObject callback = getCallback(httpHelper);
        //        if (isCallbackSuccess(callback)) {
        //            return callback.get("data").getAsString();
        //        }
        //    }
        //
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}
        return null;
    }

    public List<AutoJobLog> getLog(String key) {
        //Map<String, String> query = new HashMap<>();
        //try {
        //    query.put(ClusterConstant.LOG_KEY_QUERY, URLEncoder.encode(key, "utf-8"));
        //} catch (UnsupportedEncodingException e) {
        //    e.printStackTrace();
        //    return Collections.emptyList();
        //}
        //HttpHelper httpHelper = getClusterGetHttpHelpe(APIConstant.GET_LOG, query);
        //if (httpHelper != null) {
        //    try {
        //        httpHelper.send();
        //        lastResponseTime = httpHelper.getResponseTime();
        //        JsonObject callback = getCallback(httpHelper);
        //        if (isCallbackSuccess(callback)) {
        //            List<AutoJobLog> logList = new ArrayList<>();
        //            JsonArray array = callback.get("data").getAsJsonArray();
        //            for (int i = 0; i < array.size(); i++) {
        //                AutoJobLog autoJobLog = JsonUtil.jsonStringToPojo(array.get(i).getAsJsonObject().toString(), AutoJobLog.class);
        //                if (autoJobLog != null) {
        //                    logList.add(autoJobLog);
        //                }
        //            }
        //            return logList;
        //        }
        //
        //    } catch (Exception e) {
        //        e.printStackTrace();
        //    }
        //}
        return Collections.emptyList();
    }

    public List<AutoJobRunLog> getRunLog(String key) {
        //Map<String, String> query = new HashMap<>();
        //try {
        //    query.put(ClusterConstant.LOG_KEY_QUERY, URLEncoder.encode(key, "utf-8"));
        //} catch (UnsupportedEncodingException e) {
        //    e.printStackTrace();
        //    return Collections.emptyList();
        //}
        //HttpHelper httpHelper = getClusterGetHttpHelpe(APIConstant.GET_RUN_LOG, query);
        //if (httpHelper != null) {
        //    try {
        //        httpHelper.send();
        //        lastResponseTime = httpHelper.getResponseTime();
        //        JsonObject callback = getCallback(httpHelper);
        //        if (isCallbackSuccess(callback)) {
        //            List<AutoJobRunLog> logList = new ArrayList<>();
        //            JsonArray array = callback.get("data").getAsJsonArray();
        //            for (int i = 0; i < array.size(); i++) {
        //                AutoJobRunLog autoJobLog = JsonUtil.jsonStringToPojo(array.get(i).getAsJsonObject().toString(), AutoJobRunLog.class);
        //                if (autoJobLog != null) {
        //                    logList.add(autoJobLog);
        //                }
        //            }
        //            return logList;
        //        }
        //
        //    } catch (Exception e) {
        //        e.printStackTrace();
        //    }
        //}
        return Collections.emptyList();
    }

    public long getLastResponseTime() {
        return lastResponseTime;
    }

    //protected JsonObject getCallback(HttpHelper httpHelper) {
    //    String body = httpHelper.getResponseAsString();
    //    JsonObject callback = null;
    //    if (config.getEnableAuth()) {
    //        try {
    //            String encryptKey = httpHelper.getResponseHeader(ClusterConstant.HEADER_KEY);
    //            String decryptKey = AESUtil.build(config.getClusterPublicKey()).aesDecrypt(encryptKey);
    //            String decryptBody = AESUtil.build(decryptKey).aesDecrypt(body);
    //            callback = JsonUtil.stringToJsonObj(decryptBody);
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //    } else {
    //        callback = httpHelper.getResponseJsonObject();
    //    }
    //    return callback;
    //}


    //protected HttpHelper getHelloHttpHelper() {
    //    BaseClusterDTO me = new BaseClusterDTO();
    //    try {
    //        me.setHost(ServletUtil.getLocalhostIp());
    //        me.setPort(WebServerPortConfig.getInstance().getProPort());
    //        me.setServerTime(SystemClock.now());
    //        me.setToken(config.getClusterToken());
    //        return getClusterPostHttpHelper(JsonUtil.pojoToJsonString(me), APIConstant.HELLO);
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }
    //    return null;
    //}

    protected boolean isCallbackSuccess(JsonObject o) {
        return o != null && o.has("code") && o.get("code").getAsInt() == 200;
    }

    protected String getMessageEntry(JsonObject o) {
        return o != null && o.has("message") ? o.get("message").getAsString() : null;
    }

    //protected HttpHelper getClusterPostHttpHelper(String body, String api) {
    //    return getClusterPostHttpHelper(body, api, null);
    //}
    //
    //public HttpHelper getClusterPostHttpHelper(String body, String api, Map<String, String> query) {
    //    String key = StringUtils.getRandomStr(16);
    //    try {
    //        String encryptKey = AESUtil.build(config.getClusterPublicKey()).aesEncrypt(key);
    //        HttpHeaders httpHeaders = new HttpHeaders();
    //        httpHeaders.add("Content-Type", "application/json; charset=utf-8");
    //        httpHeaders.add(ClusterConstant.HEADER_KEY, encryptKey);
    //        return HttpHelper.builder().setBody(config.getEnableAuth() ? AESUtil.build(key).aesEncrypt(body) : body).setRequestHeaders(httpHeaders).setUrl(getURL(api, key, query)).setCreateLinkTimeOut((int) (config.getClusterCreateConnectTimeout() * 1000)).setGetDataTimeOut((int) (config.getClusterGetDataTimeout() * 1000)).setWays("post").build();
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }
    //    return null;
    //}

    //protected HttpHelper getClusterGetHttpHelper(String api) {
    //    return getClusterGetHttpHelper(api, null);
    //}
    //
    //public HttpHelper getClusterGetHttpHelper(String api, Map<String, String> query) {
    //    String key = StringUtils.getRandomStr(16);
    //    String encryptKey = null;
    //    try {
    //        encryptKey = AESUtil.build(config.getClusterPublicKey()).aesEncrypt(key);
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }
    //    HttpHeaders headers = new HttpHeaders();
    //    headers.add("Content-Type", "application/json; charset=utf-8");
    //    headers.add(ClusterConstant.HEADER_KEY, encryptKey);
    //    return HttpHelper.builder().setUrl(getURL(api, key, query)).setRequestHeaders(headers).setCreateLinkTimeOut((int) (config.getClusterCreateConnectTimeout() * 1000)).setGetDataTimeOut((int) (config.getClusterGetDataTimeout() * 1000)).setWays("get").build();
    //}

    //protected String getURL(String api) {
    //    String host = clusterNode.getHost();
    //    if ("localhost".equals(clusterNode.getHost())) {
    //        host = ServletUtil.getLocalhostIp();
    //    }
    //    String prefix = config.getClusterNodePrefix();
    //
    //    if (!StringUtils.isEmpty(prefix)) {
    //        prefix = prefix.replace("'", "");
    //        prefix = prefix.replace("\"", "");
    //        if (prefix.lastIndexOf("/") == -1) {
    //            prefix = prefix + "/";
    //        }
    //        return String.format("http://%s:%s/%s%s", host, clusterNode.getPort(), prefix, api);
    //    }
    //    return String.format("http://%s:%s/%s", host, clusterNode.getPort(), api);
    //}
    //
    //protected String getURL(String api, String key) {
    //    String host = clusterNode.getHost();
    //    if ("localhost".equals(clusterNode.getHost())) {
    //        host = ServletUtil.getLocalhostIp();
    //    }
    //    String prefix = config.getClusterNodePrefix();
    //    String encryptToken = null;
    //    try {
    //        encryptToken = URLEncoder.encode(AESUtil.build(key).aesEncrypt(config.getClusterToken()), "utf-8");
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }
    //    if (!StringUtils.isEmpty(prefix)) {
    //        prefix = prefix.replace("'", "");
    //        prefix = prefix.replace("\"", "");
    //        if (prefix.lastIndexOf("/") == -1) {
    //            prefix = prefix + "/";
    //        }
    //        return String.format("http://%s:%s/%s%s?%s=%s", host, clusterNode.getPort(), prefix, api, ClusterConstant.TOKEN_QUERY, encryptToken);
    //    }
    //    return String.format("http://%s:%s/%s?%s=%s", host, clusterNode.getPort(), api, ClusterConstant.TOKEN_QUERY, encryptToken);
    //}

    //protected String getURL(String api, String key, Map<String, String> query) {
    //    StringBuilder url = new StringBuilder(getURL(api, key));
    //    if (CollectionUtils.isEmpty(query)) {
    //        return url.toString();
    //    }
    //    for (Map.Entry<String, String> entry : query.entrySet()) {
    //        url.append(String.format("&%s=%s", entry.getKey(), entry.getValue()));
    //    }
    //    return url.toString();
    //}


    @Override
    public void close() throws IOException {
        ClusterNode clusterNode = ClusterNode.getLocalHostNode();
        //HttpHelper httpHelper = getClusterPostHttpHelper(JsonUtil.pojoToJsonString(clusterNode), APIConstant.OFF_LINE);
        //if (httpHelper != null) {
        //    try {
        //        httpHelper.sendWithBody();
        //        lastResponseTime = httpHelper.getResponseTime();
        //        JsonObject callback = getCallback(httpHelper);
        //        if (isCallbackSuccess(callback)) {
        //            log.info("节点离线请求成功");
        //        } else {
        //            log.info("节点离线请求失败");
        //        }
        //    } catch (Exception e) {
        //        throw new IOException(e.getMessage());
        //    }
        //}
    }

    private static class APIConstant {
        static final String HELLO = "cluster/hello";
        static final String GET_CLUSTER_NODES = "cluster/get_cluster_nodes";
        static final String TRANSFER_TASK = "cluster/receive_task";
        static final String GET_LOG = "cluster/get_log";
        static final String GET_RUN_LOG = "cluster/get_run_log";
        static final String OFF_LINE = "cluster/off_line";
    }
}
