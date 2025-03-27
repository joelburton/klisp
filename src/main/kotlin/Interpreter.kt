package com.joelburton.klisp

typealias LispFunc = (a: List<Any>) -> Any?
typealias SpecialForm = (env: Environ, a: List<Ast>) -> Any?

/** The interpreter for the Lisp system.
 *
 * This has the definitions of the built-in words ('+', '-', etc.), as well
 * as the "special words", and core evaluation functions for evaluating an
 * expression.
 */

class Interpreter(val environ: Environ = Environ(null)) {

    /** The top-level eval: this is what outside callers use. */
    fun eval(ast: Ast) = eval(environ, ast)

    /** Special forms are not evaluated; the author of them must evaluate them.
     *
     * This is what allows short-circuiting in logical expressions and is
     * what lambda and definitions use.
     */
    val specialForms = mutableMapOf<String, SpecialForm>(
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
    private fun createLambda(parent: Environ, lambda: List<Ast>): LispFunc {
        val names: Ast = lambda[0]
        val body: Ast = lambda[1]
        return { a ->
            // Create an environment and add the lambda variables to it.
            val env = Environ(parent)
            for (i in 0..<names.children.size) env[names[i].token] = a[i]
            // When a lambda is called, it evaluates itself: here, in fact
            eval(env, body)
        }
    }

    /** The recursive evaluator at the core of the system. */
    private fun eval(env: Environ, ast: Ast): Any? {
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
    private fun evalAtom(env: Environ, ast: Ast): Any? =
        ast.double ?: ast.boolean ?: env[ast.token]
}