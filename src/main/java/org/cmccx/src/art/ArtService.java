package org.cmccx.src.art;

import org.cmccx.config.BaseException;
import org.cmccx.src.art.model.PostArtReq;
import org.cmccx.src.art.model.PutArtReq;
import org.cmccx.utils.FileService;
import org.cmccx.utils.JwtService;
import org.cmccx.utils.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.cmccx.config.BaseResponseStatus.*;

@Service
public class ArtService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ArtProvider artProvider;
    private final ArtDao artDao;
    private final JwtService jwtService;
    private final S3Service s3Service;
    private final FileService fileService;

    @Autowired
    public ArtService(ArtProvider artProvider, ArtDao artDao, JwtService jwtService, S3Service s3Service, FileService fileService) {
        this.artProvider = artProvider;
        this.artDao = artDao;
        this.jwtService = jwtService;
        this.s3Service = s3Service;
        this.fileService = fileService;
    }

    /** 작품 등록 **/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public long registerArt(PostArtReq postArtReq, MultipartFile image) throws BaseException {
        String artImageUrl = null;
        try {
            // 회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

            // 작품명 중복 검사
            int isDuplicated = artProvider.checkArtTitle(userId, postArtReq.getTitle(), 0);
            if (isDuplicated == 1) {
                throw new BaseException(DUPLICATED_ARTIST_TITLE);
            }

            // 이미지 확장자 검증
            boolean isValidFile = fileService.validateFile(image.getInputStream());
            if (!isValidFile) {
                throw new BaseException(INVALID_IMAGE_FILE);
            }

            // 이미지 워터마크 합성

            // 이미지 가로, 세로 크기 저장
            BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            if (width <= 0 || height <= 0) {
                throw new BaseException(INVALID_IMAGE_WIDTH_HEIGHT);
            }
            postArtReq.setImageWidth(width);
            postArtReq.setImageHeight(height);

            // 이미지 저장
            artImageUrl = s3Service.uploadImage(image);
            postArtReq.setArtImage(artImageUrl);

            // 작품 정보 저장
            long artId = artDao.insertArt(userId, postArtReq);

            // 파일 유형 저장
            int result = artDao.insertFiletype(artId, postArtReq.getFiletypeId());
            if (result < postArtReq.getFiletype().size()) {
                throw new BaseException(DATABASE_ERROR);
            }

            // 허용 범위 저장
            result = artDao.insertCopyright(artId, postArtReq.getCopyrightId());
            if (result < postArtReq.getCopyright().size()) {
                throw new BaseException(DATABASE_ERROR);
            }

            // 태그 저장
            List<String> tags = postArtReq.getTags();
            if (tags != null && !tags.isEmpty()) {
                List<Long> tagId = artDao.insertTag(tags);

                // 작품 해시태그 저장
                result = artDao.insertArtTag(artId, tagId);
                if (result < tags.size()) {
                    throw new BaseException(DATABASE_ERROR);
                }
            }

            return artId;

        } catch (BaseException e) {
            s3Service.deleteImage(artImageUrl);
            throw new BaseException(e.getStatus());
        } catch (Exception e) {
            s3Service.deleteImage(artImageUrl);
            logger.error("RegisterArt Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 작품 수정 **/
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public long modifyArt(long artId, PutArtReq putArtReq, MultipartFile newImage) throws BaseException {
        String artImageUrl = null;
        try {
            // 회원 검증 및 ID 추출
            long userId = jwtService.getUserId();

            // 작가-작품 관계 확인
            int isValid = artProvider.checkUserArt(userId, artId);
            if (isValid == 0){
                throw new BaseException(INVALID_USER_JWT);
            }

            // 작품명 중복 검사
            int isDuplicated = artProvider.checkArtTitle(userId, putArtReq.getTitle(), artId);
            if (isDuplicated == 1){
                throw new BaseException(DUPLICATED_ARTIST_TITLE);
            }

            // 이미지 수정할 경우
            if (newImage != null){
                // 이미지 확장자 검증
                boolean isValidFile = fileService.validateFile(newImage.getInputStream());
                if (!isValidFile){
                    throw new BaseException(INVALID_IMAGE_FILE);
                }

                // 이미지 워터마크 합성

                // 이미지 가로, 세로 크기 저장
                BufferedImage bufferedImage = ImageIO.read(newImage.getInputStream());
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();
                if (width <= 0 || height <= 0) {
                    throw new BaseException(INVALID_IMAGE_WIDTH_HEIGHT);
                }
                putArtReq.setImageWidth(width);
                putArtReq.setImageHeight(height);

                // 이미지 업데이트
                artImageUrl = s3Service.uploadImage(newImage);
                putArtReq.setNewArtImage(artImageUrl);
            }

            // 작품 정보 저장
            int result = artDao.updateArt(artId, putArtReq);
            if (result == 0){
                throw new BaseException(DATABASE_ERROR);
            }

            // 파일 유형 저장
            artDao.deleteFiletype(artId);
            result = artDao.insertFiletype(artId, putArtReq.getFiletypeId());
            if (result < putArtReq.getFiletype().size()){
                throw new BaseException(DATABASE_ERROR);
            }

            // 허용 범위 저장
            artDao.deleteCopyright(artId);
            result = artDao.insertCopyright(artId, putArtReq.getCopyrightId());
            if (result < putArtReq.getCopyright().size()){
                throw new BaseException(DATABASE_ERROR);
            }

            // 태그 저장
            artDao.deleteArtTag(artId);
            List<String> tags = putArtReq.getTags();
            if (tags != null && !tags.isEmpty()){
                List<Long> tagId = artDao.insertTag(tags);

                // 작품 해시태그 저장
                result = artDao.insertArtTag(artId, tagId);
                if (result < tags.size()) {
                    throw new BaseException(DATABASE_ERROR);
                }
            }

            // 기존 이미지 삭제
            s3Service.deleteImage(putArtReq.getOriginArtImage());

            return artId;

        } catch (BaseException e){
            s3Service.deleteImage(artImageUrl);
            throw new BaseException(e.getStatus());
        } catch (Exception e){
            s3Service.deleteImage(artImageUrl);
            logger.error("ModifyArt Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /** 작품 삭제 **/
    public String removeArt(long artId) throws BaseException {
        try {
            // 회원ID 추출
            long userId = jwtService.getUserId();

            int result = artDao.deleteArt(userId, artId);
            if (result == 0){
                throw new BaseException(BAD_REQUEST);
            }
            String message = "삭제되었습니다.";
            return message;

        } catch (BaseException e){
            throw new BaseException(e.getStatus());
        } catch (Exception e){
            logger.error("DeleteArt Error", e);
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
