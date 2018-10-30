package fr.layer4.hhsl;

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

import org.junit.Test;

public class LocalClusterServiceTest {

    private LocalClusterService localClusterService;

    @Test
    public void getCluster_ok() {

        // Given

        // When
        Cluster cluster = localClusterService.getCluster("test");

        // Then
        cluster.getId();

    }

    @Test
    public void getCluster_unknown() {

        // Given

        // When
        Cluster cluster = localClusterService.getCluster("test");

        // Then
        cluster.getId();

    }

    @Test
    public void addCluster_ok() {

        // Given

        // When
        Cluster cluster = localClusterService.addCluster("type", "name", "uri", "banner");

        // Then
        cluster.getId();

    }

    @Test
    public void deleteCluster_ok() {

        // Given

        // When
        localClusterService.deleteCluster("name");

        // Then

    }

    @Test
    public void deleteCluster_unknown() {

        // Given

        // When
        localClusterService.deleteCluster("name");

        // Then

    }
}
