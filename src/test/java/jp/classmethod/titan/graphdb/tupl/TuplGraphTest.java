/*
 * Copyright 2016 Classmethod, Inc. or its affiliates. All Rights Reserved.
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
package jp.classmethod.titan.graphdb.tupl;

import com.thinkaurelius.titan.diskstorage.configuration.WriteConfiguration;
import com.thinkaurelius.titan.graphdb.TitanGraphTest;

import jp.classmethod.titan.TuplStorageSetup;

/**
 * 
 * @author Alexander Patrikalakis
 *
 */
public class TuplGraphTest extends TitanGraphTest {

    @Override
    protected boolean isLockingOptimistic() {
        return false;
    }

    @Override
    public WriteConfiguration getConfiguration() {
        return TuplStorageSetup.getTuplStorageWriteConfiguration();
    }

}
