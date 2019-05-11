package io.nyaruko.braceselect;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;

public class Select extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        if(e.getInputEvent().isAltDown()) return; //Override for this selection mode

        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if(editor == null) return; //Clicked a non-text panel
        if(editor.getCaretModel().getAllCarets().size() == 1) {
            Caret caret = editor.getCaretModel().getCurrentCaret();
            int leftPos = caret.getSelectionStart();

            if(leftPos > 0) {//Prevent out of bounds
                char left = editor.getDocument().getText(new TextRange(leftPos-1, leftPos)).charAt(0);
                //Check left char is matchable
                if("\"'({[<".lastIndexOf(left) != -1) {
                    //Get matching character and setup area to check
                    char right = getRightMatch(left);
                    char[] rawLine = editor.getDocument().getText(new TextRange(leftPos, caret.getVisualLineEnd()-1)).toCharArray();

                    //Offset to select up to
                    int rightOffset = -1;
                    //Counter for handling nested braces
                    int braceLevel = 1;
                    for (int i = 0; i < rawLine.length; i++) {
                        if(rawLine[i] == left) {
                            if(left != right) braceLevel++;
                            else if(i != 0 && rawLine[i-1] == '\\') continue;
                        }

                        //Char match found, check this is the outer most nesting layer
                        if (rawLine[i] == right) {
                            braceLevel--;
                            if(braceLevel == 0) {
                                rightOffset = i;
                                break;
                            }
                        }
                    }

                    //Select between braces
                    caret.setSelection(leftPos, leftPos + rightOffset);
                    caret.moveToOffset(leftPos + rightOffset);
                }
            }
        }
    }

    private char getRightMatch(char left){
        switch (left){
            case '"':
                return '"';
            case '\'':
                return '\'';
            case '(':
                return ')';
            case '{':
                return '}';
            case '[':
                return ']';
            case '<':
                return '>';
        }
        return left;
    }
}
