# The engine's native side finds its Java classes and methods **by name**, through JNI.
# R8 renames what it can reach, and a renamed class is one the .so cannot find any more --
# a failure that compiles cleanly and only shows up when a turn is analysed on a phone.
# Seen here rather than guessed at: without this, `ai.onnxruntime.a` was in the release dex.
-keep class ai.onnxruntime.** { *; }
