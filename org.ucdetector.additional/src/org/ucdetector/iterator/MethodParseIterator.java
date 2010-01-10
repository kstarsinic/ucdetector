/**
 * Copyright (c) 2010 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.iterator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.ucdetector.search.UCDProgressMonitor;
import org.ucdetector.util.JavaElementUtil;

/**
 * Detect double class names
 */
@SuppressWarnings("nls")
public class MethodParseIterator extends AdditionalIterator {
  private static final List<IMethod> visitedMethods = new ArrayList<IMethod>();
  private static List<IMethod> allMethods = null;

  @Override
  public void handleStartGlobal(IJavaElement[] javaElements) throws CoreException {
    visitedMethods.clear();
    if (javaElements == null || javaElements.length == 0) {
      return;// nothing to do
    }
    IJavaElement javaElement = javaElements[0];
    if (javaElement instanceof IMethod) {
      startParsing((IMethod) javaElement);
      return;
    }
    IType type = JavaElementUtil.getTypeFor(javaElement, true);
    if (type != null) {
      IMethod mainMethod = JavaElementUtil.getMainMethod(type);
      if (mainMethod != null) {
        startParsing(mainMethod);
        return;
      }
    }
    System.err.println("No method to parse found");
  }

  private static void startParsing(IMethod method) throws CoreException {
    MethodCollectIterator methodCollector = new MethodCollectIterator();
    methodCollector.setMonitor(new UCDProgressMonitor());
    methodCollector.iterate(method.getJavaProject());
    allMethods = methodCollector.getAllmethods();
    parse(method);
    //    System.out.println("allMethods:\n" + JavaElementUtil.dumpJavaElements(allMethods));
  }

  private static void parse(IMethod method) throws JavaModelException {
    if (visitedMethods.contains(method)) {
      return;
    }
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    ICompilationUnit compilationUnit = method.getCompilationUnit();
    parser.setSource(compilationUnit); // compilationUnit needed for resolve bindings!
    parser.setKind(ASTParser.K_COMPILATION_UNIT);
    parser.setResolveBindings(true);
    final int offset = method.getNameRange().getOffset();
    ASTNode ast = parser.createAST(null);
    ASTVisitor visitor = new MethodInvocationVisitor(offset);
    ast.accept(visitor);
    visitedMethods.add(method);
  }

  private static final class MethodInvocationVisitor extends ASTVisitor {
    private final int offset;

    private MethodInvocationVisitor(int offset) {
      this.offset = offset;
    }

    @Override
    public boolean visit(MethodDeclaration declaration) {
      int start = declaration.getStartPosition();
      int length = declaration.getLength();
      boolean found = start < offset && offset <= (start + length);
      //        System.out.printf("found %s: %s%n", found, declaration.getName());
      return found;
    }

    @Override
    public boolean visit(MethodInvocation node) {
      //        System.out.println("MethodInvocation: " + node.toString());
      IMethodBinding methodBinding = node.resolveMethodBinding();
      //        System.out.println("methodBinding: " + methodBinding);
      IMethod methodFound = (IMethod) methodBinding.getJavaElement();
      if (methodFound.isBinary()) {
        return true;
      }
      if (visitedMethods.contains(methodFound)) {
        return true;
      }
      System.out.println("methodFound: " + methodFound);
      try {
        parse(methodFound);
      }
      catch (JavaModelException e) {
        e.printStackTrace();
      }
      //        ITypeBinding typeBinding = node.resolveTypeBinding();
      //        System.out.println("typeBinding: " + typeBinding);
      return true;
    }
  }

  @Override
  public void handleEndGlobal(IJavaElement[] objects) throws CoreException {
    System.out.println("END:\n");
    System.out.println("-----------------------------\nvisitedMethod:\n"
        + JavaElementUtil.getElementNames(visitedMethods));
    List<IMethod> unusedMethods = new ArrayList<IMethod>(allMethods);
    unusedMethods.removeAll(visitedMethods);
    //    System.out.println("-----------------------------\nunusedMethods:\n"
    //        + JavaElementUtil.dumpJavaElements(unusedMethods));
  }

  /**
   * do a simple "report"
   */
  @Override
  public String getMessage() {
    return "Finished parsing method";
  }

  @Override
  public String getJobName() {
    return "Parse method";
  }
}
