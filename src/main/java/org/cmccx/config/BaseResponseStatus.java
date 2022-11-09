package org.cmccx.config;

import lombok.Getter;

/**
 * 에러 코드 관리
 */
@Getter
public enum BaseResponseStatus {
    /**
     * 1000 : 요청 성공
     */
    SUCCESS(true, 1000, "요청에 성공하였습니다."),
    DELETE_SUCCESS(true, 1003, "삭제되었습니다."),


    /**
     * 2000 : Request 오류
     */
    // Common
    VALIDATION_ERROR(false, 2000, ""),
    BAD_REQUEST(false, 2010, "잘못된 요청입니다."),

    EMPTY_JWT(false, 2001, "JWT를 입력해주세요."),
    INVALID_JWT(false, 2002, "유효하지 않은 JWT입니다."),
    INVALID_USER_JWT(false,2003,"권한이 없는 유저의 접근입니다."),

    INVALID_ACCESS_TOKEN(false, 2004, "유효하지 않은 Access Token입니다."),

    INVALID_SIGNUP_USER(false, 2005, ""),
    BLOCKED_SIGNUP(false, 2006, "신고 3회 누적으로 영구 차단된 계정입니다."),
    BLOCKED_LOGIN(false, 2007, ""),

    // users
    USERS_EMPTY_USER_ID(false, 2010, "유저 아이디 값을 확인해주세요."),

    // [POST] /users
    POST_USERS_EMPTY_EMAIL(false, 2015, "이메일을 입력해주세요."),
    POST_USERS_INVALID_EMAIL(false, 2016, "이메일 형식을 확인해주세요."),
    POST_USERS_EXISTS_EMAIL(false,2017,"중복된 이메일입니다."),

    DUPLICATED_NICKNAME(false, 2050, "중복된 닉네임입니다."),

    // mieczy
    DUPLICATED_ARTIST_TITLE(false, 2501, "이미 등록된 작품 제목입니다."),
    INVALID_IMAGE_FILE(false, 2502, "이미지 파일만 업로드 가능합니다."),
    EXCEEDED_FILE_SIZE(false, 2503, "업로드 가능한 최대 용량은 50MB입니다."),
    INVALID_IMAGE_WIDTH_HEIGHT(false, 2504, "이미지 크기를 확인할 수 없습니다."),
    EXCLUSIVE_SALE_ART(false, 2505, "독점 판매된 작품은 삭제할 수 없습니다."),
    EXCEEDED_ART_QUANTITY(false, 2506, "판매 수량보다 적은 값은 입력할 수 없습니다."),
    DUPLICATED_GALLERY_NAME(false, 2507, "이미 등록된 갤러리명입니다."),


    /**
     * 3000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),

    DUPLICATED_EMAIL(false, 3013, "중복된 이메일입니다."),
    FAILED_TO_LOGIN(false,3014,"없는 아이디거나 비밀번호가 틀렸습니다."),

    FAILED_ACCESS_ART(false, 3501, "차단 또는 삭제된 작품입니다."),
    FAILED_ACCESS_USER(false, 3502, "차단 또는 탈퇴한 회원입니다."),
    FAILED_ACCESS_ARTICLE(false, 3503, "차단 또는 삭제된 아티클 컴필레이션입니다."),




    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR(false, 4000, "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR(false, 4001, "알 수 없는 오류가 발생하였습니다."),

    S3_UPLOAD_ERROR(false, 4005, "S3 파일 업로드에 실패하였습니다."),
    S3_UPDATE_ERROR(false, 4006, "S3 파일 업데이트에 실패하였습니다."),
    S3_DELETE_ERROR(false, 4007, "S3 파일 삭제에 실패하였습니다."),


    PASSWORD_ENCRYPTION_ERROR(false, 4011, "비밀번호 암호화에 실패하였습니다."),
    PASSWORD_DECRYPTION_ERROR(false, 4012, "비밀번호 복호화에 실패하였습니다.");


    // 5000 : 필요시 만들어서 쓰세요
    // 6000 : 필요시 만들어서 쓰세요


    private final boolean isSuccess;
    private final int code;
    private final String message;

    private BaseResponseStatus(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}
