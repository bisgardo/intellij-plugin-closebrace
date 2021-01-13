# intellij-plugin-closebrace

IntelliJ Plugin for finding the last unmatched opening brace (`(`, `[`, `{`, or `<`) before the current caret
and closing it at the caret.

## Install

Invoke the gradle task `buildPlugin`:
```
./gradlew buildPlugin
```

This outputs the plugin in file `build/distributions/intellij-plugin-closebrace-<version>.zip`.

In Settings > Plugins, select "Install Plugin from Disk..." from the gear symbol and select the file above.

## Usage

The default key binding for the action is `Alt-P`.

*Example*

Consider an editor with the following contents, where `|` represents the current caret position:
```
x(y[z|
```
After invoking the action once, this has changed to
```
x(y[z]|
```
Invoking it again results in
```
x(y[z])|
```

### Unmatched closing braces

Any unmatched closing braces are ignored, so
```
x(yz]|
```
becomes
```
x(yz])|
```

The same thing goes for closing braces after the caret; i.e.
```
x(y|z)
```
becomes
```
x(y)|z)
```
