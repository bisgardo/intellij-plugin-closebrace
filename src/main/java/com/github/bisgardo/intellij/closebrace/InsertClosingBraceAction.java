package com.github.bisgardo.intellij.closebrace;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;

public class InsertClosingBraceAction extends AnAction {
	
	@Override
	public void actionPerformed(AnActionEvent e) {
		Editor editor = e.getData(CommonDataKeys.EDITOR);
		if (editor != null) {
			InsertBrace.action(editor, e.getProject(), false);
		}
	}
}
