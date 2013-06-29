/*
 * Copyright (c) 2013 Yan Pujante
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.pongasoft.glu.packaging.setup

import org.linkedin.glu.groovy.utils.shell.Shell
import org.linkedin.util.io.resource.Resource
import org.pongasoft.glu.provisioner.core.metamodel.AgentMetaModel
import org.pongasoft.glu.provisioner.core.metamodel.ConsoleMetaModel
import org.pongasoft.glu.provisioner.core.metamodel.GluMetaModel
import org.pongasoft.glu.provisioner.core.metamodel.ZooKeeperClusterMetaModel

/**
 * @author yan@pongasoft.com  */
public class GluPackager
{
  GluMetaModel gluMetaModel

  Shell shell

  Resource configsRoot
  Resource packagesRoot
  Resource outputFolder
  Resource keysRoot

  def packagedArtifacts = [:]

  boolean dryMode = false

  void packageAll()
  {
    packageAgents()
    packageConsoles()
    packageZooKeeperClusters()
  }

  void packageAgents()
  {
    gluMetaModel.agents.each { AgentMetaModel model ->
      PackagedArtifact pa = packageAgent(model)
      packagedArtifacts[model] = pa
      if(!dryMode)
        println "Generated agent ${pa.location} ${pa.host}:${pa.port}"
    }
  }

  void packageConsoles()
  {
    gluMetaModel.consoles.values().each { ConsoleMetaModel model ->
      PackagedArtifact pa = packageConsole(model)
      packagedArtifacts[model] = pa
      if(!dryMode)
        println "Generated console ${pa.location} ${pa.host}:${pa.port}"
    }
  }

  void packageZooKeeperClusters()
  {
    gluMetaModel.zooKeeperClusters.values().each { ZooKeeperClusterMetaModel model ->
      def pas = packageZooKeeperCluster(model)
      packagedArtifacts[model] = pas
      if(!dryMode)
      {
        pas.zooKeepers.each { zki ->
          println "Generated ZooKeeper instance ${zki.location} ${zki.host}:${zki.port}"
        }
        println "Generated ZooKeeper cluster ${pas.zooKeeperCluster.location} ${model.zooKeeperConnectionString}"
      }
    }
  }

  protected PackagedArtifact packageAgent(AgentMetaModel agentMetaModel)
  {
    def out = shell.mkdirs(outputFolder.createRelative('agents'))
    def packager =
      new AgentServerPackager(packagerContext: createPackagerContext(),
                              outputFolder: out,
                              inputPackage: getInputPackage('org.linkedin.glu.agent-server',
                                                            agentMetaModel.version),
                              configsRoot: configsRoot,
                              metaModel: agentMetaModel,
                              dryMode: dryMode)
    packager.createPackage()
  }

  protected PackagedArtifact packageConsole(ConsoleMetaModel consoleMetaModel)
  {
    def out = shell.mkdirs(outputFolder.createRelative('consoles'))
    def packager =
      new ConsoleServerPackager(packagerContext: createPackagerContext(),
                                outputFolder: out,
                                inputPackage: getInputPackage('org.linkedin.glu.console-server',
                                                              consoleMetaModel.version),
                                configsRoot: configsRoot,
                                metaModel: consoleMetaModel,
                                dryMode: dryMode)
    packager.createPackage()
  }

  protected def packageZooKeeperCluster(ZooKeeperClusterMetaModel zooKeeperClusterMetaModel)
  {
    def out = shell.mkdirs(outputFolder.createRelative('zookeeper-clusters'))
    def packager =
      new ZooKeeperClusterPackager(packagerContext: createPackagerContext(),
                                   outputFolder: out,
                                   inputPackage: getInputPackage('org.linkedin.zookeeper-server',
                                                                 zooKeeperClusterMetaModel.zooKeepers[0].version),
                                   configsRoot: configsRoot,
                                   metaModel: zooKeeperClusterMetaModel,
                                   dryMode: dryMode)
    packager.createPackage()
  }

  protected PackagerContext createPackagerContext()
  {
    new PackagerContext(shell: shell,
                        keysRoot: keysRoot ?: outputFolder.createRelative('keys'))
  }

  protected Resource getInputPackage(String name, String version)
  {
    def inputPackage = packagesRoot.createRelative("${name}-${version}")

    if(!inputPackage.exists())
      inputPackage = packagesRoot.createRelative("${name}-${version}.tgz")

    if(!inputPackage.exists())
      throw new FileNotFoundException("${inputPackage} does not exist")

    return inputPackage
  }

}