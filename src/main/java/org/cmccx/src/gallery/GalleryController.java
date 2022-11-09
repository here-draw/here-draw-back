package org.cmccx.src.gallery;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponse;
import org.cmccx.src.art.model.ArtInfo;
import org.cmccx.src.gallery.model.GetGalleriesRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/galleries")
@Validated
public class GalleryController {
    private final GalleryProvider galleryProvider;
    private final GalleryService galleryService;

    @Autowired
    public GalleryController(GalleryProvider galleryProvider, GalleryService galleryService) {
        this.galleryProvider = galleryProvider;
        this.galleryService = galleryService;
    }

    /**
     * 갤러리 목록 조회 API
     * [GET] /galleries
     * @return BaseResponse<List<GetGalleriesRes>>
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetGalleriesRes>> getGalleriesRes() throws BaseException {
        List<GetGalleriesRes> result = galleryProvider.getGalleries();
        return new BaseResponse<>(result);
    }

    /**
     * 갤러리 내 작품 목록 조회 API
     * [GET] /galleries/{gallery-id}
     * @return BaseResponse<List<ArtInfo>>
     */
    @ResponseBody
    @GetMapping("/{gallery-id}")
    public BaseResponse<List<ArtInfo>> getArtsByGalleryId(@PathVariable("gallery-id") long galleryId) throws BaseException {
        List<ArtInfo> result = galleryProvider.getArtsByGalleryId(galleryId);
        return new BaseResponse<>(result);
    }

    /**
     * 갤러리 생성 API
     * [POST] /galleries
     * @return BaseResponse<Long>
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<Long> registerGallery(@RequestBody @Valid Map<String, @NotNull(message = "갤러리명을 입력하세요.") @Size(min = 1, max = 30, message = "갤러리명은 최소 1글자, 최대 30글자입니다.")String> param) throws BaseException {
        long result = galleryService.registerGallery(param.get("name"));
        return new BaseResponse<>(result);
    }

    /**
     * 갤러리명 수정 API
     * [PATCH] /galleries/{gallery-id}
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("{gallery-id}")
    public BaseResponse<String> modifyGalleryName(@PathVariable("gallery-id") long galleryId,
                                                  @RequestBody @Valid Map<String, @NotNull(message = "갤러리명을 입력하세요.") @Size(min = 1, max = 30, message = "갤러리명은 최소 1글자, 최대 30글자입니다.") String> param) throws BaseException {
        String result = galleryService.modifyGalleryName(galleryId ,param.get("name"));
        return new BaseResponse<>(result);
    }

    /**
     * 갤러리 삭제 API
     * [DELETE] /galleries/{gallery-id}
     * @return BaseResponse<String>
     */
    @ResponseBody
    @DeleteMapping("{gallery-id}")
    public BaseResponse<String> deleteGallery(@PathVariable("gallery-id") long galleryId) throws BaseException {
        String result = galleryService.removeGalley(galleryId);
        return new BaseResponse<>(result);
    }

    /**
     * 작품 찜 API
     * [POST] /galleries/{gallery-id}/arts/{art-id}
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PostMapping("/{gallery-id}/arts/{art-id}")
    public BaseResponse<Boolean> likeArt(@PathVariable("gallery-id") long galleryId,
                                         @PathVariable("art-id") long artId) throws BaseException {
        boolean result = galleryService.likeArt(galleryId, artId);
        return new BaseResponse<>(result);
    }
}
