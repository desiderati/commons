/*
 * Copyright (c) 2025 - Felipe Desiderati
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
package io.herd.common.web.security.sign_request;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@link SignRequestAuthorizationClient} entities.
 * This interface provides methods for retrieving authorized clients by their unique identifier.
 * <p>
 * The primary responsibility of this repository is to support the sign request authorization
 * process by enabling the lookup of authorized clients from a persistent or in-memory data source.
 */
public interface SignRequestAuthorizationClientRepository {

    /**
     * Finds a {@link SignRequestAuthorizationClient} by its unique identifier.
     *
     * @param id the unique identifier of the {@link SignRequestAuthorizationClient} to be retrieved.
     * @return an {@link Optional} containing the {@link SignRequestAuthorizationClient} if found,
     * or an empty {@link Optional} if not.
     */
    Optional<SignRequestAuthorizationClient> findById(UUID id);

}
