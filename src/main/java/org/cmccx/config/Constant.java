package org.cmccx.config;

import java.util.*;

public class Constant {
    // 카테고리
    public static final Map<String, Integer> CATEGORY;
    static {
        Map<String, Integer> temp = new HashMap<>();
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

    public static List<Integer> getFiletypeIdList(List<String> filetype) throws Exception {
        List<Integer> filetypeId = new ArrayList<>();
        for (String file : filetype){
            Integer id = Constant.FILETYPE.get(file);
            if (id == null){
                throw new Exception();
            }
            filetypeId.add(id);
        }
        return filetypeId;
    }

    public static List<Integer> getCopyrightIdList(List<String> copyrights) throws Exception {
        List<Integer> filetypeId = new ArrayList<>();
        for (String copyright : copyrights){
            Integer id = Constant.COPYRIGHT.get(copyright);
            if (id == null){
                throw new Exception();
            }
            filetypeId.add(id);
        }
        return filetypeId;
    }

    public static int getCategoryId(String category) throws Exception {
        Integer categoryId = Constant.CATEGORY.get(category);
        if (categoryId == null){
            throw new Exception();
        }
        return categoryId;
    }
}

