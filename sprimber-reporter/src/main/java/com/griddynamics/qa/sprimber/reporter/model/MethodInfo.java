package com.griddynamics.qa.sprimber.reporter.model;

import javassist.CtClass;
import javassist.CtMethod;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MethodInfo {

    private final CtClass ctClass;
    private final CtMethod ctMethod;

}
