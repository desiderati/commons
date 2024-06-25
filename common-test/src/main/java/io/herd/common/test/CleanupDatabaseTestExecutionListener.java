/*
 * Copyright (c) 2024 - Felipe Desiderati
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.herd.common.test;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.lang.reflect.Method;

public class CleanupDatabaseTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public final int getOrder() {
        return 2000;
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        Class<?> testClass = testContext.getTestClass();
        Method testMethod = testContext.getTestMethod();
        CleanupDatabase methodAnn = AnnotatedElementUtils.findMergedAnnotation(testMethod, CleanupDatabase.class);
        CleanupDatabase classAnn = AnnotatedElementUtils.findMergedAnnotation(testClass, CleanupDatabase.class);

        boolean methodAnnotated = (methodAnn != null);
        boolean classAnnotated = (classAnn != null);
        CleanupDatabase.Mode mode = (classAnnotated ? classAnn.mode() : null);

        if (methodAnnotated || mode == CleanupDatabase.Mode.AFTER_EACH_TEST_METHOD) {
            cleanupDatabase(testContext);
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        Class<?> testClass = testContext.getTestClass();
        CleanupDatabase classAnn = AnnotatedElementUtils.findMergedAnnotation(testClass, CleanupDatabase.class);

        boolean classAnnotated = (classAnn != null);
        CleanupDatabase.Mode mode = (classAnnotated ? classAnn.mode() : null);

        if (mode == CleanupDatabase.Mode.AFTER_CLASS) {
            cleanupDatabase(testContext);
        }
    }

    private void cleanupDatabase(TestContext testContext) throws LiquibaseException {
        ApplicationContext app = testContext.getApplicationContext();
        SpringLiquibase springLiquibase = app.getBean(SpringLiquibase.class);
        springLiquibase.setDropFirst(true);
        springLiquibase.afterPropertiesSet(); // The database will be recreated here!
    }
}
