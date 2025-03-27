package com.joelburton.klisp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AstTest {
    @Test
    fun get() {
        val list = Ast("")
        list.children.add(Ast("1"))
        list.children.add(Ast("2"))
        list.children.add(Ast("3"))
        assertEquals(Ast("1"), list.head)
    }

    @Test
    fun getDouble() {
        val num = Ast("1")
        assertEquals(1.0, num.double)

        val nope = Ast("1foo")
        assertEquals(null, nope.double)
    }

    @Test
    fun getBoolean() {
        val true_ = Ast("true")
        assertEquals(true, true_.boolean)

        val false_ = Ast("false")
        assertEquals(false, false_.boolean)

        val nope = Ast("1")
        assertNull(nope.boolean)
    }

    @Test
    fun isAtom() {
        val atom = Ast("1")
        assertTrue(atom.isAtom)

        val list = Ast("")
        list.children.add(Ast("1"))
        assertFalse(list.isAtom)
    }

    @Test
    fun getHead() {
        val list = Ast("")
        list.children.add(Ast("1"))
        list.children.add(Ast("2"))
        list.children.add(Ast("3"))
        assertEquals(Ast("1"), list.head)
    }

    @Test
    fun getTail() {
        val list = Ast("")
        list.children.add(Ast("1"))
        list.children.add(Ast("2"))
        list.children.add(Ast("3"))
        assertEquals(listOf(Ast("2"), Ast("3")), list.tail)
    }

    @Test
    fun getToken() {
        val tokAst = Ast("1")
        assertEquals("1", tokAst.token)
    }

    @Test
    fun getChildren() {
        val list = Ast("")
        list.children.add(Ast("1"))
        list.children.add(Ast("2"))
        list.children.add(Ast("3"))
        assertEquals(listOf(Ast("1"), Ast("2"), Ast("3")), list.children)
    }

    @Test
    fun dump() {
        val list = Ast("", mutableListOf(Ast("1"), Ast("2")))
        assertEquals("""
''
  '1'
  '2'

        """.trimIndent(), list.dump())

        val nested = Ast("", mutableListOf(
            Ast("1"),
            Ast("2"),
            Ast("", mutableListOf(
                Ast("3a"),
                Ast("3b")
            ))
        ))
        assertEquals("""
''
  '1'
  '2'
  ''
    '3a'
    '3b'

        """.trimIndent(), nested.dump())
    }
}