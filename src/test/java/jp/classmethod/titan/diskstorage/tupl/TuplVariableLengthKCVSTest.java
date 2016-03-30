/*
 * Copyright 2016 Classmethod, Inc. or its affiliates. All Rights Reserved.
 * Portions copyright Titan: Distributed Graph Database - Copyright 2012 and onwards Aurelius.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package jp.classmethod.titan.diskstorage.tupl;

import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.thinkaurelius.titan.diskstorage.BackendException;
import com.thinkaurelius.titan.diskstorage.KeyColumnValueStoreTest;
import com.thinkaurelius.titan.diskstorage.keycolumnvalue.KeyColumnValueStoreManager;

import jp.classmethod.titan.TuplStorageSetup;

/**
 * @author Alexander Patrikalakis
 *
 */
// BEGIN adaptation of:
// https://github.com/thinkaurelius/titan/blob/1.0.0/titan-berkeleyje/src/test/java/com/thinkaurelius/titan/diskstorage/berkeleyje/BerkeleyVariableLengthKCVSTest.java#L13
public class TuplVariableLengthKCVSTest extends KeyColumnValueStoreTest {
    @Override
    public KeyColumnValueStoreManager openStorageManager() throws BackendException {
        return TuplStorageSetup.getKCVStorageManager();
    }

    // TODO broken
    @Test @Override
    public void testConcurrentGetSliceAndMutate() throws BackendException, ExecutionException, InterruptedException {

    }
}
// END adaptation of:
// https://github.com/thinkaurelius/titan/blob/1.0.0/titan-berkeleyje/src/test/java/com/thinkaurelius/titan/diskstorage/berkeleyje/BerkeleyVariableLengthKCVSTest.java#L34