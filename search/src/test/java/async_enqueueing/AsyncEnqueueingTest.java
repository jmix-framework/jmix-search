/*
 * Copyright 2021 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package async_enqueueing;

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.search.index.queue.IndexingQueueManager;
import io.jmix.search.index.queue.entity.EnqueueingSession;
import io.jmix.search.index.queue.impl.EnqueueingSessionAction;
import io.jmix.search.index.queue.impl.IndexingOperation;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AsyncEnqueueingTestConfiguration;
import test_support.TestCommonEntityWrapperManager;
import test_support.TestIndexingQueueItemsTracker;
import test_support.entity.TestRootEntity;

import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {AsyncEnqueueingTestConfiguration.class}
)
public class AsyncEnqueueingTest {

    @Autowired
    TestIndexingQueueItemsTracker indexingQueueItemsTracker;
    @Autowired
    IndexingQueueManager indexingQueueManager;
    @Autowired
    TestCommonEntityWrapperManager ewm;

    @Autowired
    Metadata metadata;
    @Autowired
    DataManager dataManager;

    @BeforeEach
    public void setUp() {
        indexingQueueItemsTracker.clear();

        List<EnqueueingSession> sessions = dataManager.load(EnqueueingSession.class).all().list();
        sessions.forEach(session -> dataManager.remove(session));

        List<TestRootEntity> entities = dataManager.load(TestRootEntity.class).all().list();
        entities.forEach(entity -> dataManager.remove(entity));
    }

    @Test
    @DisplayName("Initialize enqueueing session for entity")
    public void initSession() {
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);
        List<EnqueueingSession> sessions = dataManager.load(EnqueueingSession.class).all().list();

        Assert.assertEquals(1, sessions.size());

        EnqueueingSession session = sessions.get(0);
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals("id", session.getOrderingProperty());
        Assert.assertEquals(EnqueueingSessionAction.EXECUTE, session.getAction());
        Assert.assertNull(session.getLastProcessedValue());
    }

    @Test
    @DisplayName("Stop enqueueing session for entity")
    public void stopSession() {
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        indexingQueueManager.stopAsyncEnqueueIndexAll(entityName);
        EnqueueingSession session = dataManager.load(EnqueueingSession.class).all().one();
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals(EnqueueingSessionAction.STOP, session.getAction());
    }

    @Test
    @DisplayName("Suspend enqueueing session for entity")
    public void suspendSession() {
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        indexingQueueManager.suspendAsyncEnqueueIndexAll(entityName);
        EnqueueingSession session = dataManager.load(EnqueueingSession.class).all().one();
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals(EnqueueingSessionAction.SKIP, session.getAction());
    }

    @Test
    @DisplayName("Resume suspending enqueueing session for entity")
    public void resumeSession() {
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        indexingQueueManager.suspendAsyncEnqueueIndexAll(entityName);
        EnqueueingSession session = dataManager.load(EnqueueingSession.class).all().one();
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals(EnqueueingSessionAction.SKIP, session.getAction());

        indexingQueueManager.resumeAsyncEnqueueingIndexAll(entityName);
        session = dataManager.load(EnqueueingSession.class).all().one();
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals(EnqueueingSessionAction.EXECUTE, session.getAction());
    }

    @Test
    @DisplayName("Process next enqueueing session")
    public void processNextSession() {
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        String entityName = metadata.getClass(TestRootEntity.class).getName();

        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        int processed = indexingQueueManager.processNextEnqueueingSession(2);
        int itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(2, processed);
        Assert.assertEquals(2, itemsInQueue);

        EnqueueingSession session = dataManager.load(EnqueueingSession.class).all().one();
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals(EnqueueingSessionAction.EXECUTE, session.getAction());
        Assert.assertNotNull(session.getLastProcessedValue());

        processed = indexingQueueManager.processNextEnqueueingSession(2);
        itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(1, processed);
        Assert.assertEquals(3, itemsInQueue);

        List<EnqueueingSession> sessions = dataManager.load(EnqueueingSession.class).all().list();
        Assert.assertTrue(sessions.isEmpty());
    }

    @Test
    @DisplayName("Process enqueueing session for entity")
    public void processSessionForEntity() {
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        int processed = indexingQueueManager.processEnqueueingSession(entityName, 2);
        int itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(2, processed);
        Assert.assertEquals(2, itemsInQueue);

        EnqueueingSession session = dataManager.load(EnqueueingSession.class).all().one();
        Assert.assertEquals(entityName, session.getEntityName());
        Assert.assertEquals(EnqueueingSessionAction.EXECUTE, session.getAction());
        Assert.assertNotNull(session.getLastProcessedValue());

        processed = indexingQueueManager.processEnqueueingSession(entityName, 2);
        itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(1, processed);
        Assert.assertEquals(3, itemsInQueue);

        List<EnqueueingSession> sessions = dataManager.load(EnqueueingSession.class).all().list();
        Assert.assertTrue(sessions.isEmpty());
    }

    @Test
    @DisplayName("Process suspended-resumed enqueueing session")
    public void processSuspendedAndResumedSession() {
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        indexingQueueManager.suspendAsyncEnqueueIndexAll(entityName);

        int processed = indexingQueueManager.processEnqueueingSession(entityName, 2);
        int itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(0, processed);
        Assert.assertEquals(0, itemsInQueue);

        indexingQueueManager.resumeAsyncEnqueueingIndexAll(entityName);

        processed = indexingQueueManager.processEnqueueingSession(entityName, 2);
        itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(2, processed);
        Assert.assertEquals(2, itemsInQueue);
    }

    @Test
    @DisplayName("Process stopped enqueueing session")
    public void processStoppedSession() {
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        ewm.createTestRootEntity().save();
        String entityName = metadata.getClass(TestRootEntity.class).getName();
        indexingQueueManager.initAsyncEnqueueIndexAll(entityName);

        indexingQueueManager.stopAsyncEnqueueIndexAll(entityName);

        int processed = indexingQueueManager.processEnqueueingSession(entityName, 2);
        int itemsInQueue = indexingQueueItemsTracker.getAmountOfItemsForEntity(entityName, IndexingOperation.INDEX);
        Assert.assertEquals(0, processed);
        Assert.assertEquals(0, itemsInQueue);

        List<EnqueueingSession> sessions = dataManager.load(EnqueueingSession.class).all().list();
        Assert.assertTrue(sessions.isEmpty());
    }
}
