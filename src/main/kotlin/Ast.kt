package com.joelburton.klisp

/** An AST for List.
 *
 * Each node has a string value and a "children" AST.
 *
 * There are convenience functions to get the number (double) value for a
 * numeric string, and the boolean value of boolean strings ("true" or "false").
 *
 */

data class Ast(
    val token: String, val children: MutableList<Ast> = mutableListOf()
) {
    override fun toString() = if (isAtom) token else children.toString()
    operator fun get(index: Int) = children[index]

    /** Get token-as-double. Returns null when called on a non-number token. */
    val double get() = token.toDoubleOrNull()

    /** Get token-as-bool. Returns null on a non-boolean token. */
    val boolean get() = token.toBooleanStrictOrNull()

    /** An "atom" is an AST of a single value, like `1` or `foo` */
    val isAtom get() = children.isEmpty()

    /** The Lisp CAR idea: the "head" is the first child. */
    val head get() = children[0]

    /** The Lisp CDR idea: the "tail" is a list of all subsequent children. */
    val tail get() = children.subList(1, children.size)

    /** For debugging/learning: returns level-indented representation of AST
     *
     * Recursively collections children and returns final string.
     * */
    fun dump(indent: Int = 0): String =
        buildString {
            appendLine("${" ".repeat(indent)}'${token}'")
            children.forEach { c -> append(c.dump(indent + 2)) }
        }
}