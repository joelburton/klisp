package com.joelburton.klisp

import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.Highlighter
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle.*
import java.util.regex.Pattern

/** Highlighter for Lisp, compatible with JLine interface. */

internal class LispHighter : Highlighter {
    val paren = "(\\(|\\))"
    val float = "([-+]?[0-9]*\\.?[0-9]+)"
    val bool = "(true|false)"
    val comment = "(;.*)"
    val symbol = "([^\\s()]+)"
    val ws = "(\\s+)"
    val reAll = "$paren|$float|$bool|$ws|$comment|$symbol".toRegex()

    override fun highlight(
        reader: LineReader, buffer: String
    ): AttributedString {
        val sb = AttributedStringBuilder()
        for (m in reAll.findAll(buffer)) {
            if (m.groups[1] != null)
                sb.append(m.groups[1]!!.value, DEFAULT.foreground(MAGENTA))
            else if (m.groups[2] != null)
                sb.append(m.groups[2]!!.value, DEFAULT.foreground(BLUE))
            else if (m.groups[3] != null)
                sb.append(m.groups[3]!!.value, DEFAULT.foreground(BLUE))
            else if (m.groups[4] != null)
                sb.append(m.groups[4]!!.value, DEFAULT)
            else if (m.groups[5] != null)
                sb.append(m.groups[5]!!.value, DEFAULT.foreground(BLACK))
            else if (m.groups[6] != null) {
                val v = m.groups[6]!!.value
                if (v in interp.environ || v in interp.specialForms) {
                    sb.append(v, DEFAULT.bold())
                } else {
                    sb.append(v, DEFAULT.underline())
                }
            }
        }
        return sb.toAttributedString()
    }

    override fun refresh(reader: LineReader?) {
        super.refresh(reader)
    }

    override fun setErrorPattern(p0: Pattern?) {
        throw Exception("Not yet implemented")
    }

    override fun setErrorIndex(p0: Int) {
        throw Exception("Not yet implemented")
    }
}

/** JLine completer that uses the Lisp environment to complete. */

internal class LispCompleter(val interp: Interpreter) : Completer {
    override fun complete(
        reader: LineReader,
        line: ParsedLine,
        candidates: MutableList<Candidate>
    ) {
        for (fn in interp.specialForms.keys) {
            candidates.add(Candidate(fn))
            candidates.add(Candidate("($fn"))
        }
        for (fn in interp.environ.keys) {
            candidates.add(Candidate(fn))
            candidates.add(Candidate("($fn"))
        }
    }
}
