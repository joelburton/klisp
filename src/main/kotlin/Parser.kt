package com.joelburton.klisp

import java.io.PushbackReader
import java.io.StringReader

private const val END_OF_STREAM = -1

/** A basic recursive descendant parser for LISP. */

class Parser {
    class ParseError(message: String) : Exception(message)

    operator fun invoke(input: String): Ast {
        val reader = PushbackReader(StringReader(input))

        // Recursively parse the string and create the AST
        val ast = parse(reader)

        // A valid Lisp s-exp is one thing, like `(a b c)`; something with more
        // than one part, like `(a b c) (d e f)` isn't a valid s-exp; so if
        // there is anything else other than the head, this isn't an expression
        // and throw an error.
        var rest = peek(reader)
        if (rest == ";") {
            // there's a trailing comment; ignore this...
            while(peek(reader) != "\n" && peek(reader) != "") reader.read()
            rest = peek(reader)
        }
        if (rest.isNotEmpty()) throw ParseError("Unexpected input: $rest")

        return ast
    }

    /** Parse an s-exp. This calls itself recursively on each atom.
     *
     * This is Lisp-specific in treating parentheses as the reason to recurse
     * and build a new AST, but it doesn't know what Lisp is, really; everything
     * is just tossed into the AST except the parentheses.
     *
     */

    private fun parse(reader: PushbackReader): Ast {
        var token = nextToken(reader)

        if (token == ";") {
            while (true) {
                val chr = reader.read()
                if (chr == END_OF_STREAM) return Ast("")
                if (chr == '\n'.code) break
            }
            token = nextToken(reader)
        }

        // When we hit an open-paren, we parse that whole expression through to
        // the ending close-paren. So, having this be called on a close paren
        // would happen with input like `( a ) )`, which is invalid.
        //
        //  Also, the last token should always be a close-paren, so hitting the
        //  end without that is a parsing error, like `( 1 2`

        if (token == ")" || token.isEmpty()) {
            throw ParseError("Parsing error: $token")
        }

        // If we do find an open paren, parse everything inside of it
        // recursively, then skip its closing paren
        return if (token == "(") {
            val ast = Ast(token)
            while (peek(reader) != ")") ast.children.add(parse(reader))
            nextToken(reader) // skip )
            ast
        } else {
            // Just an ordinary token, like `1` or `foo`
            Ast(token)
        }
    }

    /** Read next token (open-paren, close-paren, or a word */
    private fun nextToken(reader: PushbackReader): String {
        return when (peek(reader)) {
            "(", ")" -> reader.read().toChar().toString()
            else -> nextAtom(reader)
        }
    }

    /** Read next "atom" (a non-paren token) */
    private fun nextAtom(reader: PushbackReader): String {
        val buffer = StringBuilder()
        while (true) {
            val chr = reader.read()
            if (chr == END_OF_STREAM) break
            val char = chr.toChar()
            if (char.isWhitespace() || char == ')') {
                if (char == ')') reader.unread(')'.code)
                break
            }
            buffer.append(char)
        }
        return buffer.toString()
    }

    /** Peek at the next non-whitespace char */
    private fun peek(reader: PushbackReader): String {
        var chr: Int

        do chr = reader.read()
        while (chr != END_OF_STREAM && chr.toChar().isWhitespace())

        if (chr == END_OF_STREAM) return ""

        reader.unread(chr)
        return chr.toChar().toString()
    }
}