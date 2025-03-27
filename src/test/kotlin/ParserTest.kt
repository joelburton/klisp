package com.joelburton.klisp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class ParserTest {
    @Test
    fun succeeds() {
        val parser = Parser()
        val sexpr = parser("(+ 1 2)")
        assertEquals(
            sexpr,
            Ast("(", mutableListOf(Ast("+"), Ast("1"), Ast("2"))),
        )
    }

    @Test
    fun `fails on empty string`() {
        val parser = Parser()
        assertFailsWith<Parser.ParseError> { parser("") }
    }

    @Test
    fun `fails on unclosed open-paren`() {
        val parser = Parser()
        assertFailsWith<Parser.ParseError> { parser("( 1 2") }
    }

    @Test
    fun `fails on closing non-open`() {
        val parser = Parser()
        assertFailsWith<Parser.ParseError> { parser("( 1 2 ) )") }
    }
}