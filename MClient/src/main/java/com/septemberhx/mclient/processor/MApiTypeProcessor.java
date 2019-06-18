package com.septemberhx.mclient.processor;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;
import com.septemberhx.mclient.annotation.MApiType;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("com.septemberhx.mclient.annotation.MApiType")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class MApiTypeProcessor extends AbstractProcessor {

    private JavacElements elementUtils;
    private Messager messager;
    private JavacTrees trees;
    private TreeMaker treeMaker;
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.trees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        this.elementUtils = (JavacElements) processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(MApiType.class);
        set.forEach(element -> {
            System.out.println("============================================================");
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) elementUtils.getTree(element);
            makeProxyMethodDecl(jcMethodDecl);
        });

        return true;
    }

    private JCTree.JCExpression memberAccess(String components) {
        String[] componentArray = components.split("\\.");
        JCTree.JCExpression expr = treeMaker.Ident(getNameFromString(componentArray[0]));
        for (int i = 1; i < componentArray.length; i++) {
            expr = treeMaker.Select(expr, getNameFromString(componentArray[i]));
        }
        return expr;
    }

    private Name getNameFromString(String s) { return names.fromString(s); }

    private void makeProxyMethodDecl(JCTree.JCMethodDecl jcMethodDecl) {
        JCTree.JCExpression[] parameterTypes = new JCTree.JCExpression[jcMethodDecl.getParameters().length() + 2];
        JCTree.JCExpression[] parameters = new JCTree.JCExpression[jcMethodDecl.getParameters().length() + 2];
        parameterTypes[0] = memberAccess("java.lang.String");
        parameterTypes[1] = memberAccess("java.lang.String");
        parameters[0] = treeMaker.Ident(getNameFromString("id"));
        parameters[1] = treeMaker.Literal(jcMethodDecl.name.toString());

        int i = 2;
        int j = 2;
        for (JCTree.JCVariableDecl paramDecl : jcMethodDecl.getParameters()) {
            messager.printMessage(Diagnostic.Kind.NOTE, paramDecl.vartype.type.toString() + " " + paramDecl.getName() + " has been processed");
            System.out.println(paramDecl.vartype.type.toString() + " " + paramDecl.getName() + " has been processed");
            parameterTypes[i++] = memberAccess(paramDecl.vartype.type.toString());
            parameters[j++] = treeMaker.Ident(getNameFromString(paramDecl.name.toString()));
        }
        List<JCTree.JCExpression> parameterTypeList = List.from(parameterTypes);

        treeMaker.pos = jcMethodDecl.pos;
        JCTree.JCExpressionStatement restJudgeStatement = treeMaker.Exec(treeMaker.Apply(
                List.of(memberAccess("java.lang.String"), memberAccess("java.lang.String")),
                memberAccess("com.septemberhx.mclient.core.MClient.isRestNeeded"),
                List.of(treeMaker.Ident(getNameFromString("id")), treeMaker.Literal(jcMethodDecl.name.toString()))
        ));

        JCTree.JCExpressionStatement restCallStatement = treeMaker.Exec(treeMaker.Apply(
                parameterTypeList,
                memberAccess("com.septemberhx.mclient.core.MClient.restRequest"),
                List.from(parameters)
        ));

        jcMethodDecl.body = treeMaker.Block(0, List.of(
                treeMaker.If(
                        treeMaker.Binary(
                                JCTree.Tag.EQ,
                                restJudgeStatement.getExpression(),
                                treeMaker.Literal(true)
                        ),
                        treeMaker.Return(treeMaker.TypeCast(jcMethodDecl.getReturnType(), restCallStatement.getExpression())),
                        jcMethodDecl.body
                )
        ));
    }
}
