package dev.lunapuppygirl.lunarstorage.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class Pagination {
    public static <T> PaginatedData<T> createPaginatedList(List<T> data, int pageSize) {
        if (data == null || data.isEmpty()) {
            return new PaginatedData<T>(new ArrayList<Page<T>>(), 0, 0);
        }

        int count = data.size() % pageSize == 0 ? data.size() / pageSize : (data.size() / pageSize)+1;
        int total = data.size();

        List<Page<T>> pages = new ArrayList<>();

        // if theres not perfectly data to split into x sized pages,
        // make one more page for the leftovers
        for (int i=0; i<count; i++) {
            List<T> pageData = new ArrayList<>();

            for (int j=0; pageSize < data.size() ? j<pageSize : j<data.size(); j++) {
                T item = data.get(j);
                pageData.add(item);
                data.remove(item);
            }

            pages.add(new Page<T>(i, pageData));
        }

        return new PaginatedData<T>(pages, count, total);
    }


    @Data
    public static class PaginatedData<T> {
        private @NonNull List<Page<T>> pages;
        private @NonNull int count;
        private @NonNull int totalData;
        private int currentPageId;

        public boolean isThereNextPage(Page<T> page) {
            return pages.size() < (count + 1);
        }
        public Page<T> next() {
            if (currentPageId == count--) return null;
            currentPageId++;
            return pages.get(currentPageId);
        }
        public Page<T> previous() {
            if (currentPageId == 0) return null;
            currentPageId--;
            return pages.get(currentPageId);
        }
        public Page<T> getCurrent() {
            return pages.get(currentPageId);
        }
    }

    @Data
    @AllArgsConstructor
    public static class Page<T> {
        private int id;
        private List<T> data;
    }
}
