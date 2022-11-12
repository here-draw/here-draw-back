package org.cmccx.src.user;

import org.cmccx.src.user.model.*;

import org.cmccx.utils.JwtService;
import org.cmccx.utils.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponse;
import static org.cmccx.config.BaseResponseStatus.*;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class UserProvider {

    private final UserDao userDao;
    private final JwtService jwtService;
    private final S3Service s3Service;
    
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UserProvider(UserDao userDao, JwtService jwtService, S3Service s3Service) {
        this.userDao = userDao;
        this.jwtService = jwtService;
        this.s3Service = s3Service;
    }

    // 회원 가입 여부 확인
    public UserInfo checkUser(String socialType, String socialId) throws BaseException {
        try {
            return userDao.checkUser(socialType, socialId);
        } catch (Exception e){
            logger.error("CheckUser Error(UserDao)", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 닉네임 중복 여부 확인
    public int checkNickname(String nickname) throws BaseException {
        try {
            return userDao.checkNickname(nickname);
        } catch (Exception e){
            logger.error("CheckNickname Error(UserDao)", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 카카오에서 사용자 정보 조회
    public String getKakaoUserInfo(String accessToken) throws BaseException{
        String reqURL = "https://kapi.kakao.com/v2/user/me";
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "bearer " + accessToken);
            LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> restRequest = new HttpEntity<>(params, headers);

            return restTemplate.postForObject(reqURL, restRequest, String.class);

        } catch (Exception e){
            logger.error("Kakao API Fail", e);
            throw new BaseException(INVALID_ACCESS_TOKEN);
        }
    }

    // 마이페이지 - 프로필 정보 조회
    public ProfileInfo getProfileInfo(long userId) throws BaseException {
        try {
            return userDao.getProfileInfo(userId);
        } catch (Exception e){
            logger.error("GetProfileInfo Error(UserDao)", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public LikeInfo getLikeInfo(long userId) throws BaseException {
        try {
            return userDao.getLikeInfo(userId);
        } catch (Exception e){
            logger.error("GetLikeInfo Error(UserDao)", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public ArtistInfo getArtistInfo(long userId, long artistId) throws BaseException {
        try {
            int check = userDao.checkUserId(artistId);
            if(check == 1) {
                return userDao.getArtistInfo(userId, artistId);
            } else {
                throw new BaseException(BAD_REQUEST);
            }
        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e){
            logger.error("GetArtistInfo Error(UserDao)", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
