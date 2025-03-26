package kf

val parser = Parser()
val interpreter = Interpreter()

fun peval(s: String) = parser.parse(s).also { println(interpreter.eval(it)) }

fun main() {
    peval("(define one (lambda (a) a))")
    peval("(one 2)")
    peval("(define pi 3.14159)")
    peval("pi")
    peval("(* (+ 1 2) 4)")
    peval("(or true false false true false)")
    peval("""
        (if 
          (not 
            (and
              (= (+ 1 1) 2)
              (= (* 1 1) 1)))
          42)
    """)
    peval("""
        (if 
          (not 
            (and
              (= (+ 1 1) 2)
              (= (* 1 1) 2)))
          42)
    """)
    peval("(lambda (a b) (* (+ a b) b))")
    peval("""
    (define nested 
      (lambda (a b) 
        (lambda (c) 
          (* (+ a b) c))))          
    """
    )
    peval("((nested 3 4) 5)")
}