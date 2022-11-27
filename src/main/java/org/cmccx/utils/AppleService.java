package org.cmccx.utils;

import org.cmccx.src.user.model.AppleToken;
import org.cmccx.config.secret.Secret;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import lombok.Getter;
import lombok.Setter;

import org.cmccx.config.BaseException;
import static org.cmccx.config.BaseResponseStatus.*;

import org.springframework.stereotype.Service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.*;

@Service
public class AppleService {
    private String KEY_ID = Secret.KEY_ID;
    private String TEAM_ID = Secret.TEAM_ID;
    private String CLIENT_ID = Secret.CLIENT_ID;
    private String KEY_PATH = Secret.KEY_PATH;

    @Getter
    @Setter
    public static class Key {
        private String kty;
        private String kid;
        private String use;
        private String alg;
        private String n;
        private String e;
    }

    // 애플 publicKey 가져오기
    public Key getMatchedKey(Map<String, String> header) throws BaseException, JsonProcessingException {
        String reqURL = "https://appleid.apple.com/auth/keys";

        // HTTP Request
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(reqURL, String.class);

        //써야하는 Element (kid, alg 일치하는 element)
        JsonNode availableObject = null;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode arrNode = objectMapper.readTree(result).get("keys");
        String header_kid = header.get("kid").replaceAll("\"", "");
        String header_alg = header.get("alg").replaceAll("\"", "");
        if (arrNode.isArray()) {
            for (JsonNode objNode : arrNode)
                if(objNode.get("kid").toString().replaceAll("\"", "").equals(header_kid))
                    if(objNode.get("alg").toString().replaceAll("\"", "").equals(header_alg)) {
                        availableObject = objNode;
                        break;
                    }
        }
        if (availableObject == null) {
            throw new BaseException(INVALID_ACCESS_TOKEN);
        }
        return objectMapper.treeToValue(availableObject, Key.class);
    }

    public Claims getClaimsBy(String identityToken) throws BaseException {
        try {
            String headerOfIdentityToken = identityToken.substring(0, identityToken.indexOf("."));
            Map<String, String> header = new ObjectMapper().readValue(new String(Base64.getDecoder().decode(headerOfIdentityToken), "UTF-8"), Map.class);

            AppleService.Key key = getMatchedKey(header);

            byte[] nBytes = Base64.getUrlDecoder().decode(key.getN());
            byte[] eBytes = Base64.getUrlDecoder().decode(key.getE());

            BigInteger n = new BigInteger(1, nBytes);
            BigInteger e = new BigInteger(1, eBytes);

            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
            KeyFactory keyFactory = KeyFactory.getInstance(key.getKty());
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            Claims userInfo = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(identityToken).getBody();

            return userInfo;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        }
        /* catch (ExpiredJwtException e) {
            //토큰이 만료됐기 때문에 클라이언트는 토큰을 refresh 해야함.
            e.printStackTrace();
        */
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String createClientSecret() throws IOException {
        Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .setHeaderParam("kid", KEY_ID)
                .setHeaderParam("alg", "ES256")
                .setIssuer(TEAM_ID)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .setAudience("https://appleid.apple.com")
                .setSubject(CLIENT_ID)
                .signWith(SignatureAlgorithm.ES256, getPrivateKey())
                .compact();
    }

    private PrivateKey getPrivateKey() throws IOException {
        ClassPathResource resource = new ClassPathResource(KEY_PATH);
        String privateKey = new String(Files.readAllBytes(Paths.get(resource.getURI())));
        Reader pemReader = new StringReader(privateKey);
        PEMParser pemParser = new PEMParser(pemReader);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
        return converter.getPrivateKey(object);
    }

    public AppleToken GenerateAuthToken(String authorizationCode) throws IOException {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        String authUrl = "https://appleid.apple.com/auth/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", CLIENT_ID);
        params.add("client_secret", createClientSecret());
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<AppleToken> response = restTemplate.postForEntity(authUrl, httpEntity, AppleToken.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new IllegalArgumentException("GenerateAuthToken Error");
        }
    }

    public void revoke(String authorizationCode) throws IOException {
        AppleToken appleAuthToken = GenerateAuthToken(authorizationCode);

        if (appleAuthToken.getAccessToken() != null) {
            RestTemplate restTemplate = new RestTemplateBuilder().build();
            String revokeUrl = "https://appleid.apple.com/auth/revoke";

            LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", CLIENT_ID);
            params.add("client_secret", createClientSecret());
            params.add("token", appleAuthToken.getAccessToken());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

            restTemplate.postForEntity(revokeUrl, httpEntity, String.class);
        }
    }
}