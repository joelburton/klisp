package kf

/** The environment is the list of definitions available to a function.
 *
 * The are a bunch of standard things (like `+`, `-`, and other built-in
 * definitions) that will be in here, but for a lambda, this will also have
 * it's variables available. And, of course, this is recursive.
 *
 * Each environment points to its parent; the top has a null parent.
 */

class Environment(val parent: Environment? = null) : HashMap<String, Any?>() {
    /** Look up a name: start here, then recursively up through parents. */
    override operator fun get(key: String): Any? =
        super[key] ?: parent?.get(key)
    fun addFunc(name: String, func: Func) = put(name, func)
}