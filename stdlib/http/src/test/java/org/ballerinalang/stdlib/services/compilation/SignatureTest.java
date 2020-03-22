/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.services.compilation;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Objects;

/**
 * Resource signature compiler validation test class.
 *
 * @since 0.95.6
 */
public class SignatureTest {

    @Test()
    public void testSignatureWithSingleParam() {
        CompileResult compileResult = BCompileUtil.compileOffline(new File(
                Objects.requireNonNull(getClass().getClassLoader().getResource(
                        "test-src/services/signature/no-request-param.bal")).getPath()).getAbsolutePath());

        Assert.assertEquals(compileResult.getErrorCount(), 12);

        Diagnostic[] diagnostics = compileResult.getDiagnostics().clone();

        assertError(diagnostics[0], 9, 5, "resource signature parameter count should be >= 2");
        assertError(diagnostics[1], 13, 5, "first parameter should be of type `ballerina/http:Caller`");
        assertError(diagnostics[2], 17, 5, "second parameter should be of type `ballerina/http:Request`");
        assertError(diagnostics[3], 21, 5, "second parameter should be of type `ballerina/http:Request`");
        assertError(diagnostics[4], 25, 75, "missing annotation of parameter `key`: expected `@http:PathParam`, " +
                "`@http:QueryParam`, `@http:BodyParam`");
        assertError(diagnostics[5], 29, 86, "invalid path param: `boolean key`, missing segment `{key}` in the " +
                "path config of the resource annotation");
        assertError(diagnostics[6], 33, 83, "incompatible entity-body param type: `int`, expected `string`, `json`, " +
                "`xml`, `byte[]`, `{}`, `{}[]`");
        assertError(diagnostics[7], 44, 78, "missing annotation of parameter `p`: expected `@http:PathParam`, " +
                "`@http:QueryParam`, `@http:BodyParam`");
        assertError(diagnostics[8], 51, 82, "incompatible path param type: `json`, expected `string`, `int`, " +
                "`boolean`, `float`");
        assertError(diagnostics[9], 55, 83, "incompatible query param type: `json`, expected `string`, `string[]`");
        assertError(diagnostics[10], 59, 84, "incompatible query param type: `int[]`, expected `string`, `string[]`");
        assertError(diagnostics[11], 63, 90, "invalid multiple `@http:BodyParam` annotations: cannot specify > 1 " +
                "entity-body params");
    }

    private void assertError(Diagnostic diagnostic, int line, int column, String message) {
        Assert.assertEquals(diagnostic.getPosition().getStartLine(), line, "incorrect error line");
        Assert.assertEquals(diagnostic.getPosition().getStartColumn(), column, "incorrect error column");
        Assert.assertEquals(diagnostic.getMessage(), message, "incorrect error message");
    }

    @Test
    public void testSignatureWithInvalidReturn() {
        CompileResult compileResult = BCompileUtil.compile(new File(
                Objects.requireNonNull(getClass().getClassLoader().getResource(
                        "test-src/services/signature/invalid-return.bal")).getPath()).getAbsolutePath());

        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics().clone()[0].getMessage(), "invalid resource function return" +
                " type 'int', expected a subtype of 'error?' containing '()'");
    }

    @Test
    public void testDuplicateResources() {
        CompileResult compileResult = BCompileUtil.compile(new File(
                Objects.requireNonNull(getClass().getClassLoader().getResource(
                        "test-src/services/resources/duplicate_resource_test.bal")).getPath()).getAbsolutePath());
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        BAssertUtil.validateError(compileResult, 0, "redeclared symbol 'dataservice$$service$0.employee'", 8, 23);
    }
}
