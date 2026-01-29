package com.kratosgado.blog.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PageRequest {
  @Builder.Default
  protected int page = 0;
  @Builder.Default
  protected int size = 10;
  @Builder.Default
  protected String sortBy = "id";
  @Builder.Default
  protected String sortDir = "desc";

  public int getOffset() {
    return page * size;
  }
}
