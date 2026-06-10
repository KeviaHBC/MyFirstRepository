package com.example.scaffold.module.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /** AI 文本回复 */
    private String reply;

    /** 异常列表（可为空） */
    private List<AnomalyInfo> anomalies;

    /** 是否显示图片占位区 */
    private boolean imagePlaceholder;
}
