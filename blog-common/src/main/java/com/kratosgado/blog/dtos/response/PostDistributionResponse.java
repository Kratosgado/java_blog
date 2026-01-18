
package com.kratosgado.blog.dtos.response;

public record PostDistributionResponse(
    long published,
    long draft,
    long privateCount) {
}
