package com.septemberhx.mclient.processor;

import com.google.auto.service.AutoService;
import com.septemberhx.mclient.annotation.MRestApiType;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
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

@SupportedAnnotationTypes({"com.septemberhx.mclient.annotation.MApiType", "com.septemberhx.mclient.annotation.MRestApiType"})
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

        Set<? extends Element> set2 = roundEnv.getElementsAnnotatedWith(MRestApiType.class);
        set2.forEach(element -> {
            System.out.println("Deal with MRestApiType...");
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) elementUtils.getTree(element);
            JCTree.JCClassDecl jcClassDecl = (JCTree.JCClassDecl) elementUtils.getTree((element.getEnclosingElement()));
            JCTree.JCMethodDecl newMethodDecl = makeRestOverloadFunction(jcMethodDecl);
            jcClassDecl.defs = jcClassDecl.defs.prepend(newMethodDecl);
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

    private JCTree.JCMethodDecl makeRestOverloadFunction(JCTree.JCMethodDecl jcMethodDecl) {
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);

        JCTree.JCExpression[] parameterTypes = new JCTree.JCExpression[jcMethodDecl.getParameters().length()];
        JCTree.JCExpression[] parameters = new JCTree.JCExpression[jcMethodDecl.getParameters().length()];

//        JCTree.JCStatement[] jcStatements = new JCTree.JCStatement[jcMethodDecl.getParameters().length()];
        // prepare the functions that the raw function needed
        int i = 0;
        for (JCTree.JCVariableDecl paramDecl : jcMethodDecl.getParameters()) {
//            jcStatements[i++] = treeMaker.VarDef(
//                    treeMaker.Modifiers(0),
//                    paramDecl.getName(),
//                    paramDecl.vartype,
//                    treeMaker.Exec(
//                        treeMaker.TypeCast(
//                            paramDecl.vartype,
//                            treeMaker.Apply(
//                                List.of(memberAccess("java.lang.String")),
//                                treeMaker.Select(
//                                    treeMaker.Ident(names.fromString("jsonParameters")),
//                                    names.fromString("get")
//                                ),
//                                List.of(treeMaker.Literal(paramDecl.name.toString()))
//                            )
//                        )
//                    ).getExpression()
//            );
            parameterTypes[i] = paramDecl.vartype;
            parameters[i++] = treeMaker.TypeCast(
                paramDecl.vartype,
                treeMaker.Apply(
                    List.of(memberAccess("java.lang.String")),
                    treeMaker.Select(
                            treeMaker.Ident(names.fromString("jsonParameters")),
                            names.fromString("get")
                    ),
                    List.of(treeMaker.Literal(paramDecl.name.toString()))
                )
            );
        }

        // prepare the parameters in the new-generated function
        JCTree.JCVariableDecl parameterDecl =
                treeMaker.VarDef(
                        treeMaker.Modifiers(Flags.PARAMETER),
                        names.fromString("jsonParameters"),
                        memberAccess("org.json.JSONObject"),
                        null
                );

        // call raw function with parameters in jsonParameters
        JCTree.JCExpression funcCall = treeMaker.Apply(
                List.from(parameterTypes),
                treeMaker.Select(
                        treeMaker.Ident(names.fromString("this")),
                        jcMethodDecl.getName()
                ),
                List.from(parameters)
        );
        JCTree.JCExpressionStatement callJudgeStatement = treeMaker.Exec(treeMaker.Apply(
                List.of(memberAccess("java.lang.String"), memberAccess("java.lang.String")),
                memberAccess("com.septemberhx.mclient.core.MClientSkeleton.checkIfContinue"),
                List.of(treeMaker.Ident(getNameFromString("id")), treeMaker.Literal(jcMethodDecl.name.toString()))
        ));
        JCTree.JCMethodDecl resultMethodDecl = treeMaker.MethodDef(
                modifiers,
                jcMethodDecl.name,
                memberAccess("org.json.JSONObject"),
                List.<JCTree.JCTypeParameter>nil(),
                List.<JCTree.JCVariableDecl>of(parameterDecl),
                List.<JCTree.JCExpression>nil(),
                treeMaker.Block(0, List.of(
                    treeMaker.If(
                            treeMaker.Binary(
                                    JCTree.Tag.EQ,
                                    callJudgeStatement.getExpression(),
                                    treeMaker.Literal(true)
                            ),
                            treeMaker.Return(funcCall),
                            treeMaker.Return(treeMaker.Ident(parameterDecl.getName()))
                        )
                    )
                ),
                null
        );
        return resultMethodDecl;
    }

    private void makeProxyMethodDecl(JCTree.JCMethodDecl jcMethodDecl) {
        JCTree.JCExpression[] parameterTypes = new JCTree.JCExpression[jcMethodDecl.getParameters().length() * 2 + 3];
        JCTree.JCExpression[] parameters = new JCTree.JCExpression[jcMethodDecl.getParameters().length() * 2 + 3];
        // mObjectId
        parameterTypes[0] = memberAccess("java.lang.String");
        // functionName
        parameterTypes[1] = memberAccess("java.lang.String");
        // return type
        parameterTypes[2] = memberAccess("java.lang.String");

        parameters[0] = treeMaker.Ident(getNameFromString("id"));
        parameters[1] = treeMaker.Literal(jcMethodDecl.name.toString());
        parameters[2] = treeMaker.Literal(jcMethodDecl.restype.type.toString());

        int i = 3;
        int j = 3;
        for (JCTree.JCVariableDecl paramDecl : jcMethodDecl.getParameters()) {
            messager.printMessage(Diagnostic.Kind.NOTE, paramDecl.vartype.type.toString() + " " + paramDecl.getName() + " has been processed");
            System.out.println(paramDecl.vartype.type.toString() + " " + paramDecl.getName() + " has been processed");
            parameterTypes[i++] = memberAccess("java.lang.String");
            parameters[j++] = treeMaker.Literal(paramDecl.name.toString());

            parameterTypes[i++] = memberAccess(paramDecl.vartype.type.toString());
            parameters[j++] = treeMaker.Ident(getNameFromString(paramDecl.name.toString()));
        }
        List<JCTree.JCExpression> parameterTypeList = List.from(parameterTypes);

        treeMaker.pos = jcMethodDecl.pos;
        JCTree.JCExpressionStatement restJudgeStatement = treeMaker.Exec(treeMaker.Apply(
                List.of(memberAccess("java.lang.String"), memberAccess("java.lang.String")),
                memberAccess("com.septemberhx.mclient.core.MClientSkeleton.isRestNeeded"),
                List.of(treeMaker.Ident(getNameFromString("id")), treeMaker.Literal(jcMethodDecl.name.toString()))
        ));

        JCTree.JCExpressionStatement restCallStatement = treeMaker.Exec(treeMaker.Apply(
                parameterTypeList,
                memberAccess("com.septemberhx.mclient.core.MClientSkeleton.restRequest"),
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
