package kf

typealias Func = (a: List<Any>) -> Any?
typealias SpecialForm = (env: Environment, a: List<AST>) -> Any?

/** The interpreter for the Lisp system.
 *
 * This has the definitions of the built-in words ('+', '-', etc.), as well
 * as the "special words", and core evaluation functions for evaluating an
 * expression.
 */

class Interpreter(private val environment: Environment = Environment(null)) {
    /** Special forms are not evaluated; the author of them must evaluate them.
     *
     * This is what allows short-circuiting in logical expressions and is
     * what lambda and definitions use.
     */
    private val specialForms = mutableMapOf<String, SpecialForm>(
        "or" to { env, a -> a.any { eval(env, it) as Boolean } as Any },
        "and" to { env, a -> a.all { eval(env, it) as Boolean } as Any },
        "if" to { env, a ->
            if (eval(env, a[0]) as Boolean) eval(env, a[1])
            else if (a.size > 2) eval(env, a[2]) else null
        },
        "define" to { env, a -> env[a[0].token] = eval(env, a[1]) },
        "lambda" to ::lambda,
    )

    /** Add these words to the top-level environment. */
    init {
        with(environment) {
            addFunc("+") { a -> (a[0] as Double) + (a[1] as Double) }
            addFunc("*") { a -> (a[0] as Double) * (a[1] as Double) }
            addFunc("-") { a -> (a[0] as Double) - (a[1] as Double) }
            addFunc("/") { a -> (a[0] as Double) / (a[1] as Double) }
            addFunc("not") { a -> !(a[0] as Boolean) }
            addFunc("=") { a -> a[0] == a[1] }
        }
    }

    /** Creates a lambda function. */
    private fun lambda(parent: Environment, lambda: List<AST>): Func {
        val names: AST = lambda[0]
        val body: AST = lambda[1]
        return { a ->
            // Create an environment and add the lambda variables to it.
            val env = Environment(parent)
            for (i in 0..<names.children.size) env[names[i].token] = a[i]
            // When a lambda is called, it evaluates itself: here, in fact
            eval(env, body)
        }
    }

    /** The top-level eval: this is what outside callers use. */
    fun eval(ast: AST) = eval(environment, ast)

    /** The recursive evaluator at the core of the system. */
    private fun eval(env: Environment, ast: AST): Any? {
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
        if (func == null) throw Exception("Unknown function: ${ast.head.token}")
        @Suppress("UNCHECKED_CAST")
        return (func as Func).invoke(ast.tail.map {
            eval(env, it) as Any
        })
    }

    /** Evaluate, in order: a-number? a-bool? a-function? */
    private fun evalAtom(env: Environment, ast: AST): Any? =
        ast.double ?: ast.boolean ?: env[ast.token]
}