package org.cmccx.config;

import java.util.*;

import static org.cmccx.config.BaseResponseStatus.BAD_REQUEST;

public class Constant {

    public static final int USER = 0;
    public static final int ART = 1;

    // 카테고리
    public static final Map<String, Integer> CATEGORY;
    static {
        Map<String, Integer> temp = new HashMap<>();
        temp.put("전체", 0);
        temp.put("캐릭터", 1);
        temp.put("풍경화", 2);
        temp.put("만화", 3);
        temp.put("인물화", 4);
        temp.put("기타", 5);
        CATEGORY = Collections.unmodifiableMap(temp);
    }

    // 파일 유형
    public static final Map<String, Integer> FILETYPE;
    static {
        Map<String, Integer> temp = new HashMap<>();
        temp.put("JPG", 1);
        temp.put("PNG", 2);
        temp.put("SVG", 3);
        temp.put("Ai", 4);
        temp.put("PSD", 5);
        temp.put("PDF", 6);
        FILETYPE = Collections.unmodifiableMap(temp);
    }

    // 허용 범위
    public static final Map<String, Integer> COPYRIGHT;
    static {
        Map<String, Integer> temp = new HashMap<>();
        temp.put("개인 사용", 1);
        temp.put("상업적 이용", 2);
        temp.put("2차가공 허용", 3);
        temp.put("재배포 허용", 4);
        COPYRIGHT = Collections.unmodifiableMap(temp);
    }

    // 회원 신고 유형
    public static final Map<String, Integer> USER_REPORT;
    static {
        Map<String, Integer> temp = new HashMap<>();
        temp.put("유출/사칭/사기", 100);
        temp.put("욕설/비하", 101);
        temp.put("음란물/불건전만남 및 대화", 102);
        temp.put("구매자신고(미입금)", 103);
        temp.put("판매자신고(미발송)", 104);
        USER_REPORT = Collections.unmodifiableMap(temp);
    }

    // 작품 신고 유형
    public static final Map<String, Integer> ART_REPORT;
    static {
        Map<String, Integer> temp = new HashMap<>();
        temp.put("유출/사칭/사기", 200);
        temp.put("욕설/비하", 201);
        temp.put("음란물", 202);
        temp.put("기타", 203);
        ART_REPORT = Collections.unmodifiableMap(temp);
    }

    public static List<Integer> getFiletypeIdList(List<String> filetype) throws BaseException {
        List<Integer> filetypeId = new ArrayList<>();
        for (String file : filetype){
            Integer id = FILETYPE.get(file);
            if (id == null){
                throw new BaseException(BAD_REQUEST);
            }
            filetypeId.add(id);
        }
        return filetypeId;
    }

    public static List<Integer> getCopyrightIdList(List<String> copyrights) throws BaseException {
        List<Integer> filetypeId = new ArrayList<>();
        for (String copyright : copyrights){
            Integer id = COPYRIGHT.get(copyright);
            if (id == null){
                throw new BaseException(BAD_REQUEST);
            }
            filetypeId.add(id);
        }
        return filetypeId;
    }

    public static int getCategoryId(String category) throws BaseException {
        Integer categoryId = CATEGORY.get(category);
        if (categoryId == null){
            throw new BaseException(BAD_REQUEST);
        }
        return categoryId;
    }

    public static int getReportId(int type, String report) throws BaseException {
        Integer reportId;
        if (type == 0) { // 회원 신고
            reportId = USER_REPORT.get(report);
        } else {    // 작품 신고
            reportId = ART_REPORT.get(report);
        }
        if (reportId == null){
            throw new BaseException(BAD_REQUEST);
        }
        return reportId;
    }
}

