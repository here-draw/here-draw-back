package org.cmccx.src.user;

import io.jsonwebtoken.Claims;
import org.cmccx.src.user.model.*;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponse;
import static org.cmccx.config.BaseResponseStatus.*;

import org.cmccx.utils.AppleService;
import org.cmccx.utils.JwtService;
import org.cmccx.utils.S3Service;
import org.cmccx.utils.FileService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import io.jsonwebtoken.Claims;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class UserService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserDao userDao;
    private final UserProvider userProvider;
    private final JwtService jwtService;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;
    private final FileService fileService;
    private final AppleService appleService;

    @Autowired
    public UserService(UserDao userDao, UserProvider userProvider, JwtService jwtService, S3Service s3Service, ObjectMapper objectMapper, FileService fileService, AppleService appleService) {
        this.userDao = userDao;
        this.userProvider = userProvider;
        this.jwtService = jwtService;
        this.s3Service = s3Service;
        this.objectMapper = objectMapper;
        this.fileService = fileService;
        this.appleService = appleService;
    }

    /** 자동 로그인 **/
    @Transactional(rollbackFor = Exception.class)
    public void login(long userId) throws BaseException{
        try {
            String status = userDao.getUserStatus(userId);
            if(!status.equals("A")) {
                throw new BaseException(BLOCKED_LOGIN, "로그인 불가능한 유저입니다. (" + status + ")");
            }
            userDao.updateLoginDate(userId);
        } catch (BaseException e) {
            throw new BaseException(e.getStatus(), e.getMessage());
        } catch (Exception e) {
            logger.error("Auto Login Fail", e);
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }

    /** 카카오로 로그인 **/
    @Transactional(rollbackFor = Exception.class)
    public PostLoginRes loginByKakao(String accessToken) throws BaseException{
        long userId;
        String nickname = null;
        String status = "A";

        try{
            // accessToken을 이용하여 유저 정보 추출
            String kakaoUserInfo = userProvider.getKakaoUserInfo(accessToken);
            JsonNode jsonNode = objectMapper.readTree(kakaoUserInfo);
            String socialId = jsonNode.get("id").toString();

            // 등록된 유저인지 확인
            UserInfo userInfo = userProvider.checkUser("K", socialId);

            if(userInfo == null){ // 미등록 유저
                // 회원가입
                KakaoInfo kakaoInfo = getKakaoInfo(jsonNode);
                userId = userDao.insertUser("K", socialId, kakaoInfo.getEmail(), kakaoInfo.getProfileImage());
            } else {
                userId = userInfo.getUserId();
                nickname = userInfo.getNickname();
                status = userInfo.getStatus();

                if(status.equals("I")) {
                    // 휴면 회원 -> 정보 다시 가져오고, 로그인 처리.
                    userDao.updateUserStatus(userId, "A");
                } else if(status.equals("D")) {
                    // 탈퇴 회원 -> 가입 가능한 날짜인지 확인, 가입 처리.
                    checkEnableDate(userId, status);
                    // 이전 userId 삭제
                    userDao.deletePrevUserId(userId);
                    // 새로운 userId 등록하고, 새로 가입 처리.
                    KakaoInfo kakaoInfo = getKakaoInfo(jsonNode);
                    userId = userDao.insertUser("K", socialId, kakaoInfo.getEmail(), kakaoInfo.getProfileImage());
                } else if(status.equals("P")) {
                    // 영구 차단
                    throw new BaseException(BLOCKED_SIGNUP);
                } else if(status.equals("B")) {
                    checkEnableDate(userId, status);
                    userDao.updateUserStatus(userId, "A");
                }
            }
            // 정상 로그인 처리
            return approvalUser(userId, nickname);
        } catch (BaseException e) {
            throw new BaseException(e.getStatus(), e.getMessage());
        } catch (Exception e) {
            logger.error("Kakao Login Fail", e);
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }

    /** 애플로 로그인 **/
    @Transactional(rollbackFor = Exception.class)
    public PostLoginRes loginByApple(String identityToken) throws BaseException{
        long userId = 0;
        String nickname = null;
        String status;
        try {
            Claims appleInfo = appleService.getClaimsBy(identityToken);
            String socialId = appleInfo.get("sub").toString();

            // 등록된 유저인지 확인
            UserInfo userInfo = userProvider.checkUser("A", socialId);
            if(userInfo == null){ // 미등록 유저
                // 회원가입
                userId = userDao.insertUser("A", socialId, appleInfo.get("email").toString(), null);
            } else {
                userId = userInfo.getUserId();
                nickname = userInfo.getNickname();
                status = userInfo.getStatus();
                if(status.equals("I")) {
                    userDao.updateUserStatus(userId, "A");
                } else if(status.equals("D")) {
                    // 탈퇴 회원 -> 가입 가능한 날짜인지 확인, 가입 처리.
                    checkEnableDate(userId, status);
                    // 이전 userId 삭제
                    userDao.deletePrevUserId(userId);
                    // 새로운 userId 등록하고, 새로 가입 처리.
                    userId = userDao.insertUser("A", socialId, appleInfo.get("email").toString(), null);
                } else if(status.equals("P")) {
                    // 영구 차단
                    throw new BaseException(BLOCKED_SIGNUP);
                } else if(status.equals("B")) {
                    checkEnableDate(userId, status);
                    userDao.updateUserStatus(userId, "A");
                }
            }
            return approvalUser(userId, nickname);
        } catch (BaseException e) {
            throw new BaseException(e.getStatus(), e.getMessage());
        } catch (Exception e) {
            logger.error("Apple Login Fail", e);
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }

    public void checkEnableDate(long userId, @NotNull String status) throws BaseException {
        try {
            LocalDate nowDate = LocalDate.now();
            int checkDate;
            if(status.equals("D")) {
                LocalDate enableSignUpDate = userDao.getEnableSignUpDate(userId);
                checkDate = nowDate.compareTo(enableSignUpDate);
                if(checkDate < 0) {
                    throw new BaseException(INVALID_SIGNUP_USER, enableSignUpDate.format(DateTimeFormatter.ofPattern("YYYY년 MM월 dd일")));
                }
            } else if(status.equals("B")) {
                LocalDate blockedDate = userDao.getBlockedDate(userId);
                checkDate = nowDate.compareTo(blockedDate);
                if(checkDate <= 0) {
                    // 차단기한 통보
                    throw new BaseException(BLOCKED_LOGIN, blockedDate.format(DateTimeFormatter.ofPattern("YYYY년 MM월 dd일")));
                }
            }
        } catch (BaseException e) {
            throw new BaseException(e.getStatus(), e.getMessage());
        }
    }

    // 로그인 승인 처리
    public PostLoginRes approvalUser(long userId, String nickname){
        String jwt = jwtService.createJwt(userId);
        userDao.updateLoginDate(userId);

        return new PostLoginRes(jwt, nickname);
    }

    // 카카오에서 (선택 동의 시)프로필, (필수 동의)이메일 정보 가져오기
    public KakaoInfo getKakaoInfo(JsonNode jsonNode) {
        KakaoInfo kakaoInfo = new KakaoInfo();
        kakaoInfo.setEmail(jsonNode.get("kakao_account").get("email").asText());
        try {
            if(jsonNode.get("kakao_account").get("profile").get("is_default_image").asBoolean()) {
                System.out.println("default image");
            } else {
                kakaoInfo.setProfileImage(jsonNode.get("properties").get("thumbnail_image").asText());
            }
        } catch (Exception e) {
            return kakaoInfo;
        }
        return kakaoInfo;
    }

    // 닉네임 설정(변경)
    public void modifyNickname(long userId, String nickname) throws BaseException{
        try {
            int isExist = userDao.checkNickname(nickname);
            if(isExist == 1) {
                throw new BaseException(DUPLICATED_NICKNAME);
            }
            userDao.modifyNickname(userId, nickname);
        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        }
    }

    // 기본 갤러리 생성
    public void createDefaultGallery(long userId) throws BaseException {
        try {
            userDao.createDefaultGallery(userId);
        } catch (Exception e){
            logger.error("CreateDefaultGallery Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 프로필 수정
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void modifyProfileInfo(long userId, ProfileInfo profileInfo, MultipartFile newImage) throws BaseException {
        String profileImgUrl = null;
        String prevProfileImgUrl = null;
        try {
            // 새로운 프로필 이미지 업로드 및 기존 프로필 삭제
            if (newImage != null) {
                // 이미지 확장자 검증
                boolean isValidFile = fileService.validateFile(newImage.getInputStream());
                if (!isValidFile) {
                    throw new BaseException(INVALID_IMAGE_FILE);
                }

                prevProfileImgUrl = userDao.getProfileImg(userId);
                profileImgUrl = s3Service.updateImage(prevProfileImgUrl, newImage);
            }

            if(newImage == null) {
                userDao.modifyProfileInfo(userId, profileInfo.getNickname(), profileInfo.getDescription());
            } else {
                userDao.modifyProfileInfo(userId, profileImgUrl, profileInfo.getNickname(), profileInfo.getDescription());
            }

        } catch (BaseException e){
            s3Service.deleteImage(profileImgUrl);
            throw new BaseException(e.getStatus());
        } catch (Exception e){
            s3Service.deleteImage(profileImgUrl);
            logger.error("ModifyProfileInfo Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 팔로우
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void postFollow(long userId, long targetId) throws BaseException {
        try {
            if(userDao.checkUserId(userId) == 0){ // 잘못된 JWT(해당 user없음)
                throw new BaseException(INVALID_JWT);
            }
            if(userDao.checkUserId(targetId) == 0) { // 해당 (target)user없음
                throw new BaseException(BAD_REQUEST);
            }
            if(userDao.checkFollowList(userId, targetId) == 1) { // FollowList 안에 이미 있는 경우 (A / I)
                if(userDao.patchFollowList(userId, targetId, "A") == 0) {
                    throw new BaseException(DUPLICATED_FOLLOW);
                }
            } else {
                try {
                    userDao.postFollowList(userId, targetId);
                } catch (Exception exception) {
                    throw new BaseException(DATABASE_ERROR);
                }
            }

        } catch (BaseException e){
            throw new BaseException(e.getStatus());
        } catch (Exception e){
            logger.error("PostFollow Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 팔로우 취소
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void deleteFollow(long userId, long targetId) throws BaseException {
        try {
            if(userDao.checkUserId(userId) == 0){ // 잘못된 JWT(해당 user없음)
                throw new BaseException(INVALID_JWT);
            }
            if(userDao.checkUserId(targetId) == 0) { // 해당 (target)user없음
                throw new BaseException(BAD_REQUEST);
            }
            if(userDao.checkFollowList(userId, targetId) == 1) { // FollowList 안에 이미 있는 경우 (A / I)
                if(userDao.patchFollowList(userId, targetId, "I") == 0) {
                    throw new BaseException(DUPLICATED_UNFOLLOW);
                }
            } else {
                throw new BaseException(DUPLICATED_UNFOLLOW);
            }

        } catch (BaseException e){
            throw new BaseException(e.getStatus());
        } catch (Exception e){
            logger.error("DeleteFollow Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 애플 탈퇴 **/
    @Transactional(rollbackFor = Exception.class)
    public void revokeByApple(String authorizationCode) {
        try {
            appleService.revoke(authorizationCode);
        } catch (Exception e) {
            logger.error("RevokeByApple Fail(Apple Service) Fail", e);
        }
    }
}
