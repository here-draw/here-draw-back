package org.cmccx.src.gallery;

import org.cmccx.config.BaseException;
import org.cmccx.src.art.model.ArtInfo;
import org.cmccx.src.gallery.model.GetGalleriesRes;
import org.cmccx.src.user.UserProvider;
import org.cmccx.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

import static org.cmccx.config.BaseResponseStatus.*;

@Service
public class GalleryProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GalleryDao galleryDao;
    private final UserProvider userProvider;
    private final JwtService jwtService;

    public GalleryProvider(GalleryDao galleryDao, UserProvider userProvider, JwtService jwtService) {
        this.galleryDao = galleryDao;
        this.userProvider = userProvider;
        this.jwtService = jwtService;
    }

    /** 갤러리-회원 관계 확인 **/
    public boolean checkGalleryByUserId(long galleryId) throws BaseException {
        try {
            // 회원 ID 검증 및 추출
            long userId = jwtService.getUserId();

            return galleryDao.checkGalleryByUserId(userId, galleryId);
        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("CheckGalleryByUserId", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 작품 찜 여부 조회 **/
    public boolean checkBookmarkStatus(long galleryId, long artId) throws BaseException {
        try {
            return galleryDao.checkBookmarkStatus(galleryId, artId);
        } catch (Exception e) {
            logger.error("CcheckBookmarkStatus error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 갤러리 목록 조회 **/
    @Transactional
    public List<GetGalleriesRes> getGalleries() throws BaseException {
        try {
            // 회원 ID 검증 및 추출
            long userId = jwtService.getUserId();

            // 유효한 회원인지 확인
            int isValid = userProvider.checkUserId(userId);
            if(isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 갤러리 기본 정보 조회
            List<GetGalleriesRes> result = galleryDao.selectGalleries(userId);

            // 갤러리별 대표이미지 4개 조회
            for (GetGalleriesRes g : result) {
                List<String> images = galleryDao.selectGalleyImages(g.getGalleryId());
                g.setGalleryImages(images);
            }

            return result;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("GetGalleries Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /** 갤러리 내 작품 목록 조회 **/
    public List<ArtInfo> getArtsByGalleryId(long galleryId) throws BaseException {
        try {
            // 회원 ID 검증 및 추출
            long userId = jwtService.getUserId();

            // 유효한 회원인지 확인
            int isValid = userProvider.checkUserId(userId);
            if(isValid == 0) {
                throw new BaseException(BAD_REQUEST);
            }

            // 갤러리-회원 관계 확인
            boolean isValidGallery = galleryDao.checkGalleryByUserId(userId, galleryId);
            if (!isValidGallery) {
                throw new BaseException(INVALID_USER_JWT);
            }

            // 갤러리 내 작품 조회
            List<ArtInfo> result = galleryDao.selectArtsByGalleryId(galleryId);
            return result;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("GetArtsByGalleryId Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }
}
