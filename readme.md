# Lisp interpreter in Kotlin.

This is a toy Lisp interpreter. I built it for a presentation I want to give
on Church-v-Turing concepts seen through the frame of Lisp vs Forth.

It's small and, since it's written in Kotlin, easy to read and hack on.

## Installing

```shell
./gradlew shadowJar
java -jar build/libs/klisp-1.0-all.jar
```

(I'm currently building it for JVMs for Java 21+, but I suspect you could
lower that in the `build.gradle.kts` and everything will be fine)

Using gradle to run terminal apps doesn't work well, since it tries to control
the entire console while it's running. So I haven't made anything like
`gradlew run` work -- but the Jar file that it makes above has everything
needed, including the Kotlin standard library.

I was assuming that I'd hate JLine as Java bloatware, but, while hideously
under-documented, it has a lot of nice features for making CLIs. It was easy
to get Lisp-environment-aware autosuggestions and color syntax highlighting
added.
