package com.joelburton.klisp

import org.jline.reader.*
import org.jline.reader.LineReader.Option
import org.jline.reader.impl.DefaultParser
import org.jline.reader.impl.DefaultParser.Bracket
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle
import org.jline.utils.AttributedStyle.*
import org.jline.widget.AutosuggestionWidgets


val parser = Parser()
val interp = Interpreter()
val terminal: Terminal = TerminalBuilder.builder().dumb(true).build()


/** Print to either interactive console or dumb terminal/stdout */
fun kPrint(s: String, style: AttributedStyle = DEFAULT) {
    if (terminal.type == "dumb") {
        print(s)
    } else {
        val out = AttributedStringBuilder()
            .style(style)
            .append(s)
            .style(DEFAULT)
            .toAnsi(terminal)
        terminal.writer().print(out)
    }
}

/** Print line to either interactive console or dumb terminal/stdout */
fun kPrintln(s: String, style: AttributedStyle = DEFAULT) =
    kPrint("$s\n", style)

/** Set up JLine reader:
 *
 * - make a prompt string
 * - make a prompt2 continuation string
 * - setup line reader w/completions, history, autosuggestions, bracket-closing
 *
 * Returns (prompt, reader)
 */

private fun setupLineReader(): Pair<String, LineReader> {
    val parser = DefaultParser()
    parser.setEofOnUnclosedBracket(Bracket.ROUND)
    parser.lineCommentDelims(arrayOf(";"))

    val prompt = AttributedStringBuilder()
        .style(DEFAULT.foreground(GREEN))
        .append("klisp> ")
        .style(DEFAULT)
        .toAnsi(terminal)

    val prompt2 = AttributedStringBuilder()
        .style(DEFAULT.foreground(GREEN))
        .append("...... ")
        .style(DEFAULT)
        .toAnsi(terminal)

    val reader = LineReaderBuilder
        .builder()
        .parser(parser)
        .completer(LispCompleter(interp))
        .variable(LineReader.SECONDARY_PROMPT_PATTERN, prompt2)
        .variable(LineReader.INDENTATION, 2)
        .option(Option.INSERT_BRACKET, true)
        .terminal(terminal)
        .highlighter(LispHighter())
        .build()

    val autosuggestionWidgets = AutosuggestionWidgets(reader)
    autosuggestionWidgets.enable()

    return Pair(prompt, reader)
}


fun main() {
    if (terminal.type == "dumb") noninteractive()
    else interactive()
}

fun noninteractive() {
    while (true) {
        try {
            val line = readLine() ?: break
            if (line.trim().isEmpty()) continue
            val result = interp.eval(parser(line))
            if (result is Unit) continue
            println("=> $result")
        } catch (e: Exception) {
            println("ERROR ${e.message ?: "Unknown error"}")
        }
    }
}

/** REPL loop. */

fun interactive() {
    kPrintln("Welcome to Klisp!\n", style = DEFAULT.foreground(GREEN))
    kPrint("Special forms: ", style = DEFAULT.foreground(YELLOW))
    kPrintln(interp.specialForms.keys.joinToString(" "))
    kPrint("Words: ", style = DEFAULT.foreground(YELLOW))
    kPrintln("${interp.environ.keys.joinToString(" ")}\n")

    val (prompt, reader) = setupLineReader()

    while (true) {
        try {
            val line = reader.readLine(prompt)
            if (line.trim().isEmpty()) continue
            val result = interp.eval(parser(line))
            if (result is Unit) continue
            kPrintln("⇒ $result", style = DEFAULT.italic())
        } catch (_: EndOfFileException) {
            return
        } catch (e: Exception) {
            kPrintln(
                "ERROR: ${e.message ?: e}", style = DEFAULT.foreground(RED)
            )
        }
    }
}


@Suppress("unused")
const val example = """
(+ 1 2)

(define pi 3.14159)
pi

(define nested
  (lambda (a b)
    (lambda (c)
      (* (+ a b) c))))

((nested 3 4) 5)

(define factorial
  (lambda (n)
    (if (= n 1)
      1
      (* n (factorial (- n 1))))))

(factorial 5)    
"""
