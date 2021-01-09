package com.github.bisgardo.intellij.closebrace;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;

public class InsertClosingBraceAction extends AnAction {
	
	@Override
	public void actionPerformed(AnActionEvent e) {
		Editor editor = e.getData(CommonDataKeys.EDITOR);
		if (editor == null) {
			return;
		}
		
		Caret caret = editor.getCaretModel().getCurrentCaret();
		Document document = editor.getDocument();
		
		int offset = caret.getOffset();
		char end = findEndOfUnmatchedBeginBrace(document.getCharsSequence(), offset);
		if (end == 0) {
			// No unmatched begin brace found.
			showPopup(JBPopupFactory.getInstance().guessBestPopupLocation(editor));
			return;
		}
		
		// Insert end brace of unmatched begin at caret and move caret.
		insert(e.getProject(), document, caret, offset, String.valueOf(end));
	}
	
	private char findEndOfUnmatchedBeginBrace(CharSequence contents, int offset) {
		class ClosingBrace {
			final ClosingBrace prev;
			final char end;
			
			ClosingBrace(ClosingBrace prev, char end) {
				this.prev = prev;
				this.end = end;
			}
		}
		
		// Stack of end braces found when scanning back from the caret.
		ClosingBrace head = null;
		
		// Scan back through document starting at the offset.
		for (int i = offset - 1; i >= 0; i--) {
			char c = contents.charAt(i);
			if (isEnd(c)) {
				// Case: 'c' is an end brace: push to stack.
				head = new ClosingBrace(head, c);
				continue;
			}
			
			char end = matchingEnd(c);
			if (end == 0) {
				// Case: 'c' is not a begin (nor end) brace.
				continue;
			}
			
			// Case: 'c' is a begin brace with matching end brace 'end'.
			// Ignore (i.e. pop) any mismatching end braces.
			while (head != null && head.end != end) {
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
	
	private static boolean isEnd(char c) {
		switch (c) {
			case ')':
			case ']':
			case '}':
			case '>':
				return true;
		}
		return false;
	}
	
	private static char matchingEnd(char c) {
		switch (c) {
			case '(': return ')';
			case '[': return ']';
			case '{': return '}';
			case '<': return '>';
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
