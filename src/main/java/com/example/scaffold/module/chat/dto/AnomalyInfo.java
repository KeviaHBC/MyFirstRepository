package com.example.scaffold.module.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyInfo {

    /** 异常类型，如"价格异常" */
    private String type;

    /** 涉及字段，如"price" */
    private String field;

    /** 实际值 */
    private String actualValue;

    /** 期望值 */
    private String expectedValue;

    /** 严重程度: 严重 | 警告 | 提示 */
    private String severity;

    /** 关联记录 ID */
    private Long recordId;

    /** 关联记录名称 */
    private String recordName;

    /** 修复建议 */
    private String suggestion;
}
