package com.joelburton.klisp

val parser = Parser()
val interp = Interpreter()

/** Print one or more s-exps and print the eval of the final one. */
fun peval(vararg sexps: String) =
    sexps.map { interp.eval(parser(it)) }.lastOrNull().also { println(it) }

fun main() {
    peval("(define pi 3.14159)", "pi")

    peval(
        """
        (define nested 
          (lambda (a b) 
            (lambda (c) 
              (* (+ a b) c))))          
    """,
        "((nested 3 4) 5)"
    )

    peval(
        """
        (define factorial
          (lambda (n)
            (if (= n 1)
              1
              (* n (factorial (- n 1))))))
    """,
        "(factorial 5)"
    )
}