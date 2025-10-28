package com.resetrix.horaion.shared.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Custom DTO for paginated responses that provides a stable JSON structure.
 * This is an alternative to using Spring Data's PagedModel.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PagedResponse<T>(
        List<T> content,
        PageInfo page
) {

    /**
     * Creates a PagedResponse from a Spring Data Page object.
     */
    public static <T> PagedResponse<T> of(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                new PageInfo(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.isFirst(),
                        page.isLast(),
                        page.hasNext(),
                        page.hasPrevious()
                )
        );
    }

    /**
     * Pagination metadata.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PageInfo(
            int number,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            boolean hasNext,
            boolean hasPrevious
    ) {
    }
}
