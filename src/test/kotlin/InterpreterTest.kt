package com.joelburton.klisp

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class InterpreterTest {
    lateinit var interp: Interpreter
    val parser = Parser()

    @BeforeEach
    fun beforeEach() {
        // make sure any defines don't affect following tests
        interp = Interpreter()
    }

    fun p(s: String) = interp.eval(parser(s)).toString()
    
    @Test
    fun `works for atomic`() {
        assertEquals("1.0", p("1"))
    }

    @Test
    fun `works for built-in functions`() {
        assertEquals("3.0", p("(+ 1 2)"))
    }

    @Test
    fun `works for lambdas`() {
        assertEquals("4.0", p("( (lambda (x) (+ x 2)) 2 )"))
    }

    @Test
    fun `works for if`() {
        assertEquals("2.0", p("( if (= 1 1) 2 3 )"))
    }

    @Test
    fun `works for else`() {
        assertEquals("3.0", p("( if (= 1 2) 2 3 )"))
    }

    @Test
    fun `works for define`() {
        p("( define foo 2 )")
        assertEquals("2.0", p("foo"))
    }
}