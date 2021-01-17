package com.github.bisgardo.intellij.closebrace;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;

public class InsertBrace {
	
	public static void action(Editor editor, Project project, boolean forward) {
		Caret caret = editor.getCaretModel().getCurrentCaret();
		Document document = editor.getDocument();
		
		int offset = caret.getOffset();
		char match = findMatchOfUnmatchedBrace(document.getCharsSequence(), offset, forward);
		if (match == 0) {
			// No unmatched begin brace found.
			showPopup(JBPopupFactory.getInstance().guessBestPopupLocation(editor));
			return;
		}
		
		// Insert match brace of unmatched begin at caret and move caret.
		insert(project, document, caret, offset, String.valueOf(match));
	}
	
	public static char findMatchOfUnmatchedBrace(CharSequence contents, int offset, boolean forward) {
		class ClosingBrace {
			final ClosingBrace prev;
			final char match;
			
			ClosingBrace(ClosingBrace prev, char match) {
				this.prev = prev;
				this.match = match;
			}
		}
		
		// Stack of end braces found when scanning back from the caret.
		ClosingBrace head = null;
		
		// Scan back through document starting at the offset.
		int length = contents.length();
		for (int i = first(offset, forward); isDone(i, length, forward); i = next(i, forward)) {
			char c = contents.charAt(i);
			if (isEnd(c, forward)) {
				// Case: 'c' is an end brace: push to stack.
				head = new ClosingBrace(head, c);
				continue;
			}
			
			char end = matchingEnd(c, forward);
			if (end == 0) {
				// Case: 'c' is not a begin (nor end) brace.
				continue;
			}
			
			// Case: 'c' is a begin brace with matching end brace 'end'.
			// Ignore (i.e. pop) any mismatching end braces.
			while (head != null && head.match != end) {
				head = head.prev;
			}
			if (head != null) {
				// Pop matching end brace.
				head = head.prev;
				continue;
			}
			
			// Matching end brace of 'c' not found on the stack.
			return end;
		}
		
		// No unmatched begin brace found.
		return 0;
	}
	
	private static int first(int offset, boolean forward) {
		return forward ? offset : offset - 1;
	}
	
	private static int next(int i, boolean forward) {
		return forward ? i + 1 : i - 1;
	}
	
	private static boolean isDone(int i, int length, boolean forward) {
		return forward ? i < length : i >= 0;
	}
	
	private static void insert(Project p, Document d, Caret c, int offset, String end) {
		WriteCommandAction.runWriteCommandAction(
				p,
				"Insert Matching Brace",
				"insert_matching_brace",
				() -> {
					d.insertString(offset, end);
					c.moveCaretRelatively(1, 0, false, false);
				}
		);
	}
	
	private static boolean isEnd(char c, boolean forward) {
		return matchingEnd(c, !forward) != 0;
	}
	
	private static char matchingEnd(char c, boolean forward) {
		if (forward) {
			switch (c) {
				case ')': return '(';
				case ']': return '[';
				case '}': return '{';
				case '>': return '<';
			}
		} else {
			switch (c) {
				case '(': return ')';
				case '[': return ']';
				case '{': return '}';
				case '<': return '>';
			}
		}
		return 0;
	}
	
	private static void showPopup(RelativePoint target) {
		JBPopupFactory.getInstance()
				.createHtmlTextBalloonBuilder("No unmatched begin brace found", MessageType.INFO, null)
				.setFadeoutTime(2000)
				.createBalloon()
				.show(target, Balloon.Position.below);
	}
}
