package org.cmccx.src.user;

import org.cmccx.src.user.model.*;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponse;
import static org.cmccx.config.BaseResponseStatus.*;

import org.cmccx.utils.JwtService;
import org.cmccx.utils.S3Service;
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

    @Autowired
    public UserService(UserDao userDao, UserProvider userProvider, JwtService jwtService, S3Service s3Service, ObjectMapper objectMapper) {
        this.userDao = userDao;
        this.userProvider = userProvider;
        this.jwtService = jwtService;
        this.s3Service = s3Service;
        this.objectMapper = objectMapper;
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
            long social_id = jsonNode.get("id").asLong();

            // 등록된 유저인지 확인
            UserInfo userInfo = userProvider.checkUser("K", social_id);

            if(userInfo == null){ // 미등록 유저
                // 회원가입
                KakaoInfo kakaoInfo = getKakaoInfo(jsonNode);
                userId = userDao.insertUser("K", social_id, kakaoInfo.getEmail(), kakaoInfo.getProfileImage());
            } else {
                userId = userInfo.getUserId();
                nickname = userInfo.getNickname();
                status = userInfo.getStatus();

                if(status.equals("I")) {
                    // 휴면 회원 -> 정보 다시 가져오고, 로그인 처리.
                    try {
                        userDao.recoveryUserInfo(userId);
                    } catch (Exception e) {
                        throw new BaseException(DATABASE_ERROR);
                    }
                } else if(status.equals("D")) {
                    // 탈퇴 회원 -> 가입 가능한 날짜인지 확인, 가입 처리.
                    LocalDate nowDate = LocalDate.now();
                    LocalDate enableSignUpDate = userDao.getEnableSignUpDate(userId);
                    int checkDate = nowDate.compareTo(enableSignUpDate);
                    if(checkDate < 0) {
                        throw new BaseException(INVALID_SIGNUP_USER, enableSignUpDate.format(DateTimeFormatter.ofPattern("YYYY년 MM월 dd일")));
                    } else {
                        KakaoInfo kakaoInfo = getKakaoInfo(jsonNode);
                        userId = userDao.insertUser("K", social_id, kakaoInfo.getEmail(), kakaoInfo.getProfileImage());
                    }
                } else if(status.equals("B")) {
                    // 차단 회원 -> 영구 차단이면 로그인 및 회원가입 불가, 차단 기한이 끝나지 않았으면 로그인 불가.
                    LocalDate permanentBlock = LocalDate.of(2999, 1, 1);
                    LocalDate blockedDate = userDao.getBlockedDate(userId);
                    if(blockedDate == null) {
                        throw new BaseException(DATABASE_ERROR);
                    }
                    boolean isBlocked = blockedDate.isEqual(permanentBlock);

                    if(isBlocked) {
                        throw new BaseException(BLOCKED_SIGNUP);
                    } else {
                        LocalDate nowDate = LocalDate.now();
                        int checkDate = nowDate.compareTo(blockedDate);
                        if(checkDate <= 0) {
                            // 차단기한 통보
                            throw new BaseException(BLOCKED_LOGIN, blockedDate.format(DateTimeFormatter.ofPattern("YYYY년 MM월 dd일")));
                        }
                    }
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
                kakaoInfo.setProfileImage(jsonNode.get("properties").get("profile_image").asText());
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
}
