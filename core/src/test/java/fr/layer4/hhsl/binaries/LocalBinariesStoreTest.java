package fr.layer4.hhsl.binaries;

/*-
 * #%L
 * HHSL
 * %%
 * Copyright (C) 2018 Layer4
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Paths;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class LocalBinariesStoreTest {

    private LocalBinariesStore localBinariesStore;

    @Mock
    private ClientPreparer zookeeperClientPreparer;

    @Mock
    private ClientPreparer hbaseClientPreparer;

    @Before
    public void beforeEachTest() {
        this.localBinariesStore = new LocalBinariesStore();
        this.localBinariesStore.setClientPreparers(Arrays.asList(this.hbaseClientPreparer, this.zookeeperClientPreparer));
    }

    @After
    public void afterEachTest() {
        Mockito.verifyNoMoreInteractions(this.hbaseClientPreparer, this.zookeeperClientPreparer);
        Mockito.reset(this.hbaseClientPreparer, this.zookeeperClientPreparer);
    }

    @Test
    public void preparerFound() {

        // Given
        Mockito.when(this.hbaseClientPreparer.isCompatible(Mockito.eq("zookeeper"), Mockito.anyString())).thenReturn(false);
        Mockito.when(this.zookeeperClientPreparer.isCompatible(Mockito.eq("zookeeper"), Mockito.anyString())).thenReturn(true);

        // When
        this.localBinariesStore.prepare(Paths.get(""), "zookeeper", "3.4.3", false);

        // Then
        Mockito.verify(this.zookeeperClientPreparer).isCompatible("zookeeper", "3.4.3");
        Mockito.verify(this.hbaseClientPreparer).isCompatible("zookeeper", "3.4.3");
        Mockito.verify(this.zookeeperClientPreparer).prepare(Paths.get(""), "zookeeper", "3.4.3", false);
    }

    @Test
    public void preparerNotFound() {

        // Given
        Mockito.when(this.hbaseClientPreparer.isCompatible(Mockito.eq("unknown"), Mockito.eq("3.4.3"))).thenReturn(false);
        Mockito.when(this.zookeeperClientPreparer.isCompatible(Mockito.eq("unknown"), Mockito.eq("3.4.3"))).thenReturn(false);

        // When
        this.localBinariesStore.prepare(Paths.get(""), "unknown", "3.4.3", false);

        // Then
        Mockito.verify(this.hbaseClientPreparer).isCompatible("unknown", "3.4.3");
        Mockito.verify(this.zookeeperClientPreparer).isCompatible("unknown", "3.4.3");
    }
}
