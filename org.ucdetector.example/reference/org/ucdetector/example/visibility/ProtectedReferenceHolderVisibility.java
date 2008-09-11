package org.ucdetector.example.visibility;

import org.ucdetector.example.impl.PublicReferenceHolder;

public class ProtectedReferenceHolderVisibility {
    public static void main(String[] args) {
        // ---------------------------------------------------------------------
        System.out.println(UseProtectedClass.USE_PROTECTED);
        UseProtectedClass.useProtectedStatic();

        UseProtectedClass useProtectedClass = new UseProtectedClass();
        System.out.println(useProtectedClass.useProtected_default);
        System.out.println(useProtectedClass.useProtected_protected);
        System.out.println(useProtectedClass.useProtected_public);
        useProtectedClass.useProtected();
        // ---------------------------------------------------------------------
        new PublicReferenceHolder();
        // ---------------------------------------------------------------------
		UsePrivateClass usePrivateClass = new UsePrivateClass();
		System.out.println(usePrivateClass.dontUsePrivate_default);
		System.out.println(usePrivateClass.dontUsePrivate_protected);

        // ---------------------------------------------------------------------
		System.out.println(UseDefaultClass.class);
        // ---------------------------------------------------------------------

        // ---------------------------------------------------------------------

    }
}
