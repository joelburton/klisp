package kf

/** The environment is the list of definitions available to a function.
 *
 * The are a bunch of standard things (like `+`, `-`, and other built-in
 * definitions) that will be in here, but for a lambda, this will also have
 * it's variables available. And, of course, this is recursive.
 *
 * Each environment points to its parent; the top has a null parent.
 */

class Environment(val parent: Environment? = null) {
    /** The variables available in this environment. */
    val vars: MutableMap<String, Any?> = mutableMapOf()

    /** Look up a name: start here, then recursively up through parents. */
    fun at(key: String): Any? = vars[key] ?: parent?.at(key)
}