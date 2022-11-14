package org.cmccx.src.gallery;

import org.cmccx.config.BaseException;
import org.cmccx.src.user.UserProvider;
import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.cmccx.config.BaseResponseStatus.*;

@Service
public class GalleryService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GalleryProvider galleryProvider;
    private final GalleryDao galleryDao;
    private final UserProvider userProvider;
    private final JwtService jwtService;

    public GalleryService(GalleryProvider galleryProvider, GalleryDao galleryDao, UserProvider userProvider, JwtService jwtService) {
        this.galleryProvider = galleryProvider;
        this.galleryDao = galleryDao;
        this.userProvider = userProvider;
        this.jwtService = jwtService;
    }

    /** 갤러리 생성 **/
    public long registerGallery(String name) throws BaseException {
        try {
            // 회원 ID 검증 및 추출
            long userId = jwtService.getUserId();

            // 유효한 회원인지 확인
            int isValid = userProvider.checkUserId(userId);
            if(isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 갤러리명 중복 검사
            boolean isDuplicated = galleryDao.checkGalleryName(userId, name);
            if (isDuplicated) {
                throw new BaseException(DUPLICATED_GALLERY_NAME);
            }

            return galleryDao.insertGallery(userId, name);

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("RegisterGallery Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 갤러리명 수정 **/
    public String modifyGalleryName(long galleryId, String name) throws BaseException {
        try {
            // 회원 ID 검증 및 추출
            long userId = jwtService.getUserId();

            // 유효한 회원인지 확인
            int isValid = userProvider.checkUserId(userId);
            if(isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 갤러리-회원 관계 확인
            boolean isValidGallery = galleryProvider.checkGalleryByUserId(galleryId);
            if (!isValidGallery) {
                throw new BaseException(INVALID_USER_JWT);
            }

            // 갤러리명 중복 검사
            boolean isDuplicated = galleryDao.checkGalleryName(userId, name);
            if (isDuplicated) {
                throw new BaseException(DUPLICATED_GALLERY_NAME);
            }

            // 갤러리명 변경
            int result = galleryDao.updateGalleryName(galleryId, name);
            if (result == 0){
                throw new BaseException(BAD_REQUEST);
            }

            String message = "갤러리명이 변경되었습니다.";
            return message;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("RegisterGallery Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 갤러리 삭제 **/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public String removeGalley(long galleryId) throws BaseException {
        try {
            // 회원 ID 검증 및 추출
            long userId = jwtService.getUserId();

            // 유효한 회원인지 확인
            int isValid = userProvider.checkUserId(userId);
            if(isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 갤러리-회원 관계 확인
            boolean isValidGallery = galleryProvider.checkGalleryByUserId(galleryId);
            if (!isValidGallery) {
                throw new BaseException(INVALID_USER_JWT);
            }

            // 갤러리 삭제
            int result = galleryDao.deleteGalley(galleryId);
            if (result == 0){
                throw new BaseException(BAD_REQUEST);
            }

            String message = "갤러리가 삭제되었습니다.";
            return message;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("RegisterGallery Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 작품 찜 기능 **/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public boolean likeArt(long galleryId, long artId) throws BaseException {
        try {
            // 회원 ID 검증 및 추출
            long userId = jwtService.getUserId();

            // 유효한 회원인지 확인
            int isValid = userProvider.checkUserId(userId);
            if(isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 갤러리-회원 관계 확인
            boolean isValidGallery = galleryProvider.checkGalleryByUserId(galleryId);
            if (!isValidGallery) {
                throw new BaseException(INVALID_USER_JWT);
            }

            boolean status = galleryProvider.checkBookmarkStatus(galleryId, artId);
            if (status) {   // 찜 해제
                galleryDao.deleteBookmark(galleryId, artId);
                return galleryProvider.checkBookmarkStatus(galleryId, artId);
            } else {  // 찜 추가
                galleryDao.insertBookmark(galleryId, artId);
                return galleryProvider.checkBookmarkStatus(galleryId, artId);
            }
        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("LkeArt Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
