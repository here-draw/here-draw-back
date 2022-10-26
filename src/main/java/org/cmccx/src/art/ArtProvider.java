package org.cmccx.src.art;

import org.cmccx.config.BaseException;
import org.cmccx.src.art.model.ArtInfo;
import org.cmccx.src.art.model.GetArtByArtIdRes;
import org.cmccx.src.art.model.GetArtsByUserRes;
import org.cmccx.src.art.model.GetArtsRes;
import org.cmccx.utils.JwtService;
import org.cmccx.utils.ScrollPagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /** 작가-작품 관계 확인 **/
    public int checkUserArt(long userId, long artId) throws BaseException {
        try {
            return artDao.checkUserArt(userId, artId);
        } catch (Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 작가:작품명 중복 확인 **/
    public int checkArtTitle(long userId, String title, long artId) throws BaseException {
        try {
            return artDao.checkArtTitle(userId, title, artId);
        } catch (Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 메인: 작품 조회(최신 등록순) **/
    public GetArtsRes getArts(int categoryId, long artId, String date, int size) throws BaseException {
        try {
            List<ArtInfo> artInfoList = artDao.selectArts(categoryId, artId, date, size);
            ScrollPagination<ArtInfo> scrollInfo = ScrollPagination.of(artInfoList, size);
            GetArtsRes result = GetArtsRes.of(scrollInfo);

            return result;

        } catch (Exception e){
            logger.error("GetArts Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /** 추천 작품 조회 **/
    public List<ArtInfo> getRecommendedArts(long artId) throws BaseException {
        try {
            // 회원 검증 및 ID 추출
            long userId = jwtService.getUserIdx();

            List<ArtInfo> result = artDao.selectRecommendedArts(userId, artId);

            return result;

        } catch (BaseException e){
            throw new BaseException(e.getStatus());
        } catch (Exception e){
            logger.error("GetRecommendedArts Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /** 작가별 작품 조회 **/
    public GetArtsByUserRes getArtsByUser(long artistId, String type, long artId, int size) throws BaseException {
        try {
            List<ArtInfo> artList;
            GetArtsByUserRes result;

            // 회원 검증 및 ID 추출
            long userId = jwtService.getUserIdx();

            // 작가ID 확인
            int isUser = artDao.checkUser(artistId);
            if (isUser == 0){
                throw new BaseException(BAD_REQUEST);
            }

            if (type.equals("my")) {
                if (userId == artistId){ // MyPage
                    artList = artDao.selectArsByUserId(userId, artistId, true, artId, size);
                    result = new GetArtsByUserRes(artList.size(), artList);

                } else {
                    throw new BaseException(INVALID_USER_JWT);
                }
            } else {    // 그 외 화면
                artList = artDao.selectArsByUserId(userId, artistId, false, artId, size);
                result = new GetArtsByUserRes(artList.size(), artList);
            }

            return result;

        } catch (BaseException e){
            throw new BaseException(e.getStatus());
        } catch (Exception e){
            logger.error("GetArtsByUserId Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    /** 작품 상세 조회 **/
    public GetArtByArtIdRes getArtByArtId(long artId) throws BaseException {
        try {
            // 작품ID 확인
            int isArt = artDao.checkArt(artId);
            if (isArt == 0){
                throw new BaseException(BAD_REQUEST);
            }

            GetArtByArtIdRes result = artDao.selectArtByArtId(artId);
            return result;

        } catch (BaseException e) {
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            logger.error("GetArtByArtId Error", e);
            throw new BaseException(RESPONSE_ERROR);
        }
    }
}
