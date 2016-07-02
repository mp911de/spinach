/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.paluch.spinach;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import biz.paluch.spinach.api.async.DisqueAsyncCommands;
import biz.paluch.spinach.api.sync.DisqueCommands;

import com.lambdaworks.redis.RedisFuture;

/**
 * @author Mark Paluch
 */
@RunWith(Parameterized.class)
public class SyncAsyncApiConvergenceTest {

    private Method method;

    @SuppressWarnings("rawtypes")
    private Class<DisqueAsyncCommands> asyncClass = DisqueAsyncCommands.class;

    @Parameterized.Parameters(name = "Method {0}/{1}")
    public static List<Object[]> parameters() {

        List<Object[]> result = new ArrayList<Object[]>();
        Method[] methods = DisqueCommands.class.getMethods();
        for (Method method : methods) {

            if (method.getName().equals("setTimeout")) {
                continue;
            }

            result.add(new Object[] { method.getName(), method });
        }

        return result;
    }

    public SyncAsyncApiConvergenceTest(String methodName, Method method) {
        this.method = method;
    }

    @Test
    public void testMethodPresentOnAsyncApi() throws Exception {
        Method method = asyncClass.getMethod(this.method.getName(), this.method.getParameterTypes());
        assertThat(method).isNotNull();
    }

    @Test
    public void testSameResultType() throws Exception {
        Method method = asyncClass.getMethod(this.method.getName(), this.method.getParameterTypes());
        Class<?> returnType = method.getReturnType();

        if (returnType.equals(RedisFuture.class)) {
            ParameterizedType genericReturnType = (ParameterizedType) method.getGenericReturnType();
            Type[] actualTypeArguments = genericReturnType.getActualTypeArguments();

            if (actualTypeArguments[0] instanceof TypeVariable) {

                assertThat(Object.class).isEqualTo(this.method.getReturnType());
                return;
            }

            if (actualTypeArguments[0] instanceof ParameterizedType) {

                ParameterizedType parameterizedType = (ParameterizedType) actualTypeArguments[0];
                returnType = (Class<?>) parameterizedType.getRawType();
            } else if (actualTypeArguments[0] instanceof GenericArrayType) {

                GenericArrayType arrayType = (GenericArrayType) actualTypeArguments[0];
                returnType = Array.newInstance((Class<?>) arrayType.getGenericComponentType(), 0).getClass();
            } else {
                returnType = (Class<?>) actualTypeArguments[0];
            }
        }

        Class<?> expectedType = getType(this.method.getReturnType());
        returnType = getType(returnType);

        assertThat(returnType).describedAs(this.method.toString()).isEqualTo(expectedType);

    }

    private Class<?> getType(Class<?> returnType) {
        if (returnType == Long.class) {
            return Long.TYPE;
        }

        if (returnType == Integer.class) {
            return Integer.TYPE;
        }

        if (returnType == Boolean.class) {
            return Boolean.TYPE;
        }
        return returnType;
    }
}
