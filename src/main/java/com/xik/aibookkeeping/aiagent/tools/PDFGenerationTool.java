package com.xik.aibookkeeping.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.xik.aibookkeeping.aiagent.constant.FileConstant;
import com.xik.aibookkeeping.common.utils.AliOssUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * PDF 文件生成
 */
@Slf4j
public class PDFGenerationTool {

    @Resource
    private AliOssUtil aliOssUtil;

    @Tool(description = "Generate a PDF file with given content")
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {
        String objectName = "pdf/" + fileName;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter writer = new PdfWriter(baos);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                // 使用内置中文字体
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);
                // 创建段落
                Paragraph paragraph = new Paragraph(content);
                // 添加段落并关闭文档
                document.add(paragraph);
            }
            // 上传到OSS
            return aliOssUtil.upload(baos.toByteArray(), objectName);
        } catch (IOException e) {
            log.error("Error generating PDF", e);
            return "Error generating PDF: " + e.getMessage();
        }
    }
}
