

#### 1、Http请求的几种实现方式

##### 1.1 使用Hutool工具类的实现的

###### POST请求的实现示例

```java
    private String httpPostMethod(String url, Map<String, Object> params, Map<String, String> headers, boolean isJson) {

        SysContentResult sysContentResult;
        String requestInfoForLog = String.format("对于url: %s, 参数: %s, header头: %s", url,params,headers);
        try {
            HttpRequest post = HttpUtil.createPost(url);
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    post = post.header(entry.getKey(), entry.getValue());
                }
            }
            HttpRequest request;
            if (isJson) {
                request = post.body(JSON.toJSONString(params));
            } else {
                request = post.form(params);
            }
            HttpResponse httpResponse = request.execute();
            String json = httpResponse.body();
            if (!httpResponse.isOk()) {
                log.error("#基础信息# 状态码：【{}】 返回结果出错 结果：【{}】", httpResponse.getStatus(), json);
                throw new ServiceException("request.is.fail");
            }
            log.info("#基础信息#  获取结果为 {}", json);
            if (StringUtils.isBlank(json)){
                log.warn("#基础信息# {} 获取结果为空",requestInfoForLog);
                throw new ServiceException("get.sys.content.null");
            }
            reture json;
        } catch (Exception e) {
            log.error("#基础信息# {} 请求异常，异常为 {}",requestInfoForLog, Throwables.getStackTraceAsString(e));
            throw new ServiceException("request.is.fail");
        }
    }
```

###### GET请求的实现示例

```java
    private String httpGetMethod(String url, Map<String, String> params, Map<String, String> headers) {
        // url处理
        String requestInfoForLog = String.format("对于url: %s, 参数: %s, header头: %s", url, params, nheaders);
        if (StringUtils.isEmpty(url)) {
            log.warn("{} url为空", requestInfoForLog);
            throw new ServiceException("url.is.null");
        }
        url = resovleGetUrl(url, params);
        // header头处理
        HttpRequest get = HttpUtil.createGet(url);
        if (CollUtil.isNotEmpty(headers)) {
            get = get.addHeaders(headers);
        }
        // 发送get请求
        HttpResponse response = get.execute();
        if (!response.isOk()){
            log.error("{} 返回内容错误， 返回码为{} 内容为{}", requestInfoForLog, response.getStatus(), response.body());
            throw new ServiceException("get.sys.content.interface.fail");
        }
        String resultString = response.body();
        if (StringUtils.isBlank(resultString)){
            log.warn("{} 返回内容为空", requestInfoForLog);
            throw new ServiceException("get.sys.content.interface.fail");
        }
       return resultString;
    }

	// 封装参数
    private String resovleGetUrl(String url, Map<String, String> params) {
        if (params == null) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        sb.append("?");
        params.forEach((key, param) -> {
            sb.append(key).append('=').append(param).append('&');
        });
        url = sb.toString();
        url = url.substring(0, url.length() - 1);
        return url;
    }
```

##### 1.2 使用apache的httpcomponents实现

###### POST请求实现示例

```java
public String httpPostRequestBody(String url, Map<String, String> headers,Map<String, String> params,Boolean isJson) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);

    	// 设置请求头
    	if (CollectionUtil.isNotEmpty(headers)){
            for (Map.Entry<String, String> param : headers.entrySet()) {
            	httpPost.addHeader(param.getKey(), param.getValue());
        	}
        }
    	if(isJson){
            String json = JSON.toJSONString(params);
            StringEntity entity = new StringEntity(json, "utf-8");
            httpPost.setEntity(entity);
        }else{
            ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        	httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        }
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // long len = entity.getContentLength();// -1 表示长度未知
                String result = EntityUtils.toString(entity);
                response.close();
                return result;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
        return EMPTY_STR;
    }

    private ArrayList<NameValuePair> covertParams2NVPS(
            Map<String, String> params) {
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        return pairs;
    }
	private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager cm;
    private static String EMPTY_STR = "";
    private static String UTF_8 = "UTF-8";

    private void init() {
        if (cm == null) {
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", trustAllHttpsCertificates()).build();
            cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            cm.setMaxTotal(50);// 整个连接池最大连接数
            cm.setDefaultMaxPerRoute(5);// 每路由最大连接数，默认值是2
        }
    }

    /**
     * 通过连接池获取HttpClient
     *
     * @return
     */
    public CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            init();
            httpClient = HttpClients.custom().setConnectionManager(cm).build();

        }
        return httpClient;
    }

    //绕过ssl验证
    private static SSLConnectionSocketFactory trustAllHttpsCertificates() {
        SSLConnectionSocketFactory socketFactory = null;
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");//sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, null);
            socketFactory = new SSLConnectionSocketFactory(sc, NoopHostnameVerifier.INSTANCE);
            //HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return socketFactory;
    }

    static class miTM implements TrustManager, X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            //don't check
        }
        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            //don't check
        }
    }
```

###### GET请求实现示例

```java
	private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager cm;
    private static String EMPTY_STR = "";
    private static String UTF_8 = "UTF-8";    

	public String httpGetRequest(String url, Map<String, String> headers,
                                 Map<String, String> params) throws URISyntaxException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);
        HttpGet httpGet = new HttpGet(ub.build());
        if (CollectionUtil.isNotEmpty(params)){
            ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
            ub.setParameters(pairs);
        }
        if (CollectionUtil.isNotEmpty(headers)){
            for (Map.Entry<String, String> param : headers.entrySet()) {
                httpGet.addHeader(param.getKey(), param.getValue());
            }
        }

        return getResult(httpGet);
    }

    private ArrayList<NameValuePair> covertParams2NVPS(Map<String, String> params) {
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        return pairs;
    }

    //绕过ssl验证
    private static SSLConnectionSocketFactory trustAllHttpsCertificates() {
        SSLConnectionSocketFactory socketFactory = null;
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");//sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, null);
            socketFactory = new SSLConnectionSocketFactory(sc, NoopHostnameVerifier.INSTANCE);
            //HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return socketFactory;
    }

    static class miTM implements TrustManager, X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            //don't check
        }
        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            //don't check
        }
    }

	private void init() {
        if (cm == null) {
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", trustAllHttpsCertificates()).build();
            cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            cm.setMaxTotal(50);// 整个连接池最大连接数
            cm.setDefaultMaxPerRoute(5);// 每路由最大连接数，默认值是2
        }
    }

    /**
     * 通过连接池获取HttpClient
     *
     * @return
     */
    public CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            init();
            httpClient = HttpClients.custom().setConnectionManager(cm).build();

        }
        return httpClient;
    }


```

