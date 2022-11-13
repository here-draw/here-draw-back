package org.cmccx.src.user;

import org.cmccx.src.art.model.ArtInfo;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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

    // 유저 아이디 체크
    public int checkUserId(long userId) throws BaseException {
        try {
            return userDao.checkUserId(userId);
        } catch (Exception e) {
            logger.error("CheckUserId Error(UserDao)", e);
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

    // 팔로워 목록 조회
    public List<ProfileInfo> getFollowerList(long userId) throws BaseException {
        try {
            int check = userDao.checkUserId(userId);
            if(check == 1) {
                List<ProfileInfo> profileInfoList = userDao.getFollowerList(userId);
                return profileInfoList;
            } else {
                throw new BaseException(INVALID_ACCESS_TOKEN);
            }
        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e){
            logger.error("GetFollowerList Error(UserDao)", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 팔로잉 목록 조회
    public List<ProfileInfo> getFollowingList(long userId) throws BaseException {
        try {
            int check = userDao.checkUserId(userId);
            if(check == 1) {
                List<ProfileInfo> profileInfoList = userDao.getFollowingList(userId);
                return profileInfoList;
            } else {
                throw new BaseException(INVALID_ACCESS_TOKEN);
            }
        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e){
            logger.error("GetFollowingList Error(UserDao)", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 작가별 작품 조회 **/
    public GetArtsByUserRes getArtsByUser(long artistId, String type, long artId, int size) throws BaseException {
        try {
            List<ArtInfo> artList;
            GetArtsByUserRes result;

            // 회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

            // 작가ID 확인
            int isUser = userDao.checkUserId(artistId);
            if (isUser == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            if (type.equals("my")) {
                if (userId == artistId) { // MyPage
                    artList = userDao.selectArtsByUserId(userId, artistId, true, artId, size);
                    result = new GetArtsByUserRes(artList.size(), artList);

                } else {
                    throw new BaseException(INVALID_USER_JWT);
                }
            } else {    // 그 외 화면
                artList = userDao.selectArtsByUserId(userId, artistId, false, artId, size);
                result = new GetArtsByUserRes(artList.size(), artList);
            }

            return result;
        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("GetArtsByUser Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }
}
