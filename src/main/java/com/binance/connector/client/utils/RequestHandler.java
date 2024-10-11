package com.binance.connector.client.utils;

import com.binance.connector.client.enums.HttpMethod;
import com.binance.connector.client.exceptions.BinanceConnectorException;
import com.binance.connector.client.utils.signaturegenerator.SignatureGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class RequestHandler {
    private final String apiKey;
    private final SignatureGenerator signatureGenerator;
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private final ProxyAuth proxy;

    public RequestHandler(String apiKey, ProxyAuth proxy) {
        this(apiKey, null, proxy);
    }

    public RequestHandler(String apiKey, SignatureGenerator signatureGenerator, ProxyAuth proxy) {
        this.apiKey = apiKey;
        this.signatureGenerator = signatureGenerator;
        this.proxy = proxy;
    }

    public String sendPublicRequest(String baseUrl, String urlPath, Map<String, Object> parameters, HttpMethod httpMethod, boolean showLimitUsage) {
        String fullUrl = UrlBuilder.buildFullUrl(baseUrl, urlPath, parameters);
        logger.info("{} {}", httpMethod, fullUrl);

        return ResponseHandler.handleResponse(RequestBuilder.buildPublicRequest(fullUrl, httpMethod), showLimitUsage, proxy);
    }

    public String sendApiRequest(String baseUrl, String urlPath, Map<String, Object> parameters, HttpMethod httpMethod, boolean showLimitUsage) {
        if (null == apiKey || apiKey.isEmpty()) {
            throw new BinanceConnectorException("[RequestHandler] API key cannot be null or empty!");
        }

        String fullUrl = UrlBuilder.buildFullUrl(baseUrl, urlPath, parameters);
        logger.info("{} {}", httpMethod, fullUrl);

        return ResponseHandler.handleResponse(RequestBuilder.buildApiKeyRequest(fullUrl, httpMethod, apiKey), showLimitUsage, proxy);
    }

    public String sendSignedRequest(String baseUrl, String urlPath, Map<String, Object> parameters, HttpMethod httpMethod, boolean showLimitUsage) {                          
        return ResponseHandler.handleResponse(RequestBuilder.buildSignedRequest(baseUrl, urlPath, httpMethod, parameters, apiKey, signatureGenerator), showLimitUsage, proxy);
    }
}
