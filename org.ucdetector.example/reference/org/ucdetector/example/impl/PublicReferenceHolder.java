package org.ucdetector.example.impl;

import org.ucdetector.example.Java5Example;
import org.ucdetector.example.MixedExample;
import org.ucdetector.example.NoUcdTagExample;
import org.ucdetector.example.QuickFixExample;
import org.ucdetector.example.classes.AnnotationExample;
import org.ucdetector.example.classes.AnonymousClass;
import org.ucdetector.example.classes.EnumExample;
import org.ucdetector.example.fields.FieldExamples;
import org.ucdetector.example.fields.FinalFieldExamples;
import org.ucdetector.example.fields.SerializationFieldExample;
import org.ucdetector.example.inheritance.FinalMethodExample;
import org.ucdetector.example.inheritance.FinalMethodImplExample;
import org.ucdetector.example.inheritance.ImplementsExample;
import org.ucdetector.example.inheritance.InterfaceExample;
import org.ucdetector.example.inheritance.InterfaceImplExample;
import org.ucdetector.example.inheritance.OverrideExample;
import org.ucdetector.example.inheritance.OverrideImplExample;
import org.ucdetector.example.inheritance.bug.A;
import org.ucdetector.example.inheritance.bug.B;
import org.ucdetector.example.inheritance.bug.C;
import org.ucdetector.example.methods.AbstractMethodExample;
import org.ucdetector.example.methods.BeanExample;
import org.ucdetector.example.methods.ConstructorExample;
import org.ucdetector.example.methods.JavaDocExampleMethod;
import org.ucdetector.example.methods.MethodExamples;
import org.ucdetector.example.methods.MethodReferenceInJarExample;
import org.ucdetector.example.methods.MethodsSameNameExample;
import org.ucdetector.example.methods.ObjectClassMethodsExample;
import org.ucdetector.example.methods.SerializationMethodExample;
import org.ucdetector.example.visibility.ProtectedReferenceHolderVisibility;
import org.ucdetector.example.visibility.UsePrivateClass;
import org.ucdetector.example.visibility.UseProtectedClass;

/**
 * {@link org.ucdetector.example.classes.JavaDocClassExample}
 * 
 * {@link org.ucdetector.example.fields.FieldExamples#usedByJavadoc}
 * 
 * {@link org.ucdetector.example.methods.MethodExamples#usedByJavaDoc()}
 */
@SuppressWarnings("unused")
public class PublicReferenceHolder {
  @SuppressWarnings("deprecation")
  @AnnotationExample(parameterExmaple = "1")
  public static void main(String[] args) throws Throwable {
    FieldExamples fieldExamples = new FieldExamples();
    System.out.println(FieldExamples.USED_FIELD);
    System.out.println(fieldExamples.publicUsedField);
    // ---------------------------------------------------------------------
    System.out.println(InterfaceExample.USED);
    System.out.println(InterfaceImplExample.class);
    // ---------------------------------------------------------------------
    MethodExamples methodExamples = new MethodExamples();
    methodExamples.usedPublicMethod();
    methodExamples.start();
    methodExamples.run();
    methodExamples.append();
    // ---------------------------------------------------------------------
    MethodsSameNameExample sameNameExample = new MethodsSameNameExample();
    sameNameExample.sameName();
    sameNameExample.sameName(new Long(2L), 3);
    // ---------------------------------------------------------------------
    System.out.println(EnumExample.class);
    System.out.println(EnumExample.USED);
    System.out.println(ConstructorExample.class.getName());
    // ---------------------------------------------------------------------
    System.out.println(new OverrideExample());
    System.out.println(new OverrideImplExample());
    // ---------------------------------------------------------------------
    FinalMethodExample finalMethodExample = new FinalMethodExample();
    finalMethodExample.methodOverridden();
    finalMethodExample.methodNotOverridden();
    finalMethodExample.finalMethod();
    FinalMethodExample.staticMethod();
    FinalMethodImplExample finalMethodImplExample = new FinalMethodImplExample();
    finalMethodImplExample.methodOverridden();
    finalMethodImplExample.methodNotOverridden();
    finalMethodImplExample.noOverrideNoOverridden();
    // ---------------------------------------------------------------------
    //
    // System.out.println(new SelfReferenceExample());
    // ---------------------------------------------------------------------
    ProtectedReferenceHolderVisibility ref = new ProtectedReferenceHolderVisibility();
    // ---------------------------------------------------------------------
    System.out.println(FinalFieldExamples.class);
    System.out.println(FinalFieldExamples.DONT_USE_FINAL);
    System.out.println(FinalFieldExamples.USE_FINAL);
    // ---------------------------------------------------------------------
    System.out.println(UsePrivateClass.class);
    UsePrivateClass usePrivateClass = new UsePrivateClass();
    System.out.println(usePrivateClass.dontUsePrivate_public);
    System.out.println(UsePrivateClass.DONT_USE_PRIVATE);
    usePrivateClass.dontUsePrivate();
    UsePrivateClass.dontUsePrivateStatic();
    // ---------------------------------------------------------------------
    UseProtectedClass useProtectedClass = new UseProtectedClass();
    useProtectedClass.dontUseProtected();
    UseProtectedClass.dontUseProtectedStatic();
    // ---------------------------------------------------------------------
    ObjectClassMethodsExample oc = new ObjectClassMethodsExample();
    oc.toString();
    // System.out.println(oc.clone("a"));
    // System.out.println(oc.equals("s", "a"));
    // System.out.println(oc.hashCode("a"));
    // System.out.println(oc.toString("a"));
    // oc.finalize("a");
    // ---------------------------------------------------------------------
    System.out.println(AnonymousClass.class);
    // ---------------------------------------------------------------------
    System.out.println(new QuickFixExample());
    // ---------------------------------------------------------------------
    System.out.println("org.ucdetector.example.classes.ClassNameInJavaFile");
    // ---------------------------------------------------------------------
    FinalFieldExamples ffe = new FinalFieldExamples();
    ffe.getFieldSetInSetter();
    ffe.setFieldSetInSetter(3);
    //
    FinalFieldExamples.geStatictFieldSetInSetter();
    FinalFieldExamples.setStaticFieldSetInSetter(3);
    // ---------------------------------------------------------------------
    new MixedExample().makeFinalMethod();
    // ---------------------------------------------------------------------
    Java5Example java5Example = new Java5Example();
    java5Example.annotatedMethod2();
    java5Example.doNotUse();
    java5Example.genericExample(null);
    java5Example.varargExample("s", 1, 2, 3);
    // ---------------------------------------------------------------------
    new SerializationMethodExample();
    // ---------------------------------------------------------------------
    AbstractMethodExample.class.getName();
    org.ucdetector.example.classes.InnerClassExample.class.getName();
    JavaDocExampleMethod.class.getName();

    new JavaDocExampleMethod().usedMethod();
    // ---------------------------------------------------------------------
    System.out.println(NoUcdTagExample.USED);
    System.out.println(NoUcdTagExample.USED2);
    MethodReferenceInJarExample.class.getName();
    // ---------------------------------------------------------------------
    BeanExample.class.getName();
    SerializationFieldExample.class.getName();
    // ---------------------------------------------------------------------
    A.class.getName();
    B.class.getName();
    C.class.getName();
    ImplementsExample.class.getName();
    
    System.out.println(QuickFixExample.USE_FINAL);
    System.out.println(QuickFixExample.USE_FINAL2);
    System.out.println(QuickFixExample.USE_FINAL3);
    System.out.println(QuickFixExample.USE_FINAL4);
    // ---------------------------------------------------------------------
  }
}
