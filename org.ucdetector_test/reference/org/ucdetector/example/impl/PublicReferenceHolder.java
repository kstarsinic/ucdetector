package org.ucdetector.example.impl;

import org.ucdetector.example.AbstractMethodExample;
import org.ucdetector.example.AnnotationExample;
import org.ucdetector.example.AnonymousClass;
import org.ucdetector.example.BadMarkerExample;
import org.ucdetector.example.ConstructorExample;
import org.ucdetector.example.EnumExample;
import org.ucdetector.example.FieldExamples;
import org.ucdetector.example.FinalFieldExamples;
import org.ucdetector.example.FinalMethodExample;
import org.ucdetector.example.FinalMethodImplExample;
import org.ucdetector.example.InterfaceExample;
import org.ucdetector.example.InterfaceImplExample;
import org.ucdetector.example.Java5Example;
import org.ucdetector.example.JavaDocExampleMethod;
import org.ucdetector.example.MethodExamples;
import org.ucdetector.example.MethodsSameNameExample;
import org.ucdetector.example.MixedExample;
import org.ucdetector.example.NoUcdTagExample;
import org.ucdetector.example.ObjectClass;
import org.ucdetector.example.OverrideExample;
import org.ucdetector.example.OverrideImplExample;
import org.ucdetector.example.QuickFixExample;
import org.ucdetector.example.ReferenceInJarExample;
import org.ucdetector.example.SerializationExample;
import org.ucdetector.example.visibility.ProtectedReferenceHolderVisibility;
import org.ucdetector.example.visibility.UsePrivateClass;
import org.ucdetector.example.visibility.UseProtectedClass;

/**
 * See {@link org.ucdetector.example.JavaDocExample}
 */
@SuppressWarnings("unused")
public class PublicReferenceHolder {
  // unusued

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
    ObjectClass oc = new ObjectClass();
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
    System.out.println("org.ucdetector.example.ClassNameInJavaFile");
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
    new SerializationExample();
    // ---------------------------------------------------------------------
    AbstractMethodExample.class.getName();
    org.ucdetector.example.InnerClassExample.class.getName();
    JavaDocExampleMethod.class.getName();

    new JavaDocExampleMethod().usedMethod();
    // ---------------------------------------------------------------------
    System.out.println(NoUcdTagExample.USED);
    System.out.println(NoUcdTagExample.USED2);
    ReferenceInJarExample.class.getName();
    // ---------------------------------------------------------------------
    System.out.println(new BadMarkerExample());
    System.out.println(BadMarkerExample.USED_MARKER_NO);
    System.out.println(BadMarkerExample.USED_MARKER_YES);
    // ---------------------------------------------------------------------
  }
}
