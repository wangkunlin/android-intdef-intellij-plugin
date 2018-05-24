/*
 * Copyright (c) 2018. Kunlin Wang (http://wangkunlin.date)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wkl.idea.plugin.intdef.gen;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.wkl.idea.plugin.intdef.gen.util.Pair;

import java.util.List;

/**
 * Created by <a href="mailto:wangkunlin1992@gmail.com">Wang kunlin</a>
 * on 2018/5/22.
 */
public class IntDefAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        if (psiClass == null) return;
        OptionsDialog dialog = new OptionsDialog(psiClass);
        dialog.show();

        if (dialog.isOK()) {
            generateIntDef(psiClass, dialog);
        }
    }

    private void generateIntDef(PsiClass psiClass, OptionsDialog dialog) {
        new WriteAction(psiClass, dialog.getAnnotationClassName(), dialog.getDatas()).execute();
    }

    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        e.getPresentation().setEnabled(psiClass != null
                && !psiClass.isEnum());
    }

    // copy form ParcelableAction.java
    private PsiClass getPsiClassFromContext(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        if (psiFile == null || editor == null) {
            return null;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);

        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }

    private static class WriteAction extends WriteCommandAction.Simple {

        private PsiClass psiClass;
        private String className;
        private List<Pair<String, String>> datas;

        private WriteAction(PsiClass psiClass, String className, List<Pair<String, String>> datas) {
            super(psiClass.getProject(), psiClass.getContainingFile());
            this.psiClass = psiClass;
            this.className = className;
            this.datas = datas;
        }

        @Override
        protected void run() throws Throwable {
            new IntDefGenerator(psiClass, className, datas).generate();
        }
    }
}
