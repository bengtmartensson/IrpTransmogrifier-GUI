# IrpTransmogrifier-GUI

*Note: This is very early, experimental stuff. It is currently **not usable for non-programmers**.
However, feedback of all sorts is welcome. And, of course, contributions.*

As the name suggests, this project consists of an experimental GUI for
[IrpTransmogrifier](https://github.com/bengtmartensson/IrpTransmogrifier).
It is intended as a testbench to try some (for me) new things, like Gradle and Java Swing's internal frames.

In the future, this work might be merged either into IrpTransmogrifier, or [IrScrutinizer](https://github.com/bengtmartensson/harctoolboxbundle).

To build, a recent version of [Gradle](https://gradle.org/) is needed.
(The Fedora version is not recent enough.)

`gradle build` builds a jar with the main classes. To build a jar with all dependencies, use the
command `gradle shadowJar`. The latter creates a jar like `build/libs/IrpTransmogrifierGUI-`_version_`-all.jar`,
that can be executed in the normal way.