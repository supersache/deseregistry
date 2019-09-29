# deseregistry

deseregistry is a simple approach to build a tool that supports the manual search for Java deserialization gadgets. It does not aim to do any automated graph database search thingy but instead provide a simple relational database tool that helps the researcher perform a manual search. To do, it tries to provide a class registry which can be queried by simple SQL in order to quickly find answers to questions like:

* which classes implement `java.io.Serializable`?
* which classes implement both `java.io.Serializable` and `InvocationHandler`?
* which classes have a method `private void readObject(java.io.ObjectInputStream)` and implement `java.util.Map`?

The author used to use Eclipse a lot during research and Eclipse is already pretty good at answering quesions like these quickly but it does not allow to combine search queries like these so if you're looking for a serializable InvocationHandler, you have to do a lot of manual stuff and aggregate the results manually, afterwards, which is not a good thing when you have tons of classes to analyze.

See <a href="requirements.md">requirements</a> for more details.
