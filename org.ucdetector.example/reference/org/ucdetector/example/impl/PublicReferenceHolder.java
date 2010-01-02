package org.ucdetector.example.impl;

import static org.ucdetector.example.bugs.Bug2783734StaticImports.STATIC_IMPORT_USED;
import static org.ucdetector.example.bugs.Bug2783734StaticImports.staticImportMethod;

import org.ucdetector.example.IgnoreAnnotationExample;
import org.ucdetector.example.Java5Example;
import org.ucdetector.example.MixedExample;
import org.ucdetector.example.NoUcdAnnotationExample;
import org.ucdetector.example.NoUcdTagExample;
import org.ucdetector.example.QuickFixExample;
import org.ucdetector.example.ReferencedByTestsExample;
import org.ucdetector.example.SlowExample;
import org.ucdetector.example.bugs.Bug2139142Interface;
import org.ucdetector.example.bugs.Bug2225016;
import org.ucdetector.example.bugs.Bug2269486;
import org.ucdetector.example.bugs.Bug2539795Main;
import org.ucdetector.example.bugs.Bug2721955;
import org.ucdetector.example.bugs.Bug2743872;
import org.ucdetector.example.bugs.Bug2743908_2804064;
import org.ucdetector.example.bugs.Bug2776029FinalField;
import org.ucdetector.example.bugs.Bug2776029FinalFieldBase;
import org.ucdetector.example.bugs.Bug2779970;
import org.ucdetector.example.bugs.Bug2844899_FieldFromInnerClass;
import org.ucdetector.example.bugs.Bug2844899_Use;
import org.ucdetector.example.bugs.Bug2864967;
import org.ucdetector.example.bugs.Bug2865051;
import org.ucdetector.example.bugs.impl.Bug2225016Impl;
import org.ucdetector.example.classes.AnnotationExample;
import org.ucdetector.example.classes.AnonymousClass;
import org.ucdetector.example.classes.ClassInJarExample;
import org.ucdetector.example.classes.LocalClassExample;
import org.ucdetector.example.classes.MemberClassExample;
import org.ucdetector.example.enums.EnumExample;
import org.ucdetector.example.enums.EnumExampleUsedWithValueOf;
import org.ucdetector.example.enums.EnumExampleUsedWithValues;
import org.ucdetector.example.fields.FieldExamples;
import org.ucdetector.example.fields.FieldNeverRead;
import org.ucdetector.example.fields.FieldNeverWrite;
import org.ucdetector.example.fields.FinalFieldExamples;
import org.ucdetector.example.fields.SerializationFieldExample;
import org.ucdetector.example.inheritance.FinalMethodExample;
import org.ucdetector.example.inheritance.FinalMethodImplExample;
import org.ucdetector.example.inheritance.ImplementsExample;
import org.ucdetector.example.inheritance.InterfaceExample;
import org.ucdetector.example.inheritance.InterfaceImplExample;
import org.ucdetector.example.inheritance.InterfaceNotImplementedExample;
import org.ucdetector.example.inheritance.InterfaceUnusedMethodExample;
import org.ucdetector.example.inheritance.InterfaceUnusedMethodExampleImpl;
import org.ucdetector.example.inheritance.OverrideExample;
import org.ucdetector.example.inheritance.OverrideImpl2Example;
import org.ucdetector.example.inheritance.OverrideImplExample;
import org.ucdetector.example.methods.AbstractMethodExample;
import org.ucdetector.example.methods.BeanExample;
import org.ucdetector.example.methods.ConstructorExample;
import org.ucdetector.example.methods.ConstructorImplExample;
import org.ucdetector.example.methods.JavaDocExampleMethod;
import org.ucdetector.example.methods.MethodExamples;
import org.ucdetector.example.methods.MethodReferenceInJarExample;
import org.ucdetector.example.methods.MethodsSameNameExample;
import org.ucdetector.example.methods.ObjectClassMethodsExample;
import org.ucdetector.example.methods.SerializationMethodExample;
import org.ucdetector.example.methods.UnnecessaryBoolParam;
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
    // ------------------------------------------------------------------------
    System.out.println(EnumExampleUsedWithValues.values());
    System.out.println(EnumExampleUsedWithValueOf.valueOf("test"));
    // ------------------------------------------------------------------------
    UnnecessaryBoolParam ubp = new UnnecessaryBoolParam();
    ubp.unnecessaryBool("test", true);
    ubp.unnecessaryBool("test", true);
    //
    ubp.necessaryBool("test", true);
    ubp.necessaryBool("test", false);
    // ------------------------------------------------------------------------
    System.out.println(SlowExample.class.getName());
    System.out.println(Bug2844899_FieldFromInnerClass.class.getName());
    System.out.println(Bug2844899_Use.class.getName());
    System.out.println(Bug2864967.class.getName());
    System.out.println(Bug2865051.class.getName());
    //
    System.out.println(IgnoreAnnotationExample.class.getName());
    System.out.println(InterfaceUnusedMethodExampleImpl.class.getName());
    //
    Object o = "";
    InterfaceUnusedMethodExample interfaceUnusedMethodExample = ((InterfaceUnusedMethodExample) o);
    interfaceUnusedMethodExample.used();
    //
    System.out.println(Bug2779970.class.getName());
    System.out.println(ClassInJarExample.class.getName());
    System.out.println(Bug2776029FinalField.class.getName());
    System.out.println(Bug2776029FinalFieldBase.class.getName());
    // ---------------------------------------------------------------------
    System.out.println(NoUcdAnnotationExample.class.getName());
    // ---------------------------------------------------------------------
    System.out.println(STATIC_IMPORT_USED);
    staticImportMethod();
    // System.out.println(Bug2783734StaticImports.class.getName());
    // ---------------------------------------------------------------------
    System.out.println(Bug2743908_2804064.class.getName());
    System.out.println(Bug2743872.class.getName());
    new Bug2743872(1);
    // new Bug2743872();
    // ---------------------------------------------------------------------
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
    System.out.println(new OverrideImpl2Example());
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
    System.out.println(new QuickFixExample('c'));
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
    new MixedExample().helper();
    new MixedExample().helper();
    String s = new MixedExample().readOnlyField;
    s = new MixedExample().readOnlyField;
    // System.out.println(new MixedExample().exampleField);
    MixedExample.usedOnceMethod();
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
    MemberClassExample.class.getName();
    LocalClassExample.class.getName();
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
    ImplementsExample.class.getName();
    ConstructorImplExample.class.getName();

    new ConstructorImplExample(1, "2");

    System.out.println(QuickFixExample.USE_FINAL1);
    System.out.println(QuickFixExample.USE_FINAL2);
    System.out.println(QuickFixExample.USE_FINAL3);
    System.out.println(QuickFixExample.USE_FINAL4);
    System.out.println(QuickFixExample.USE_FINAL5);
    // ---------------------------------------------------------------------
    System.out.println(Bug2225016Impl.class.getName());
    System.out.println(Bug2225016.class.getName());
    System.out.println(InterfaceNotImplementedExample.class.getName());
    System.out.println(Bug2139142Interface.class.getName());
    System.out.println(Bug2269486.class.getName());
    // ---------------------------------------------------------------------
    FieldNeverRead fieldNeverRead = new FieldNeverRead();
    FieldNeverRead.NEVER_READ_FIELD = "WRITE";
    fieldNeverRead.neverReadField = "write";
    //
    FieldNeverWrite fieldNeverWrite = new FieldNeverWrite();
    System.out.println(FieldNeverWrite.NEVER_WRITE_FIELD);
    System.out.println(fieldNeverWrite.neverWriteField);
    //
    System.out.println(Bug2539795Main.class);
    //
    System.out.println(ReferencedByTestsExample.class);
    System.out.println(Bug2721955.class);
    new ReferencedByTestsExample().referencedByTestClassAndNormalClass();
  }
}
