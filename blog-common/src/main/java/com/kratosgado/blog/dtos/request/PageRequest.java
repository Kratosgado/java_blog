package com.kratosgado.blog.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PageRequest {
    protected int page = 0;
    protected int size = 10;
    protected String sortBy = "id";
    protected String sortDir = "desc";

    public int getOffset() {
        return page * size;
    }
}