/*
 * Copyright 2012 JBoss by Red Hat.
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
package org.jbpm.console.ng.pr.backend.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.droolsjbpm.services.impl.model.NodeInstanceDesc;
import org.jbpm.console.ng.pr.model.NodeInstanceSummary;

/**
 *
 * @author salaboy
 */
public class NodeInstanceHelper {
    public static Collection<NodeInstanceSummary> adaptCollection(Collection<NodeInstanceDesc> nodeInstances){
        List<NodeInstanceSummary> nodeInstancesSummary = new ArrayList<NodeInstanceSummary>();
        for(NodeInstanceDesc ni : nodeInstances){
            nodeInstancesSummary.add(new NodeInstanceSummary(ni.getId(), ni.getProcessInstanceId(), 
                    ni.getName(), ni.getNodeId(), ni.getNodeType(), ni.getDataTimeStamp().toString(),
                    ni.getConnection(), ni.isCompleted()));
        }
        
        return nodeInstancesSummary;
    }
    
    public static NodeInstanceSummary adapt(NodeInstanceDesc ni){
        return new NodeInstanceSummary(ni.getId(), ni.getProcessInstanceId(), 
                    ni.getName(), ni.getNodeId(), ni.getNodeType(), ni.getDataTimeStamp().toString()
                    , ni.getConnection(), ni.isCompleted());
    }
}
