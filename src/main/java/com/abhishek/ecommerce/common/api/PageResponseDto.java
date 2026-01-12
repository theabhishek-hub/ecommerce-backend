package com.abhishek.ecommerce.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDto<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;

    public static <T> PageResponseDtoBuilder<T> builder() {
        return new PageResponseDtoBuilder<>();
    }

    public static class PageResponseDtoBuilder<T> {
        private List<T> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean empty;

        public PageResponseDtoBuilder<T> content(List<T> content) {
            this.content = content;
            return this;
        }

        public PageResponseDtoBuilder<T> pageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public PageResponseDtoBuilder<T> pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PageResponseDtoBuilder<T> totalElements(long totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public PageResponseDtoBuilder<T> totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public PageResponseDtoBuilder<T> first(boolean first) {
            this.first = first;
            return this;
        }

        public PageResponseDtoBuilder<T> last(boolean last) {
            this.last = last;
            return this;
        }

        public PageResponseDtoBuilder<T> empty(boolean empty) {
            this.empty = empty;
            return this;
        }

        public PageResponseDto<T> build() {
            PageResponseDto<T> dto = new PageResponseDto<>();
            dto.content = this.content;
            dto.pageNumber = this.pageNumber;
            dto.pageSize = this.pageSize;
            dto.totalElements = this.totalElements;
            dto.totalPages = this.totalPages;
            dto.first = this.first;
            dto.last = this.last;
            dto.empty = this.empty;
            return dto;
        }
    }
}