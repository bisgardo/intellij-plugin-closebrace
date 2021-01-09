# intellij-plugin-closebrace

Plugin for finding and close last unmatched opening brace (`(`, `[`, `{`, or `<`) before the current caret.

## Install

Invoke the gradle task `buildPlugin`:
```
./gradlew buildPlugin
```

This builds the plugin into `build/distributions/intellij-plugin-closebrace-<version>.zip`.

In Settings > Plugins, select "Install Plugin from Disk..." from the gear symbol and select the file above.

## Usage

The default key binding for the action is `Alt-p`.

*Example*

Consider an editor with the following contents, where `|` represents the current caret position:
```
x(y[z|
```
After invoking the action once, this has changed to
```
x(y[z]|
```
Invoking it again makes it
```
x(y[z])|
```

### Unmatched end braces

Any unmatched closing braces are ignored:

*Example*

```
x(yz]|
```
becomes
```
x(yz])|
```

The same thing goes for closing braces after the caret:

*Example*

```
x(y|z)
```
becomes
```
x(y)|z)
```

