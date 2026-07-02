package com.example.pdfref;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReferenceExtractorTest {
    @Test
    void extractsReferencesOnlyFromReferenceSectionUntilNextPeerHeading() {
        String text = """
                1 范围
                《范围外文件》(OUT-001)
                2. 引用文件
                本文件引用：《测试文件A》(ABC-005)
                另见《测试文件B》（DEF-006）
                3 术语和定义
                《后续章节文件》(NEXT-007)
                """;

        List<ReferenceItem> references = ReferenceExtractor.extract(text);

        assertEquals(
                List.of(
                        new ReferenceItem("测试文件A", "ABC-005"),
                        new ReferenceItem("测试文件B", "DEF-006")),
                references);
    }

    @Test
    void recognizesChineseNumberedReferenceHeading() {
        String text = """
                一、引用文件
                《中文编号文件》(CN-005)
                二、其他章节
                《不应提取》(NO-001)
                """;

        List<ReferenceItem> references = ReferenceExtractor.extract(text);

        assertEquals(List.of(new ReferenceItem("中文编号文件", "CN-005")), references);
    }

    @Test
    void recognizesNormativeReferenceHeadingWithoutNumber() {
        String text = """
                前言
                规范性引用文件
                《规范文件》(NORM-005)
                术语
                《其他文件》(OTHER-005)
                """;

        List<ReferenceItem> references = ReferenceExtractor.extract(text);

        assertEquals(List.of(new ReferenceItem("规范文件", "NORM-005")), references);
    }
}
