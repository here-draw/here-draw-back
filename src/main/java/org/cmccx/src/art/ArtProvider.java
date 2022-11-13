package org.cmccx.src.art;

import org.cmccx.config.BaseException;
import org.cmccx.src.art.model.ArtInfo;
import org.cmccx.src.art.model.GetArtByArtIdRes;
import org.cmccx.src.art.model.GetArtsRes;
import org.cmccx.utils.JwtService;
import org.cmccx.utils.ScrollPagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.cmccx.config.BaseResponseStatus.*;

@Service
public class ArtProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ArtDao artDao;
    private final JwtService jwtService;

    @Autowired
    public ArtProvider(ArtDao artDao, JwtService jwtService) {
        this.artDao = artDao;
        this.jwtService = jwtService;
    }

    /**
     * 작품 ID 확인
     **/
    public int checkArt(long artId) throws BaseException {
        try {
            return artDao.checkArt(artId);
        } catch (Exception e) {
            logger.error("CheckArtId Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 작품 상태 조회
     **/
    public String checkArtStatus(long artId) throws BaseException {
        try {
            return artDao.checkArtStatus(artId);
        } catch (Exception e) {
            logger.error("CheckArtStatus Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 작품 판매 수량 조회
     **/
    public int checkArtSales(long artId) throws BaseException {
        try {
            return artDao.selectSales(artId);
        } catch (Exception e) {
            logger.error("CheckArtSales Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 작가-작품 관계 확인
     **/
    public int checkUserArt(long userId, long artId) throws BaseException {
        try {
            return artDao.checkUserArt(userId, artId);
        } catch (Exception e) {
            logger.error("CheckUserArt Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 작가:작품명 중복 확인
     **/
    public int checkArtTitle(long userId, String title, long artId) throws BaseException {
        try {
            return artDao.checkArtTitle(userId, title, artId);
        } catch (Exception e) {
            logger.error("CheckArtTitle Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 메인: 작품 조회(최신 등록순)
     **/
    public GetArtsRes getArts(int categoryId, long artId, String date, int size) throws BaseException {
        try {
            List<ArtInfo> artInfoList = artDao.selectArts(categoryId, artId, date, size);
            ScrollPagination<ArtInfo> scrollInfo = ScrollPagination.of(artInfoList, size);
            GetArtsRes result = GetArtsRes.of(scrollInfo);

            return result;

        } catch (Exception e) {
            logger.error("GetArts Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /**
     * 추천 작품 조회
     **/
    public List<ArtInfo> getRecommendedArts(long artId) throws BaseException {
        try {
            // 회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

            List<ArtInfo> result = artDao.selectRecommendedArts(userId, artId);

            return result;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("GetRecommendedArts Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /** 작품 상세 조회 **/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public GetArtByArtIdRes getArtByArtId(long artId) throws BaseException {
        try {
            // 회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

            // 작품ID 확인
            int isArt = artDao.checkArt(artId);
            if (isArt == 0) {
                throw new BaseException(FAILED_ACCESS_ART);
            }

            // 작품 상세 정보 조회
            GetArtByArtIdRes result = artDao.selectArtByArtId(artId);

            // 최근 본 작품 등록
            artDao.insertRecentArt(userId, artId);

            // 내 작품 조회인지 확인
            if (userId == result.getArtistId()) {
                result.setMyArt(true);
            }
            return result;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("GetArtByArtId Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /** 최근 본 작품 조회 **/
    public List<ArtInfo> getRecentArts() throws BaseException {
        try {
            // 회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

            List<ArtInfo> result = artDao.selectRecentArts(userId);
            return result;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("GetRecentArts Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }
}