package org.ucdetector.mini_cycle;

public class MiniB extends MiniA {
    static final MiniC MINI_C_2 = new MiniC();

    MiniA miniA = new MiniA();

    MiniC getMiniC() {
        return new MiniC();
    }

    MiniA getMiniA() {
    	return new MiniA();
    }
    
}
