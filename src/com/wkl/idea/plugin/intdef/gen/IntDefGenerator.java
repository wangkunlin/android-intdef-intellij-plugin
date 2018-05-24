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

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.wkl.idea.plugin.intdef.gen.util.Pair;

import java.util.List;

/**
 * Created by <a href="mailto:wangkunlin1992@gmail.com">Wang kunlin</a>
 * on 2018-05-23.
 */
class IntDefGenerator {

    private PsiClass psiClass;
    private String className;
    private List<Pair<String, String>> datas;

    IntDefGenerator(PsiClass psiClass, String className, List<Pair<String, String>> datas) {
        this.psiClass = psiClass;
        this.className = className.substring(0, 1).toUpperCase() + className.substring(1);
        this.datas = datas;
    }

    void generate() throws Throwable {
        PsiElementFactory elementFactory = PsiElementFactory.SERVICE.getInstance(psiClass.getProject());
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(psiClass.getProject());

        PsiJavaFile javaFile = (PsiJavaFile) psiClass.getContainingFile();

        boolean isInterface = psiClass.isInterface();
        StringBuilder sb = new StringBuilder("\n");

        StringBuilder names = new StringBuilder("@IntDef({");

        PsiElement anchor = null;
        int index = 0;
        for (Pair<String, String> pair : datas) {
            sb.delete(0, sb.length());
            if (!isInterface) {
                sb.append("public static final ");
            }
            names.append(pair.first).append(", ");
            sb.append("int ")
                    .append(pair.first.toUpperCase())
                    .append(" = ")
                    .append(pair.second)
                    .append(";");
            sb.append("\n");
            PsiField staticField = elementFactory.createFieldFromText(sb.toString(), psiClass);
            PsiElement psiElement;
            if (index == 0) {
                anchor = psiClass.getLastChild();
                psiElement = psiClass.addBefore(staticField, anchor);
            } else {
                psiElement = psiClass.addAfter(staticField, anchor);
            }
            styleManager.shortenClassReferences(psiElement);
            anchor = psiElement;
            index++;
        }
        int len = names.length();
        names.delete(len - 2, len);
        names.append("})\n");
        names.append("@Retention(java.lang.annotation.RetentionPolicy.SOURCE)\n");
        if (!isInterface) {
            names.append("public ");
        }
        names.append("@interface ").append(className).append(" {\n");
        names.append("}");

        String intDefBody = "public @interface IntDef {\n" +
                "}";
        PsiClass intDefClass = createPsiClass(intDefBody, "IntDef",
                "android.support.annotation");
        String retentionBody = "public @interface Retention {\n"
                + "}";
        PsiClass retentionClass = createPsiClass(retentionBody, "Retention",
                "java.lang.annotation");

        javaFile.importClass(intDefClass);
        javaFile.importClass(retentionClass);

        PsiClass innerClass = createPsiClass(names.toString(), className, "");
        psiClass.addAfter(innerClass, anchor);

        styleManager.shortenClassReferences(psiClass);
        styleManager.optimizeImports(javaFile);

    }

    private PsiClass createPsiClass(String body, String fileName, String packageName) {
        PsiJavaFile intDefFile = (PsiJavaFile) PsiFileFactory.getInstance(psiClass.getProject())
                .createFileFromText(fileName + ".java", JavaFileType.INSTANCE, body);
        if (packageName != null && !packageName.isEmpty()) {
            intDefFile.setPackageName(packageName);
        }
        return intDefFile.getClasses()[0];
    }

}
