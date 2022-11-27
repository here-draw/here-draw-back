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

import java.util.List;
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
     * 자동 로그인 API
     * [POST] /users/login
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/login")
    public BaseResponse<String> login() throws BaseException {
        try {
            long userIdByJwt = jwtService.getUserId();
            userService.login(userIdByJwt);

            String result = "로그인에 성공하였습니다.";
            return new BaseResponse<>(result);
        } catch(BaseException e) {
            throw new BaseException(e.getStatus(), e.getMessage());
        } catch(Exception e) {
            logger.error("Auto Login Error", e);
            throw new BaseException(RESPONSE_ERROR);
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
     * 애플 로그인 API
     * [POST] /users/apple
     * @return BaseResponse<PostLoginRes>
     */
    @ResponseBody
    @PostMapping("/apple")
    public BaseResponse<PostLoginRes> loginByApple(@RequestBody @Valid PostLoginReq appleToken) throws BaseException {
        try {
            String identityToken = appleToken.getAccessToken();
            PostLoginRes loginRes = userService.loginByApple(identityToken);

            return new BaseResponse<>(loginRes);
        } catch(BaseException e) {
            throw new BaseException(e.getStatus(), e.getMessage());
        } catch(Exception e) {
            logger.error("LoginByApple Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /**
     * 초기 닉네임 설정 API
     * [PATCH] /users/nickname
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/nickname")
    public BaseResponse<String> modifyNickname(@RequestBody Map<String,String> map) throws BaseException {
        try {
            long userIdByJwt = jwtService.getUserId();
            userService.modifyNickname(userIdByJwt, map.get("nickname"));
            userService.createDefaultGallery(userIdByJwt);

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
     * 닉네임 중복 체크 API
     * [GET] /users/check?nickname={nickname}
     * @return BaseResponse<String>
     */
    @ResponseBody
    @GetMapping("/check")
    public BaseResponse<String> checkNickname(@RequestParam(required = true) String nickname) throws BaseException {
        try {
            int isExist = userProvider.checkNickname(nickname);

            String result;
            if(isExist == 0) {
                result = "사용 가능한 닉네임입니다.";
            } else {
                throw new BaseException(DUPLICATED_NICKNAME);
            }
            return new BaseResponse<>(result);
        } catch(BaseException e) {
            throw new BaseException(e.getStatus());
        } catch(Exception e) {
            logger.error("CheckNickname Error", e);
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

    /**
     * 프로필 정보 수정 API
     * [PATCH] /users/mypage/profile
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/mypage/profile")
    public BaseResponse<String> modifyProfileInfo(@Valid @RequestPart(value = "profileInfo") ProfileInfo profileInfo,
                                                  @RequestPart(value = "image", required = false) MultipartFile image) throws BaseException {
        try {
            long userIdByJwt = jwtService.getUserId();
            userService.modifyProfileInfo(userIdByJwt, profileInfo, image);

            String result = "프로필 수정이 완료되었습니다.";
            return new BaseResponse<>(result);
        } catch(BaseException e) {
            throw new BaseException(e.getStatus());
        } catch(Exception e) {
            logger.error("ModifyProfileInfo Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /**
     * 팔로우 API
     * [POST] /users/:user-id/follow
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/{user-id}/follow")
    public BaseResponse<String> postFollow(@PathVariable("user-id") long targetId) throws BaseException {
        try {
            long userIdByJwt = jwtService.getUserId();
            userService.postFollow(userIdByJwt, targetId);
            String result = "팔로우 요청에 성공하였습니다.";

            return new BaseResponse<>(result);
        } catch(BaseException e) {
            throw new BaseException(e.getStatus());
        } catch(Exception e) {
            logger.error("PostFollow Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /**
     * 팔로워 목록 조회 API
     * [GET] /users/follower-list
     * @return BaseResponse<List<ProfileInfo>>
     */
    @ResponseBody
    @GetMapping("/follower-list")
    public BaseResponse<List<ProfileInfo>> getFollowerList() throws BaseException {
        try {
            long userIdByJwt = jwtService.getUserId();
            List<ProfileInfo> profileInfoList = userProvider.getFollowerList(userIdByJwt);

            return new BaseResponse<>(profileInfoList);
        } catch(BaseException e) {
            throw new BaseException(e.getStatus());
        } catch(Exception e) {
            logger.error("GetFollowerList Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /**
     * 팔로잉 목록 조회 API
     * [GET] /users/following-list
     * @return BaseResponse<List<ProfileInfo>>
     */
    @ResponseBody
    @GetMapping("/following-list")
    public BaseResponse<List<ProfileInfo>> getFollowingList() throws BaseException {
        try {
            long userIdByJwt = jwtService.getUserId();
            List<ProfileInfo> profileInfoList = userProvider.getFollowingList(userIdByJwt);

            return new BaseResponse<>(profileInfoList);
        } catch(BaseException e) {
            throw new BaseException(e.getStatus());
        } catch(Exception e) {
            logger.error("GetFollowingList Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /**
     * 팔로우 취소 API
     * [DELETE] /users/:user-id/unfollow
     * @return BaseResponse<String>
     */
    @ResponseBody
    @DeleteMapping("/{user-id}/unfollow")
    public BaseResponse<String> deleteFollow(@PathVariable("user-id") long targetId) throws BaseException {
        try {
            long userIdByJwt = jwtService.getUserId();
            userService.deleteFollow(userIdByJwt, targetId);
            String result = "팔로우 취소 요청에 성공하였습니다.";

            return new BaseResponse<>(result);
        } catch(BaseException e) {
            throw new BaseException(e.getStatus());
        } catch(Exception e) {
            logger.error("DeleteFollow Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /**
     * 마이페이지 상단 조회 API
     * [GET] /users/mypage
     * @return BaseResponse<LikeInfo>
     */
    @ResponseBody
    @GetMapping("/mypage")
    public BaseResponse<LikeInfo> getLikeInfo() throws BaseException {
        try {
            long userIdByJwt = jwtService.getUserId();
            LikeInfo likeInfo = userProvider.getLikeInfo(userIdByJwt);

            return new BaseResponse<>(likeInfo);
        } catch(BaseException e) {
            throw new BaseException(e.getStatus());
        } catch(Exception e) {
            logger.error("GetLikeInfo Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /**
     * 작가 정보 조회 API
     * [GET] /users/:artist-id/artist-info
     * @return BaseResponse<ArtistInfo>
     */
    @ResponseBody
    @GetMapping("/{artist-id}/artist-info")
    public BaseResponse<ArtistInfo> getArtistInfo(@PathVariable("artist-id") long artistId) throws BaseException {
        try {
            long userIdByJwt = jwtService.getUserId();
            ArtistInfo artistInfo = userProvider.getArtistInfo(userIdByJwt, artistId);

            return new BaseResponse<>(artistInfo);
        } catch(BaseException e) {
            throw new BaseException(e.getStatus());
        } catch(Exception e) {
            logger.error("GetArtistInfo Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /**
     * 작가별 작품 목록 조회 API
     * [GET] /users/{artist-id}/arts?type={type}&art-id={art-id}&size={size}
     * @return BaseResponse<GetArtsByUserRes>
     */
    @ResponseBody
    @GetMapping("/{artist-id}/arts")
    public BaseResponse<GetArtsByUserRes> getArtsByUser(@PathVariable(value = "artist-id") long artistId,
                                                        @RequestParam(value = "type", defaultValue = "artist") String type,
                                                        @RequestParam(value = "art-id", defaultValue = "0") long artId,
                                                        @RequestParam(value = "size", defaultValue = "10000") int size) throws BaseException {
        GetArtsByUserRes result = userProvider.getArtsByUser(artistId, type, artId, size);

        return new BaseResponse<>(result);
    }

    /**
     * 애플 탈퇴 API
     * [POST] /users/apple/revoke
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/apple/revoke")
    public BaseResponse<String> revokeByApple(@RequestBody @Valid AppleWithdrawalReq appleWithdrawalReq) throws BaseException {
        try {
            long userIdByJwt = jwtService.getUserId();
            userService.revokeByApple(appleWithdrawalReq.getAuthorizationCode());

            String result = "회원 탈퇴에 성공하였습니다.";
            return new BaseResponse<>(result);
        } catch(BaseException e) {
            throw new BaseException(e.getStatus());
        } catch(Exception e) {
            logger.error("RevokeByApple Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }
}
