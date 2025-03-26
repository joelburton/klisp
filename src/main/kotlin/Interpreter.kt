package com.joelburton.klisp

import com.joelburton.klisp.Interpreter.Environ

typealias LispFunc = (a: List<Any>) -> Any?
typealias SpecialForm = (env: Environ, a: List<AST>) -> Any?

/** The interpreter for the Lisp system.
 *
 * This has the definitions of the built-in words ('+', '-', etc.), as well
 * as the "special words", and core evaluation functions for evaluating an
 * expression.
 */

class Interpreter(private val environ: Environ = Environ(null)) {

    /** The environment is the list of definitions available to a function.
     *
     * The are a bunch of standard things (like `+`, `-`, and other built-in
     * definitions) that will be in here, but for a lambda, this will also have
     * it's variables available. And, of course, this is recursive.
     *
     * Each environment points to its parent; the top has a null parent.
     */
    class Environ(val parent: Environ? = null) : HashMap<String, Any?>() {
        /** Look up a name: start here, then recursively up through parents. */
        override operator fun get(key: String): Any? =
            super[key] ?: parent?.get(key)

        internal fun addFunc(name: String, func: LispFunc) = put(name, func)
    }

    /** Special forms are not evaluated; the author of them must evaluate them.
     *
     * This is what allows short-circuiting in logical expressions and is
     * what lambda and definitions use.
     */
    private val specialForms = mutableMapOf<String, SpecialForm>(
        "and" to { env, a -> a.all { eval(env, it) as Boolean } as Any },
        "or" to { env, a -> a.any { eval(env, it) as Boolean } as Any },
        "if" to { env, a ->
            if (eval(env, a[0]) as Boolean) eval(env, a[1])
            else if (a.size > 2) eval(env, a[2]) else null
        },
        "lambda" to ::createLambda,
        "define" to { env, a -> env[a[0].token] = eval(env, a[1]) },
    )

    /** Add these words to the top-level environment. */
    init {
        with(environ) {
            addFunc("+") { a -> (a[0] as Double) + (a[1] as Double) }
            addFunc("-") { a -> (a[0] as Double) - (a[1] as Double) }
            addFunc("*") { a -> (a[0] as Double) * (a[1] as Double) }
            addFunc("/") { a -> (a[0] as Double) / (a[1] as Double) }
            addFunc("=") { a -> a[0] == a[1] }
            addFunc("not") { a -> !(a[0] as Boolean) }
        }
    }

    /** Creates a lambda function. */
    private fun createLambda(parent: Environ, lambda: List<AST>): LispFunc {
        val names: AST = lambda[0]
        val body: AST = lambda[1]
        return { a ->
            // Create an environment and add the lambda variables to it.
            val env = Environ(parent)
            for (i in 0..<names.children.size) env[names[i].token] = a[i]
            // When a lambda is called, it evaluates itself: here, in fact
            eval(env, body)
        }
    }

    /** The top-level eval: this is what outside callers use. */
    fun eval(ast: AST) = eval(environ, ast)

    /** The recursive evaluator at the core of the system. */
    private fun eval(env: Environ, ast: AST): Any? {
        if (ast.isAtom) return evalAtom(env, ast)

        // Special forms handle their own evaluation (so they can do special
        // things, like short-circuit for and/or, etc.)
        val specialForm = specialForms[ast.head.token]
        if (specialForm != null) return specialForm(env, ast.tail)

        // Other things are recursively evaluated.

        // Since this function returns different kinds of things, it's not
        // possible for Kotlin to know that the result of that evaluation is
        // a function --- so it needs to be coerced into that, and the compiler
        // needs to be lovingly reassured that this is ok.
        val func = eval(env, ast.head)
            ?: throw Exception("Unknown function: ${ast.head.token}")
        @Suppress("UNCHECKED_CAST")
        return (func as LispFunc).invoke(ast.tail.map { eval(env, it) as Any })
    }

    /** Evaluate, in order: a-number? a-bool? a-function? */
    private fun evalAtom(env: Environ, ast: AST): Any? =
        ast.double ?: ast.boolean ?: env[ast.token]
}