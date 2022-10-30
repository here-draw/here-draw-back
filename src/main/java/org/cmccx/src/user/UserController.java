package org.cmccx.src.user;

import org.cmccx.src.user.model.*;

import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cmccx.utils.S3Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.cmccx.config.BaseResponse;
import org.cmccx.config.BaseException;
import static org.cmccx.config.BaseResponseStatus.*;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/users")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserProvider userProvider;
    private final UserService userService;
    private final JwtService jwtService;
    private final S3Service s3Service;

    @Autowired
    public UserController(UserProvider userProvider, UserService userService, JwtService jwtService, S3Service s3Service){
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
        this.s3Service = s3Service;
    }

    /**
     * 로그 테스트 API
     * [GET] /users/log
     * @return String
     */
    @ResponseBody
    @GetMapping("/log")
    public String getAll() {
        System.out.println("테스트");

//        info 레벨은 Console 로깅 O, 파일 로깅 X
        logger.info("INFO Level 테스트");
//        warn 레벨은 Console 로깅 O, 파일 로깅 O
        logger.warn("Warn Level 테스트");
//        error 레벨은 Console 로깅 O, 파일 로깅 O (app.log 뿐만 아니라 error.log 에도 로깅 됨)
//        app.log 와 error.log 는 날짜가 바뀌면 자동으로 *.gz 으로 압축 백업됨
        logger.error("ERROR Level 테스트");

        return "Success Test";
    }

    /**
     * Amazon S3에 이미지 업로드 테스트
     * @return 성공 시 200 Success와 함께 업로드 된 파일의 파일명 리스트 반환
     */
    @PostMapping("/image")
    public BaseResponse<String> uploadFile(@RequestParam("images") MultipartFile multipartFile) {
        try{
            return new BaseResponse<>(s3Service.uploadImage(multipartFile));
        } catch (BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 카카오로 로그인 API
     * [POST] /users/kakao
     * @return BaseResponse<PostLoginRes>
     */
    @ResponseBody
    @PostMapping("/kakao")
    public BaseResponse<PostLoginRes> loginByKakao(@RequestBody @Valid PostLoginReq kakaoToken) throws BaseException {
        try {
            String accessToken = kakaoToken.getAccessToken();
            PostLoginRes loginRes = userService.loginByKakao(accessToken);

            return new BaseResponse<>(loginRes);
        } catch(BaseException e) {
            throw new BaseException(e.getStatus(), e.getMessage());
        } catch(Exception e) {
            logger.error("LoginByKakao Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /**
     * 닉네임 설정(변경) API
     * [PATCH] /users/nickname
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/nickname")
    public BaseResponse<String> modifyNickname(@RequestBody Map<String,String> map) throws BaseException {
        try {
            long userIdByJwt = jwtService.getUserId();
            userService.modifyNickname(userIdByJwt, map.get("nickname"));

            String result = "닉네임 설정이 완료되었습니다.";
            return new BaseResponse<>(result);
        } catch(BaseException e) {
            throw new BaseException(e.getStatus());
        } catch(Exception e) {
            logger.error("ModifyNickname Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /**
     * 프로필 정보 조회 API
     * [GET] /users/mypage/profile
     * @return BaseResponse<ProfileInfo>
     */
    @ResponseBody
    @GetMapping("/mypage/profile")
    public BaseResponse<ProfileInfo> getProfileInfo() throws BaseException {
        try {
            long userIdByJwt = jwtService.getUserId();
            ProfileInfo profileInfo = userProvider.getProfileInfo(userIdByJwt);

            return new BaseResponse<>(profileInfo);
        } catch(BaseException e) {
            throw new BaseException(e.getStatus());
        } catch(Exception e) {
            logger.error("GetProfileInfo Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }
}
