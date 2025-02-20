package net.risesoft.config;

import net.risesoft.exception.ErrorCode;
import net.risesoft.exception.GlobalErrorCodeEnum;
import net.risesoft.pojo.Y9Result;
import net.risesoft.y9.configuration.Y9Properties;
import net.risesoft.y9.configuration.feature.oauth2.resource.Y9Oauth2ResourceProperties;
import net.risesoft.y9.json.Y9JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;
import y9.oauth2.resource.filter.OAuth20IntrospectionAccessTokenSuccessResponse;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

public class TokenAuthenticationFilter implements Filter {
    private final RestTemplate restTemplate = new RestTemplate();
    private final Y9Properties y9Properties;
    private final Y9Oauth2ResourceProperties y9Oauth2ResourceProperties;
    private String token;

    public TokenAuthenticationFilter(Y9Properties y9Properties,String token) {
        this.y9Properties = y9Properties;
        this.y9Oauth2ResourceProperties = y9Properties.getFeature().getOauth2().getResource();
        this.token = token;
    }
    private String obtainToken(HttpServletRequest request1) {
        String bearerToken = request1.getHeader("Authorization");
        if (StringUtils.isNotBlank(bearerToken) && bearerToken.startsWith("Basic ")) {
            return bearerToken.substring(6);
        }
        return null;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String accessToken = obtainToken(request);
        if(accessToken!=null){
            accessToken = base64Decode(accessToken);
            if(!accessToken.equals(token)){
                this.setResponse(response, HttpStatus.UNAUTHORIZED, GlobalErrorCodeEnum.API_SIGN_INCORRECT);
                return;
            }
            filterChain.doFilter(servletRequest, servletResponse);
        }else {
            accessToken = getAccessTokenFromRequest(request);
            if(StringUtils.isBlank(accessToken)){
                this.setResponse(response, HttpStatus.UNAUTHORIZED, GlobalErrorCodeEnum.ACCESS_TOKEN_NOT_FOUND);
                return;
            }
            ResponseEntity introspectEntity = null;

            try {
                introspectEntity = this.getToken(accessToken);
            } catch (Exception var19) {
                this.setResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, GlobalErrorCodeEnum.FAILURE);
                return;
            }
            OAuth20IntrospectionAccessTokenSuccessResponse introspectionResponse = (OAuth20IntrospectionAccessTokenSuccessResponse)introspectEntity.getBody();
            if (!introspectionResponse.isActive()) {
                this.setResponse(response, HttpStatus.UNAUTHORIZED, GlobalErrorCodeEnum.ACCESS_TOKEN_EXPIRED);
                return;
            }
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }

    private ResponseEntity<OAuth20IntrospectionAccessTokenSuccessResponse> getToken(String accessToken){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(this.y9Oauth2ResourceProperties.getOpaque().getClientId(), this.y9Oauth2ResourceProperties.getOpaque().getClientSecret(), StandardCharsets.UTF_8);
        String var10000 = this.y9Oauth2ResourceProperties.getOpaque().getIntrospectionUri();
        URI uri = URI.create(var10000 + "?token=" + accessToken);
        RequestEntity<?> requestEntity = new RequestEntity(headers, HttpMethod.POST, uri);
        ResponseEntity<OAuth20IntrospectionAccessTokenSuccessResponse> responseEntity = this.restTemplate.exchange(requestEntity, OAuth20IntrospectionAccessTokenSuccessResponse.class);
        return responseEntity;
    }
    private String getAccessTokenFromRequest(final HttpServletRequest request) {
        String accessToken = request.getParameter("access_token");
        if (StringUtils.isBlank(accessToken)) {
            String authHeader = request.getHeader("Authorization");
            if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring("Bearer ".length());
            }
        }

        return accessToken;
    }
    private void setResponse(HttpServletResponse response, HttpStatus httpStatus, ErrorCode errorCode) {
        response.addHeader("WWW-Authenticate", "Bearer realm=\"risesoft\"");
        response.setStatus(httpStatus.value());
        response.setContentType("application/json");
        try {
            response.getWriter().write(Y9JsonUtil.writeValueAsString(Y9Result.failure(errorCode)));
        } catch (IOException var5) {

        }

    }
    //base64解码
    private String base64Decode(String base64EncodedString){
        // 获取Base64解码器
        Base64.Decoder decoder = Base64.getDecoder();

        // 解码字符串
        byte[] decodedBytes = decoder.decode(base64EncodedString);

        // 将字节数组转换为字符串（可选）
        String decodedString = new String(decodedBytes);
        return decodedString;
    }
}
