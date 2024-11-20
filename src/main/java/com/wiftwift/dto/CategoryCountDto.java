package com.wiftwift.dto;

import com.wiftwift.entity.AstartesCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryCountDto {
    private AstartesCategory category;
    private Long count;

    public CategoryCountDto(AstartesCategory category, Long count) {
        this.category = category;
        this.count = count;
    }

}
