package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFFE2E8F0)
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val parts = parseMarkdown(markdown)
        for (part in parts) {
            when (part) {
                is MarkdownPart.CodeBlock -> {
                    CodeBlockView(
                        code = part.code,
                        language = part.language,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                is MarkdownPart.Header -> {
                    Text(
                        text = part.text,
                        color = CyberCyan,
                        fontSize = if (part.level == 1) 18.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                is MarkdownPart.Paragraph -> {
                    val annotatedString = buildAnnotatedString {
                        val text = part.text
                        var i = 0
                        while (i < text.length) {
                            if (text.startsWith("**", i)) {
                                val end = text.indexOf("**", i + 2)
                                if (end != -1) {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                                        append(text.substring(i + 2, end))
                                    }
                                    i = end + 2
                                    continue
                                }
                            } else if (text.startsWith("`", i) && !text.startsWith("```", i)) {
                                val end = text.indexOf("`", i + 1)
                                if (end != -1) {
                                    withStyle(
                                        SpanStyle(
                                            fontFamily = FontFamily.Monospace,
                                            background = Color(0x3300E5FF),
                                            color = CyberCyan
                                        )
                                    ) {
                                        append(" " + text.substring(i + 1, end) + " ")
                                    }
                                    i = end + 1
                                    continue
                                }
                            }
                            append(text[i])
                            i++
                        }
                    }
                    Text(
                        text = annotatedString,
                        color = textColor,
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

sealed class MarkdownPart {
    data class Paragraph(val text: String) : MarkdownPart()
    data class Header(val text: String, val level: Int) : MarkdownPart()
    data class CodeBlock(val code: String, val language: String) : MarkdownPart()
}

fun parseMarkdown(text: String): List<MarkdownPart> {
    val results = mutableListOf<MarkdownPart>()
    val codeBlockRegex = Regex("```([a-zA-Z0-9_-]*)\\n?([\\s\\S]*?)```")

    var lastIndex = 0
    val matches = codeBlockRegex.findAll(text)

    for (match in matches) {
        val nonCodeRange = text.substring(lastIndex, match.range.first)
        if (nonCodeRange.isNotBlank()) {
            parseTextParagraphs(nonCodeRange, results)
        }

        val lang = match.groupValues[1].ifBlank { "code" }
        val code = match.groupValues[2].trimEnd()
        results.add(MarkdownPart.CodeBlock(code = code, language = lang))

        lastIndex = match.range.last + 1
    }

    if (lastIndex < text.length) {
        val remaining = text.substring(lastIndex)
        if (remaining.isNotBlank()) {
            parseTextParagraphs(remaining, results)
        }
    }

    return results
}

private fun parseTextParagraphs(text: String, out: MutableList<MarkdownPart>) {
    val lines = text.split("\n")
    val currentParagraph = StringBuilder()

    for (line in lines) {
        val trimmed = line.trim()
        when {
            trimmed.startsWith("### ") -> {
                if (currentParagraph.isNotEmpty()) {
                    out.add(MarkdownPart.Paragraph(currentParagraph.toString().trim()))
                    currentParagraph.clear()
                }
                out.add(MarkdownPart.Header(trimmed.removePrefix("### ").trim(), 3))
            }
            trimmed.startsWith("## ") -> {
                if (currentParagraph.isNotEmpty()) {
                    out.add(MarkdownPart.Paragraph(currentParagraph.toString().trim()))
                    currentParagraph.clear()
                }
                out.add(MarkdownPart.Header(trimmed.removePrefix("## ").trim(), 2))
            }
            trimmed.startsWith("# ") -> {
                if (currentParagraph.isNotEmpty()) {
                    out.add(MarkdownPart.Paragraph(currentParagraph.toString().trim()))
                    currentParagraph.clear()
                }
                out.add(MarkdownPart.Header(trimmed.removePrefix("# ").trim(), 1))
            }
            trimmed.isEmpty() -> {
                if (currentParagraph.isNotEmpty()) {
                    out.add(MarkdownPart.Paragraph(currentParagraph.toString().trim()))
                    currentParagraph.clear()
                }
            }
            else -> {
                if (currentParagraph.isNotEmpty()) currentParagraph.append("\n")
                currentParagraph.append(trimmed)
            }
        }
    }

    if (currentParagraph.isNotEmpty()) {
        out.add(MarkdownPart.Paragraph(currentParagraph.toString().trim()))
    }
}
