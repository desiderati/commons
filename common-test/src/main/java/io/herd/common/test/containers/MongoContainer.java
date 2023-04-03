/*
 * Copyright (c) 2023 - Felipe Desiderati
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
package io.herd.common.test.containers;

import org.testcontainers.containers.GenericContainer;

import java.util.HashSet;
import java.util.Set;

/**
 * Remove it when Test Containers project releases its own implementation.
 */
@SuppressWarnings("unused")
public class MongoContainer extends GenericContainer<MongoContainer> {

    private static final String IMAGE = "mongo";
    private static final String DEFAULT_TAG = "4.0.5";
    private static final Integer MONGO_PORT = 27017;

    private String username = "test";
    private String password = "test";

    public MongoContainer() {
        super(IMAGE + ":" + DEFAULT_TAG);
    }

    public MongoContainer(String dockerImageName) {
        super(dockerImageName);
    }

    @Override
    public Set<Integer> getLivenessCheckPortNumbers() {
        return new HashSet<>(getMappedPort(MONGO_PORT));
    }

    @Override
    protected void configure() {
        addExposedPort(MONGO_PORT);
        addEnv("MONGO_INITDB_ROOT_USERNAME", username);
        addEnv("MONGO_INITDB_ROOT_PASSWORD", password);
        setStartupAttempts(3);
    }

    public String getConnectionUri() {
        return "mongodb://" + username + ":" +password + "@"
            + getHost() + ":" + getMappedPort(MONGO_PORT) +  "/";
    }

    @SuppressWarnings("SameReturnValue")
    public String getAuthenticationDatabase() {
        return "admin";
    }

    @SuppressWarnings("SameReturnValue")
    public String getDatabase() {
        return "local";
    }
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public MongoContainer withUsername(final String username) {
        this.username = username;
        return self();
    }

    public MongoContainer withPassword(final String password) {
        this.password = password;
        return self();
    }
}
