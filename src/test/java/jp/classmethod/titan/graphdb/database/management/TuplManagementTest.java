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
package jp.classmethod.titan.graphdb.database.management;

import com.thinkaurelius.titan.diskstorage.configuration.WriteConfiguration;
import com.thinkaurelius.titan.graphdb.database.management.ManagementTest;

import jp.classmethod.titan.TuplStorageSetup;

/**
 * Test schema naming constraint enforcement
 * @author Alexander Patrikalakis
 *
 */
public class TuplManagementTest extends ManagementTest {
    @Override
    public WriteConfiguration getConfiguration() {
        return TuplStorageSetup.getTuplStorageWriteConfiguration();
    }

}
