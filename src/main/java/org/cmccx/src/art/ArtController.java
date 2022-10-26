package org.cmccx.src.art;

import org.cmccx.config.BaseException;
import org.cmccx.config.BaseResponse;
import org.cmccx.src.art.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

import static org.cmccx.config.BaseResponseStatus.VALIDATION_ERROR;

@Validated
@RestController
@RequestMapping("/arts")
public class ArtController {

    private final ArtProvider artProvider;
    private final ArtService artService;

    @Autowired
    public ArtController(ArtProvider artProvider, ArtService artService) {
        this.artProvider = artProvider;
        this.artService = artService;
    }

    /**
     * 메인: 작품 목록 조회 API
     * [GET] /arts?category-id={category-id}&id={id}&date={date}&size={size}
     * @return BaseResponse<GetArtsRes>
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<GetArtsRes> getArts(@RequestParam(value = "category-id", defaultValue = "0") @Min(value = 0, message = "유효하지 않은 카테고리입니다.") @Max(value = 5, message = "유효하지 않은 카테고리입니다.") int categoryId,
                                            @RequestParam(value = "id", defaultValue = "0") long artId,
                                            @RequestParam(value = "date", defaultValue = "") String date,
                                            @RequestParam(value = "size", defaultValue = "10000") int size) throws BaseException {
        if (date.isEmpty()){
            date = LocalDate.now().plusDays(1).toString();
        }

        GetArtsRes result = artProvider.getArts(categoryId, artId, date, size);

        return new BaseResponse(result);
    }

    /**
     * 추천 작품 목록 조회 API
     * [GET] /arts/recommended?art-id={art-id}
     * @return BaseResponse<List<ArtInfo>>
     */
    @ResponseBody
    @GetMapping("/recommended")
    public BaseResponse<List<ArtInfo>> getRecommendedArts(@RequestParam(value = "art-id", defaultValue = "0") long artId) throws BaseException {
        List<ArtInfo> result = artProvider.getRecommendedArts(artId);

        return new BaseResponse<>(result);
    }

    /**
     * 작가별 작품 목록 조회 API
     * [GET] /users/{user-id}/arts?type={type}&art-id={art-id}&size={size}
     * @return BaseResponse<GetArtsByUserRes>
     */
    @ResponseBody
    @GetMapping("/{artist-id}/arts")
    public BaseResponse<GetArtsByUserRes> getArtsByUser(@PathVariable(value = "artist-id") long artistId,
                                                         @RequestParam(value = "type", defaultValue = "artist") String type,
                                                         @RequestParam(value = "art-id", defaultValue = "0") long artId,
                                                         @RequestParam(value = "size", defaultValue = "10000") int size) throws BaseException {
        GetArtsByUserRes result = artProvider.getArtsByUser(artistId, type, artId, size);

        return new BaseResponse<>(result);
    }

    /**
     * 작품 상세정보 조회 API
     * [GET] /arts/{art-id}
     * @return BaseResponse<GetArtByArtIdRes>
     */
    @ResponseBody
    @GetMapping("/{art-id}")
    public BaseResponse<GetArtByArtIdRes> getArtByArtId(@PathVariable("art-id") long artId) throws BaseException {
        GetArtByArtIdRes result = artProvider.getArtByArtId(artId);

        return new BaseResponse<>(result);
    }

    /**
     * 작품 등록 API
     * [POST] /arts
     * @return BaseResponse<Long>
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<Long> registerArt(@RequestPart(value = "artInfo") @Valid PostArtReq postArtReq,
                                          @RequestPart(value = "image") @NotNull(message = "작품 이미지를 업로드하세요.") MultipartFile image) throws BaseException {
        // 독점 구매 추가 금액 유효성 검사
        if (postArtReq.getAdditionalCharge() == -1){
            return new BaseResponse<>(VALIDATION_ERROR, "추가 금액을 100원 이상 설정하세요.");
        }

        long result = artService.registerArt(postArtReq, image);

        return new BaseResponse<>(result);
    }

    /**
     * 작품 수정 API
     * [PUT] /arts/{art-id}
     * @return BaseResponse<Long>
     */
    @ResponseBody
    @PutMapping("/{art-id}")
    public BaseResponse<Long> modifyArt(@PathVariable("art-id") long artId,
                          @RequestPart(value = "artInfo") @Valid PutArtReq putArtReq,
                          @RequestPart(value = "image", required = false) MultipartFile image) throws BaseException {
        // 독점 구매 추가 금액 유효성 검사
        if (putArtReq.getAdditionalCharge() == -1){
            return new BaseResponse<>(VALIDATION_ERROR, "추가 금액을 100원 이상 설정하세요.");
        }

        long result = artService.modifyArt(artId, putArtReq, image);

        return new BaseResponse<>(result);

    }

    /**
     * 작품 삭제 API
     * [DELETE] /arts/{art-id}
     * @return BaseResponse<BaseResponseStatus>
     */
    @ResponseBody
    @DeleteMapping("/{art-id}")
    public BaseResponse<String> removeArt(@PathVariable("art-id") long artId) throws BaseException {
        String result = artService.removeArt(artId);

        return new BaseResponse<>(result);
    }

}
