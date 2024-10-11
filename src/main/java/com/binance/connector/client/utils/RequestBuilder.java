package com.binance.connector.client.utils;

import com.binance.connector.client.enums.HttpMethod;
import com.binance.connector.client.exceptions.BinanceConnectorException;

import com.binance.connector.client.utils.signaturegenerator.Ed25519SignatureGenerator;
import com.binance.connector.client.utils.signaturegenerator.HmacSignatureGenerator;
import com.binance.connector.client.utils.signaturegenerator.RsaSignatureGenerator;
import com.binance.connector.client.utils.signaturegenerator.SignatureGenerator;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.HashMap;
import java.util.Map;

public final class RequestBuilder {
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final String USER_AGENT = "binance-connector-java/3.4.0";
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    private RequestBuilder() {
    }
    public static Request buildPublicRequest(String fullUrl, HttpMethod httpMethod) {
        try {
            final Request.Builder requestBuilder = new Request.Builder().addHeader("User-Agent", USER_AGENT).addHeader("Content-Type", CONTENT_TYPE).url(fullUrl);
            switch (httpMethod) {
                case POST:
                    return requestBuilder.post(RequestBody.create("", JSON_TYPE)).build();
                case GET:
                    return requestBuilder.get().build();
                case PUT:
                    return requestBuilder.put(RequestBody.create("", JSON_TYPE)).build();
                case DELETE:
                    return requestBuilder.delete().build();
                default:
                    throw new BinanceConnectorException("Invalid HTTP method: " + httpMethod);
            }
        } catch (IllegalArgumentException e) {
            throw new BinanceConnectorException("Invalid URL: " + e.getMessage());
        }
    }

    public static Request buildApiKeyRequest(String fullUrl, HttpMethod httpMethod, String apiKey) {
        try {
            final Request.Builder requestBuilder = new Request.Builder().addHeader("User-Agent", USER_AGENT).addHeader("Content-Type", CONTENT_TYPE).addHeader("X-MBX-APIKEY", apiKey).url(fullUrl);
            switch (httpMethod) {
                case POST:
                    return requestBuilder.post(RequestBody.create("", JSON_TYPE)).build();
                case GET:
                    return requestBuilder.get().build();
                case PUT:
                    return requestBuilder.put(RequestBody.create("", JSON_TYPE)).build();
                case DELETE:
                    return requestBuilder.delete().build();
                default:
                    throw new BinanceConnectorException("Invalid HTTP method: " + httpMethod);
            }
        } catch (IllegalArgumentException e) {
            throw new BinanceConnectorException("Invalid URL: " + e.getMessage());
        }
    }

    public static Request buildSignedRequest(String baseUrl, String urlPath, HttpMethod httpMethod, Map<String, Object> parameters, String apiKey, SignatureGenerator signatureGenerator) {
        if (signatureGenerator.getClass() == HmacSignatureGenerator.class && (null == apiKey || apiKey.isEmpty())) {
            throw new BinanceConnectorException("[RequestHandler] Secret key/API key cannot be null or empty!");
        }
        if ((signatureGenerator.getClass() == RsaSignatureGenerator.class || signatureGenerator.getClass() == Ed25519SignatureGenerator.class) && (null == apiKey || apiKey.isEmpty())) {
            throw new BinanceConnectorException("[RequestHandler] Private key/API key cannot be null or empty!");
        }

        parameters = (parameters == null) ? new HashMap<String, Object>() : parameters;
        parameters.putIfAbsent("timestamp", UrlBuilder.buildTimestamp());
        parameters.put("signature", signatureGenerator.getSignature(UrlBuilder.joinQueryParameters(parameters)));

        String fullUrl = UrlBuilder.buildFullUrl(baseUrl, urlPath, parameters);

        return RequestBuilder.buildApiKeyRequest(fullUrl, httpMethod, apiKey);
    }

    public static Request buildWebSocketRequest(String fullUrl) {
        return new Request.Builder().url(fullUrl).build();
    }
}
