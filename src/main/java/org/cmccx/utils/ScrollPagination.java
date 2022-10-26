package org.cmccx.utils;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ScrollPagination<T> {
    private final List<T> items;    // 현재 스크롤 사이즈 + 1 크기
    private final int count;        // 조회 데이터 개수

    public static <T> ScrollPagination<T> of(List<T> items, int size){
        return new ScrollPagination<>(items, size);
    }

    // 다음 페이지 여부 반환
    public boolean hasNextScroll(){
        return this.items.size() == count + 1;
    }

    // 조회한 데이터 목록 반환
    public List<T> getCurrentScrollItems(){
        if (hasNextScroll()){ // 다음 데이터가 있는 경우, 마지막 데이터 삭제
            return this.items.subList(0, count);
        }
        return this.items;
    }

    // 커서 반환
    public T getNextCursor() {
        return this.items.get(count-1);
    }

}
