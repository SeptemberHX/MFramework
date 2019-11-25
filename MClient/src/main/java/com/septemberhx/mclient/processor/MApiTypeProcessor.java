package com.septemberhx.mclient.processor;

import com.google.auto.service.AutoService;
import com.septemberhx.mclient.annotation.MRestApiType;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
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
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) elementUtils.getTree(element);
            makeProxyMethodDecl(jcMethodDecl);
        });

        Set<? extends Element> set2 = roundEnv.getElementsAnnotatedWith(MRestApiType.class);
        set2.forEach(element -> {
            System.out.println("Deal with MRestApiType...");
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) elementUtils.getTree(element);
            JCTree.JCClassDecl jcClassDecl = (JCTree.JCClassDecl) elementUtils.getTree((element.getEnclosingElement()));

//            JCTree.JCMethodDecl newMethodDecl = makeRestOverloadFunction(jcMethodDecl);
//            jcClassDecl.defs = jcClassDecl.defs.prepend(newMethodDecl);

            if (this.addHttpServletRequestToFunctionParameter(jcMethodDecl)) {
                JCTree.JCMethodDecl newMethodDecl1 = makeOverloadFunctionWithoutRequester(jcMethodDecl);
                jcClassDecl.defs = jcClassDecl.defs.prepend(newMethodDecl1);
            }
            this.transformFunction(jcClassDecl, jcMethodDecl);
//            this.addLogOutputToFunction(jcMethodDecl);
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


    private JCTree.JCVariableDecl makeVarDef(JCTree.JCModifiers modifiers, String name, JCTree.JCExpression vartype, JCTree.JCExpression init) {
        return treeMaker.VarDef(
                modifiers,
                getNameFromString(name), // name
                vartype, // type
                init // init
        );
    }

    private Name getNameFromString(String s) { return names.fromString(s); }

    private boolean addHttpServletRequestToFunctionParameter(JCTree.JCMethodDecl jcMethodDecl) {
        for (JCTree.JCVariableDecl variableDecl : jcMethodDecl.params) {
            if (variableDecl.vartype.toString().equals("javax.servlet.http.HttpServletRequest") ||
                variableDecl.vartype.toString().equals("HttpServletRequest")) {
                return false;
            }
        }

        JCTree.JCVariableDecl parameterDecl =
                treeMaker.VarDef(
                        treeMaker.Modifiers(Flags.PARAMETER),
                        names.fromString("_request"),
                        memberAccess("javax.servlet.http.HttpServletRequest"),
                        null
                );
        jcMethodDecl.params = jcMethodDecl.params.append(parameterDecl);
        return true;
    }

    private void addLogOutputToFunction(JCTree.JCMethodDecl jcMethodDecl) {
        JCTree.JCExpressionStatement callLogOutput = treeMaker.Exec(treeMaker.Apply(
                List.of(memberAccess("java.lang.String"), memberAccess("java.lang.String"), memberAccess("javax.servlet.http.HttpServletRequest")),
                memberAccess("com.septemberhx.mclient.core.MClientSkeleton.logFunctionCall"),
                List.of(treeMaker.Ident(getNameFromString("id")), treeMaker.Literal(jcMethodDecl.name.toString()), treeMaker.Literal(TypeTag.BOT, null))
        ));

        jcMethodDecl.body = treeMaker.Block(0, List.of(
                callLogOutput,
                jcMethodDecl.body
        ));
    }

    /**
     * Transform raw function with steps below:
     *   1. create a function named "__" + rawName with raw function parameters and body
     *   2. replace the raw function body with a function call to the new created function in step 1
     *   3. add log output statements around the function call created in step 2
     * @param jcClassDecl
     * @param jcMethodDecl
     */
    private void transformFunction(JCTree.JCClassDecl jcClassDecl, JCTree.JCMethodDecl jcMethodDecl) {
        // step 1: create a function named "__" + rawName
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.PRIVATE);
        JCTree.JCExpression[] parameterTypes = new JCTree.JCExpression[jcMethodDecl.getParameters().length()];
        JCTree.JCExpression[] parameters = new JCTree.JCExpression[jcMethodDecl.getParameters().length()];
        int i = 0;
        String requestObjectName = null;
        for (JCTree.JCVariableDecl variableDecl : jcMethodDecl.params) {
            parameterTypes[i] = variableDecl.vartype;
            parameters[i] = treeMaker.Ident(variableDecl.name);
            if (variableDecl.vartype.toString().equals("javax.servlet.http.HttpServletRequest") ||
                variableDecl.vartype.toString().equals("HttpServletRequest")) {
                requestObjectName = variableDecl.name.toString();
            }
            ++i;
        }

        JCTree.JCMethodDecl resultMethodDecl = treeMaker.MethodDef(
                modifiers,
                names.fromString("__" + jcMethodDecl.name.toString()),
                jcMethodDecl.restype,
                List.<JCTree.JCTypeParameter>nil(),
                List.<JCTree.JCVariableDecl>from(jcMethodDecl.params),
                List.<JCTree.JCExpression>nil(),
                jcMethodDecl.body,
                null
        );
        jcClassDecl.defs = jcClassDecl.defs.prepend(resultMethodDecl);

        // step 2 and 3: change the body of the raw function to lou information we need
        JCTree.JCExpression funcCall = treeMaker.Apply(
                List.from(parameterTypes),
                treeMaker.Select(
                        treeMaker.Ident(names.fromString("this")),
                        names.fromString("__" + jcMethodDecl.getName().toString())
                ),
                List.from(parameters)
        );
        JCTree.JCExpressionStatement callLogOutput = treeMaker.Exec(treeMaker.Apply(
                List.of(memberAccess("java.lang.String"), memberAccess("java.lang.String"), memberAccess("javax.servlet.http.HttpServletRequest")),
                memberAccess("com.septemberhx.mclient.core.MClientSkeleton.logFunctionCall"),
                List.of(treeMaker.Ident(getNameFromString("id")), treeMaker.Literal(jcMethodDecl.name.toString()), treeMaker.Ident(names.fromString(requestObjectName)))
        ));
        JCTree.JCExpressionStatement callEndLogOutput = treeMaker.Exec(treeMaker.Apply(
                List.of(memberAccess("java.lang.String"), memberAccess("java.lang.String"), memberAccess("javax.servlet.http.HttpServletRequest")),
                memberAccess("com.septemberhx.mclient.core.MClientSkeleton.logFunctionCallEnd"),
                List.of(treeMaker.Ident(getNameFromString("id")), treeMaker.Literal(jcMethodDecl.name.toString()), treeMaker.Ident(names.fromString(requestObjectName)))
        ));
        jcMethodDecl.body = treeMaker.Block(0, List.of(
                callLogOutput,
                makeVarDef(treeMaker.Modifiers(0), "_tmpResult", jcMethodDecl.restype, funcCall),
                callEndLogOutput,
                treeMaker.Return(treeMaker.Ident(names.fromString("_tmpResult")))
        ));
    }

    /**
     * This function will try to build a overload function which has no HttpServeletRequest parameter
     * @param jcMethodDecl: raw function decl
     * @return JCMethodDecl
     */
    private JCTree.JCMethodDecl makeOverloadFunctionWithoutRequester(JCTree.JCMethodDecl jcMethodDecl) {
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);
        JCTree.JCExpression[] parameterTypes = new JCTree.JCExpression[jcMethodDecl.getParameters().length()];
        JCTree.JCExpression[] parameters = new JCTree.JCExpression[jcMethodDecl.getParameters().length()];

        JCTree.JCVariableDecl[] paramCopyArr = new JCTree.JCVariableDecl[jcMethodDecl.getParameters().length() - 1];
        int i = 0;
        int metRequest = 0;
        for (JCTree.JCVariableDecl variableDecl : jcMethodDecl.params) {
            parameterTypes[i] = variableDecl.vartype;
            if (variableDecl.vartype.toString().equals("javax.servlet.http.HttpServletRequest")) {
                parameters[i] = treeMaker.Literal(TypeTag.BOT, null);
                metRequest = 1;
            } else {
                parameters[i] = treeMaker.Ident(variableDecl.name);
                paramCopyArr[i - metRequest] = variableDecl;
            }
            ++i;
        }

        JCTree.JCExpression funcCall = treeMaker.Apply(
                List.from(parameterTypes),
                treeMaker.Select(
                        treeMaker.Ident(names.fromString("this")),
                        jcMethodDecl.getName()
                ),
                List.from(parameters)
        );
        JCTree.JCMethodDecl resultMethodDecl = treeMaker.MethodDef(
                modifiers,
                jcMethodDecl.name,
                jcMethodDecl.restype,
                List.<JCTree.JCTypeParameter>nil(),
                List.<JCTree.JCVariableDecl>from(paramCopyArr),
                List.<JCTree.JCExpression>nil(),
                treeMaker.Block(0, List.of(
                        treeMaker.Return(funcCall)
                    )
                ),
                null
        );
        return resultMethodDecl;
    }

    private JCTree.JCMethodDecl makeRestOverloadFunction(JCTree.JCMethodDecl jcMethodDecl) {
        JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);

        // prepare the parameters in the new-generated function
        JCTree.JCVariableDecl parameterDecl =
                treeMaker.VarDef(
                        treeMaker.Modifiers(Flags.PARAMETER),
                        names.fromString("jsonParameters"),
                        memberAccess("com.septemberhx.common.base.MResponse"),
                        null
                );

        JCTree.JCExpression[] parameterTypes = new JCTree.JCExpression[jcMethodDecl.getParameters().length()];
        JCTree.JCExpression[] parameters = new JCTree.JCExpression[jcMethodDecl.getParameters().length()];

        // prepare the functions that the raw function needed
        int i = 0;
        for (JCTree.JCVariableDecl paramDecl : jcMethodDecl.getParameters()) {
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
                memberAccess("com.septemberhx.common.base.MResponse"),
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
