package org.cmccx.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import lombok.Getter;
import lombok.Setter;

import org.cmccx.config.BaseException;
import static org.cmccx.config.BaseResponseStatus.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;

@Service
public class AppleService {

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

        System.out.println("this is getMachedKey method!");
        System.out.println(header.get("kid"));
        System.out.println(header.get("alg"));
        System.out.println("!!!&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");

        // HTTP Request
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(reqURL, String.class);
        System.out.println(result);

        //써야하는 Element (kid, alg 일치하는 element)
        JsonNode availableObject = null;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode arrNode = objectMapper.readTree(result).get("keys");
        if (arrNode.isArray()) {
            for (JsonNode objNode : arrNode) {
                System.out.println(objNode.get("kid"));
                System.out.println(objNode.get("kid").toString());

                if (objNode.get("kid").toString().equals(header.get("kid"))) {
                    System.out.println("same kid");
                    if (objNode.get("alg").toString().equals(header.get("alg"))) {
                        System.out.println("same alg");
                        availableObject = objNode;
                        break;
                    }
                }
            }
        }
        if (availableObject == null) {
            System.out.println("throw this exception: " + "INVALID_ACCESS_TOKEN");
            throw new BaseException(INVALID_ACCESS_TOKEN);
        }
        return objectMapper.treeToValue(availableObject, Key.class);
    }

    public Claims getClaimsBy(String identityToken) {
        try {
            String headerOfIdentityToken = identityToken.substring(0, identityToken.indexOf("."));
            Map<String, String> header = new ObjectMapper().readValue(new String(Base64.getDecoder().decode(headerOfIdentityToken), "UTF-8"), Map.class);

            AppleService.Key key = getMatchedKey(header);
            System.out.println("returned key info");
            System.out.println(key.getN());
            System.out.println(key.getE());
            System.out.println("++++++++++++++++++++++++++++++++++");

            byte[] nBytes = Base64.getUrlDecoder().decode(key.getN());
            byte[] eBytes = Base64.getUrlDecoder().decode(key.getE());

            BigInteger n = new BigInteger(1, nBytes);
            BigInteger e = new BigInteger(1, eBytes);

            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
            KeyFactory keyFactory = KeyFactory.getInstance(key.getKty());
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            Claims userInfo = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(identityToken).getBody();

            return userInfo;

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
}