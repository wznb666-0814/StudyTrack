package com.repea.studytrack.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text

/**
 * 非完整 Markdown 解析器，只针对 AI 常见输出做适配：
 * - 支持行首的列表符号：- / * / + 统一渲染为「•」
 * - 支持粗体：**粗体**
 * 这样就不会在界面上直接出现原始的 *、** 符号。
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null
) {
    val annotated = remember(text, color) {
        parseSimpleMarkdown(text, color)
    }
    Text(
        text = annotated,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = textAlign
    )
}

private fun parseSimpleMarkdown(
    source: String,
    color: Color
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    var i = 0

    fun isLineStart(index: Int): Boolean {
        if (index <= 0) return true
        return source[index - 1] == '\n'
    }

    while (i < source.length) {
        // 无序列表前缀：- / * / +
        if (isLineStart(i) && i + 2 <= source.length &&
            (source.startsWith("- ", i) || source.startsWith("* ", i) || source.startsWith("+ ", i))
        ) {
            builder.append("• ")
            i += 2
            continue
        }

        // 粗体：**text**
        if (i + 4 <= source.length && source.startsWith("**", i)) {
            val end = source.indexOf("**", startIndex = i + 2)
            if (end != -1) {
                val boldText = source.substring(i + 2, end)
                val startInBuilder = builder.length
                builder.append(boldText)
                val endInBuilder = builder.length
                builder.addStyle(
                    SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        color = color
                    ),
                    start = startInBuilder,
                    end = endInBuilder
                )
                i = end + 2
                continue
            }
        }

        builder.append(source[i])
        i++
    }

    return builder.toAnnotatedString()
}

