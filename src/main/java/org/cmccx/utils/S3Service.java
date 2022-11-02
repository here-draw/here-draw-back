package org.cmccx.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class S3Service {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${cloud.aws.s3.bucket}")    // application.yml
    private String bucket;

    private final AmazonS3 amazonS3;

    String profile = System.getProperty("spring.profiles.active");
    private final String directory = "app_" + profile + "/";


    /** 파일 이름 생성 **/
    public String createFileName(String filename){
        return directory + filename + "-" + System.currentTimeMillis();
    }

    /** 파일 업로드 (1개)**/
    public String uploadImage(MultipartFile multipartFile) throws BaseException {
        String fileName = createFileName(multipartFile.getOriginalFilename());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try {
            // 파일 업로드 후 URL 저장
            amazonS3.putObject(bucket, fileName, multipartFile.getInputStream(), objectMetadata);
        } catch (Exception e){
            logger.error("S3 ERROR", e);
            // 이미지 업로드 에러
            throw new BaseException(BaseResponseStatus.S3_UPLOAD_ERROR);
        }

        return amazonS3.getUrl(bucket, fileName).toString();
    }

    /** 파일 업로드 (여러개)**/
    public List<String> uploadImage(List<MultipartFile> multipartFile) throws BaseException {
        List<String> imageUrl = new ArrayList<>();

        for (MultipartFile file : multipartFile) {
            String fileName = createFileName(file.getOriginalFilename());
            // 파일 크기
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            try {
                // 파일 업로드 후 URL 저장
                amazonS3.putObject(bucket, fileName, file.getInputStream(), objectMetadata);
                imageUrl.add(amazonS3.getUrl(bucket, fileName).toString());
            } catch (Exception e) {
                logger.error("S3 ERROR", e);
                throw new BaseException(BaseResponseStatus.S3_UPLOAD_ERROR);
            }
        }
        return imageUrl;
    }

    /** 파일 삭제 **/
    public void deleteImage(String imageUrl) throws BaseException {
        try {
            if (imageUrl != null) {
                if(imageUrl.startsWith("https://heredraw-bucket")) {
                    // 파일명 디코딩
                    String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1, imageUrl.length());
                    String decodeData = URLDecoder.decode(fileName, "UTF-8");
                    // 파일 삭제
                    amazonS3.deleteObject(new DeleteObjectRequest(bucket, directory + decodeData));
                }
            }
        } catch (Exception e) {
            logger.error("S3 ERROR", e);
            throw new BaseException(BaseResponseStatus.S3_DELETE_ERROR);
        }
    }

    /** 파일 수정(삭제 후 등록) **/
    public String updateImage(String originUrl, MultipartFile newFile) throws BaseException {
        try {
            if (originUrl != null) {
                deleteImage(originUrl);
            }
        } catch (Exception e){
            throw new BaseException(BaseResponseStatus.S3_UPDATE_ERROR);
        }

        return uploadImage(newFile);
    }
}
