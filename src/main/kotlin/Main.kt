package com.joelburton.klisp

import org.jline.reader.*
import org.jline.reader.LineReader.Option
import org.jline.reader.impl.DefaultParser
import org.jline.reader.impl.DefaultParser.Bracket
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle
import org.jline.widget.AutosuggestionWidgets


val parser = Parser()
val interp = Interpreter()


/** JLine completer that uses the Lisp environment to complete. */
private class LispCompleter(val interp: Interpreter) : Completer {
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

/** Print err msg to console in red. */
fun printErr(msg: String) {
    val out = AttributedStringBuilder()
        .style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.RED))
        .append("ERROR: $msg")
        .style(AttributedStyle.DEFAULT)
        .toAnsi()
    println(out)
}

/** Print evaluation result in bold. */
private fun printResult(result: String?) {
    val out = AttributedStringBuilder()
        .style(AttributedStyle.DEFAULT.bold())
        .append(result)
        .style(AttributedStyle.DEFAULT)
    println(out.toAnsi())
}

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
    val prompt = AttributedStringBuilder()
        .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
        .append("klisp> ")
        .style(AttributedStyle.DEFAULT)
        .toAnsi()
    val prompt2 = AttributedStringBuilder()
        .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
        .append("...... ")
        .style(AttributedStyle.DEFAULT)
        .toAnsi()
    val reader = LineReaderBuilder
        .builder()
        .parser(parser)
        .completer(LispCompleter(interp))
        .variable(LineReader.SECONDARY_PROMPT_PATTERN, prompt2)
        .variable(LineReader.INDENTATION, 2)
        .option(Option.INSERT_BRACKET, true)
        .build()
    val autosuggestionWidgets = AutosuggestionWidgets(reader)
    autosuggestionWidgets.enable()
    return Pair(prompt, reader)
}

/** Display welcome banner. */
private fun displayBanner() {
    val banner = AttributedStringBuilder()
        .style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.GREEN))
        .append("\nKLisp!\n")
        .style(AttributedStyle.DEFAULT)
        .toAnsi()
    println(banner)
}

/** REPL loop. */

fun main() {
    displayBanner()
    println("Special forms: ${interp.specialForms.keys.joinToString(" ")}")
    println("Words: ${interp.environ.keys.joinToString(" ")}\n")

    val (prompt, reader) = setupLineReader()

    while (true) {
        try {
            val line = reader.readLine(prompt)
            val result = interp.eval(parser(line))
            printResult(result)
        } catch (_: EndOfFileException) {
            return
        } catch (e: Exception) {
            printErr(e.message ?: "Unknown error")
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
