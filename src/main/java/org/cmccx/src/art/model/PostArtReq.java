package org.cmccx.src.art.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;
import java.util.List;

@Getter
@Setter
public class PostArtReq {
    @NotBlank(message = "제목을 입력하세요.")
    @Size(min = 1, max = 20, message = "제목은 최소 1글자, 최대 20글자까지 입력 가능합니다.")
    private String title;

    @NotBlank(message = "간단 작품 설명을 입력하세요.")
    @Size(min = 1, max = 50, message = "간단 작품 설명은 최소 1글자, 최대 50글자까지 입력 가능합니다.")
    private String simpleDescription;

    @NotNull(message = "가격을 입력하세요.")
    @Min(value = 100, message = "가격을 100원 이상 설정하세요.")
    private Integer price;

    @NotBlank(message = "독점 구매 여부를 입력하세요.")
    private String exclusive;

    private int additionalCharge;

    @NotNull(message = "수량을 입력하세요.")
    @Min(value = 1, message = "최소 수량은 1개입니다.")
    @Max(value = 9999, message = "최대 수량은 9,999개입니다.")
    private Integer amount;

    @NotNull(message = "허용 범위를 선택하세요.")
    @Size(min = 1,message = "허용 범위를 선택하세요.")
    private List<@Min(value = 1, message = "유효하지 않은 허용범위입니다.") @Max(value = 4, message = "유효하지 않은 허용범위입니다.") Integer> copyright;

    @NotBlank(message = "작품 설명을 입력하세요.")
    @Size(min = 1, max = 1000, message = "작품 설명은 최소 1글자, 최대 1000글자까지 입력 가능합니다.")
    private String description;

    @NotNull(message = "파일 유형을 선택하세요.")
    @Size(min = 1,message = "파일 유형을 선택하세요.")
    private List<@Min(value = 1, message = "유효하지 않은 파일유형입니다.") @Max(value = 6, message = "유효하지 않은 파일유형입니다.") Integer> filetype;

    @Min(value = 0, message = "유효하지 않은 카테고리입니다.")
    @Max(value = 5, message = "유효하지 않은 카테고리입니다.")
    private int categoryId;

    @Size(max = 10, message = "해시태그는 최대 10개까지 등록할 수 있습니다.")
    private List<@Size(min = 1, max = 15, message = "태그명은 최소 1글자, 최대 15글자까지 입력 가능합니다.") String> tags;

    private String artImage;

    public PostArtReq(String title, String simpleDescription, Integer price, String exclusive, Integer additionalCharge, Integer amount, List<Integer> copyright, String description, List<Integer> filetype, int categoryId, List<String> tags) {
        this.title = title;
        this.simpleDescription = simpleDescription;
        this.price = price;
        this.exclusive = isExclusive(exclusive);
        this.additionalCharge = getAdditional(exclusive, additionalCharge);
        this.amount = amount;
        this.copyright = copyright;
        this.description = description;
        this.filetype = filetype;
        this.categoryId = categoryId;
        this.tags = tags;
    }

    private String isExclusive(String flag){
        if (flag.equals("Y")){
            return "Y";
        }
        return "N";
    }

    private int getAdditional(String flag, int additionalCharge) {
        if (flag.equals("Y")){
            if (additionalCharge < 100){
                return -1;
            }
            return additionalCharge;
        }
        return 0;
    }
}
